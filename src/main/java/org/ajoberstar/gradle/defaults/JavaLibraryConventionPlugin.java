package org.ajoberstar.gradle.defaults;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;

public class JavaLibraryConventionPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.getPluginManager().apply(LockingConventionPlugin.class);
    project.getPluginManager().apply(MavenCentralConventionPlugin.class);
    project.getPluginManager().apply(SpotlessConventionPlugin.class);
    project.getPluginManager().apply("java-library");

    var publishing = project.getExtensions().getByType(PublishingExtension.class);
    publishing.publications(publications -> {
      publications.create("main", MavenPublication.class, publication -> {
        publication.from(project.getComponents().getByName("java"));
      });
    });
  }
}
