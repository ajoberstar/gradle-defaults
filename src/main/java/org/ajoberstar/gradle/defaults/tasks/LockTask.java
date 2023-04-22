package org.ajoberstar.gradle.defaults.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public abstract class LockTask extends DefaultTask {
  @Input
  public abstract Property<Boolean> getIsWriteLocks();

  @Input
  public abstract SetProperty<Configuration> getConfigurations();

  @TaskAction
  public void lock() {
    assert getIsWriteLocks().getOrElse(false);

    var configurations = getConfigurations().get();
    configurations.forEach(conf -> {
      if (conf.isCanBeResolved()) {
        conf.resolve();
      }
    });
  }
}
