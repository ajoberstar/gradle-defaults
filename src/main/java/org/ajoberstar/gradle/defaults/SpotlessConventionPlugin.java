package org.ajoberstar.gradle.defaults;

import com.diffplug.gradle.spotless.SpotlessExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class SpotlessConventionPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.getPluginManager().apply("com.diffplug.spotless");
    var spotless = project.getExtensions().getByType(SpotlessExtension.class);

    project.getPluginManager().withPlugin("java-base", plugin -> {
      spotless.java(java -> {
        java.importOrder("java", "javax", "");
        java.eclipse().configFile(project.getRootProject().file("gradle/eclipse-java-formatter.xml"));
      });
    });
  }
}
