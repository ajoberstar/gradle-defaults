package org.ajoberstar.gradle.defaults;

import org.ajoberstar.gradle.defaults.tasks.LockTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LockingConventionPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.dependencyLocking(locking -> {
      locking.lockAllConfigurations();
    });

    project.getTasks().register("lock", LockTask.class, task -> {
      task.getIsWriteLocks().set(project.getGradle().getStartParameter().isWriteDependencyLocks());
      task.getConfigurations().set(project.getConfigurations().matching(conf -> conf.isCanBeResolved()));
    });
  }
}
