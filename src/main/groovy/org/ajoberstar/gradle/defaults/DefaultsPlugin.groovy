/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ajoberstar.gradle.defaults

import org.gradle.api.Plugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

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

  private void addReleaseConfig(Project project) {
    project.plugins.apply('org.ajoberstar.reckon')

    project.reckon {
      normal = scopeFromProp()
      preRelease = stageFromProp('milestone', 'rc', 'final')
    }
  }

  private void addSpotless(Project project) {
    project.plugins.apply('com.diffplug.gradle.spotless')
    project.spotless {
      project.plugins.withId('java') {
        java {
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
        target '**/build.gradle'
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
      project.plugins.apply('org.ajoberstar.bintray')

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
    }
  }
}
