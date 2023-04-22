package org.ajoberstar.gradle.defaults;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LockingConventionPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.dependencyLocking(locking -> {
      locking.lockAllConfigurations();
    });

    project.getTasks().register("lock", task -> {
      task.notCompatibleWithConfigurationCache("needs to access all configurations");
      task.doLast(t -> {
        assert project.getGradle().getStartParameter().isWriteDependencyLocks();

        project.getConfigurations().all(configuration -> {
          if (configuration.isCanBeResolved()) {
            configuration.resolve();
          }
        });
      });
    });
  }
}
