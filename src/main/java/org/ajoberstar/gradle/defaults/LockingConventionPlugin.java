package org.ajoberstar.gradle.defaults;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

public class LockingConventionPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.getPluginManager().apply("java-base");
    var sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

    // enable locking on each source sets compileClasspath and runtimeClasspath
    sourceSets.configureEach(sourceSet -> {
      project.getConfigurations().named(sourceSet.getCompileClasspathConfigurationName(), conf -> {
        conf.getResolutionStrategy().activateDependencyLocking();
      });
      project.getConfigurations().named(sourceSet.getRuntimeClasspathConfigurationName(), conf -> {
        conf.getResolutionStrategy().activateDependencyLocking();
      });
    });

    // task to resolve each source sets compileClasspath and runtimeClasspath
    project.getTasks().register("lock", task -> {
      task.doFirst(t -> {
        assert project.getGradle().getStartParameter().isWriteDependencyLocks();

        sourceSets.configureEach(sourceSet -> {
          project.getConfigurations().named(sourceSet.getCompileClasspathConfigurationName(), conf -> {
            conf.resolve();
          });
          project.getConfigurations().named(sourceSet.getRuntimeClasspathConfigurationName(), conf -> {
            conf.resolve();
          });
        });
      });
    });
  }
}
