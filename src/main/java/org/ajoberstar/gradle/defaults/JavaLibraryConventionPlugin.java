package org.ajoberstar.gradle.defaults;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JavaLibraryConventionPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.getPluginManager().apply(LockingConventionPlugin.class);
    project.getPluginManager().apply(MavenCentralConventionPlugin.class);
    project.getPluginManager().apply(SpotlessConventionPlugin.class);
  }
}
