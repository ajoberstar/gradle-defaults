package org.ajoberstar.gradle.defaults

import org.apache.maven.model.Dependency
import org.gradle.api.Plugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.testing.Test

class DefaultsPlugin implements Plugin<Project> {

  void apply(Project project) {
    if (project.rootProject != project) {
      throw new GradleException('org.ajoberstar.defaults must only be applied to the root project')
    }
    addGit(project)
    addReleaseConfig(project)

    project.allprojects { prj ->
      addSpotless(prj)
      addJavaConfig(prj)
      addGroovyConfig(prj)
      addPublishingConfig(prj)
      addPluginConfig(prj)
    }
  }

  private void addGit(Project project) {
    project.plugins.apply('org.ajoberstar.grgit')
    project.plugins.apply('org.ajoberstar.git-publish')

    def addOutput = { task ->
      project.gitPublish.contents.from(task.outputs.files) {
        into "docs${task.path}".replace(':', '/')
      }
    }

    project.allprojects { prj ->
      prj.plugins.withId('java') { addOutput(prj.javadoc) }
      prj.plugins.withId('groovy') { addOutput(prj.groovydoc) }
    }

    project.gitPublish {
      branch = 'gh-pages'
    }
  }

  private void addReleaseConfig(Project rootProject) {
    rootProject.plugins.apply('org.ajoberstar.reckon')

    rootProject.reckon {
      scopeFromProp()
      stageFromProp('alpha', 'beta', 'rc', 'final')
    }

    // safety checks before releasing
    rootProject.tasks.reckonTagCreate.doFirst {
      def grgit = rootProject.grgit
      def version = rootProject.version.toString()
      if (grgit.branch.current().name != 'master') {
        throw new IllegalStateException('Can only release from master.')
      }
      if (!version.contains('-')) {
        def head = grgit.head()
        def tagsOnHead = grgit.tag.list().findAll { it.commit == head }
        if (!tagsOnHead.find { it.name.startsWith("${version}-rc.")}) {
          throw new IllegalStateException('Must release an rc of this commit before making a final.')
        }
      }
    }

    // publish docs before tag is pushed
    rootProject.tasks.reckonTagPush.dependsOn 'gitPublishPush'

    rootProject.allprojects { prj ->
      prj.tasks.matching { it.name == 'check' }.all { task ->
        // make sure tests pass before creating tag or pushing docs
        rootProject.tasks.reckonTagCreate.dependsOn task
        rootProject.tasks.gitPublishPush.dependsOn task
      }
    }
  }

  private void addSpotless(Project project) {
    project.plugins.apply('com.diffplug.gradle.spotless')
    project.spotless {
      project.plugins.withId('java') {
        java {
          importOrder 'java', 'javax', ''
          removeUnusedImports()
          eclipse().configFile(project.rootProject.file('gradle/eclipse-java-formatter.xml'))
        }
      }
      project.plugins.withId('groovy') {
        format 'groovy', {
          target 'src/**/*.groovy'
          trimTrailingWhitespace()
          indentWithSpaces(2)
          endWithNewline()
        }
      }
      format 'gradle', {
        target '**/*.gradle'
        trimTrailingWhitespace()
        indentWithSpaces(2)
        endWithNewline()
      }
    }
  }

  private void addJavaConfig(Project project) {
    project.plugins.withId('java') {
      Task sourcesJar = project.tasks.create('sourcesJar', Jar)
      sourcesJar.with {
        classifier = 'sources'
        from project.sourceSets.main.allSource
      }

      Task javadocJar = project.tasks.create('javadocJar', Jar)
      javadocJar.with {
        classifier = 'javadoc'
        from project.tasks.javadoc.outputs.files
      }
    }
  }

  private void addGroovyConfig(Project project) {
    project.plugins.withId('groovy') {
      Task groovydocJar = project.tasks.create('groovydocJar', Jar)
      groovydocJar.with {
        classifier = 'groovydoc'
        from project.tasks.groovydoc.outputs.files
      }
    }
  }

  private void addPublishingConfig(Project project) {
    project.plugins.withId('java') {
      project.plugins.apply('maven-publish')
      project.plugins.apply('nebula.maven-resolved-dependencies')

      project.publishing {
        publications {
          main(MavenPublication) {
            from project.components.java
            artifact project.sourcesJar
            artifact project.javadocJar

            project.plugins.withId('groovy') {
              artifact project.groovydocJar
            }
          }
        }
      }
    }
  }

  private void addPluginConfig(Project project) {
    project.plugins.withId('java-gradle-plugin') {
      project.plugins.apply('org.ajoberstar.stutter')
      project.plugins.apply('com.gradle.plugin-publish')

      // remove duplicate publication
      project.gradlePlugin.automatedPublishing = false

      // avoid conflict with localGroovy()
      project.configurations.all {
        exclude group: 'org.codehaus.groovy'
      }

      // use static versions in plugin POMs
      project.pluginBundle {
        withDependencies { List<Dependency> deps ->
          def resolvedDeps = project.configurations.runtimeClasspath.incoming.resolutionResult.allDependencies
          deps.each { Dependency dep ->
            String group = dep.groupId
            String artifact = dep.artifactId
            ResolvedDependencyResult found = resolvedDeps.find { r ->
              (r.requested instanceof ModuleComponentSelector) &&
                  (r.requested.group == group) &&
                  (r.requested.module == artifact)
            }
            if (found) {
              dep.version = found.selected.moduleVersion.version
            }
          }
        }
      }
    }
  }
}
