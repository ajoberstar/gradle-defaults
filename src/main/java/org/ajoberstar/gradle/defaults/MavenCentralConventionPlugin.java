package org.ajoberstar.gradle.defaults;

import java.net.URI;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.VariantVersionMappingStrategy;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.plugins.signing.SigningExtension;

public class MavenCentralConventionPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    var extension = project.getExtensions().create("mavenCentral", MavenCentralExtension.class);

    // publishing
    project.getPluginManager().apply("maven-publish");
    var publishing = project.getExtensions().getByType(PublishingExtension.class);
    configureRepositories(project, publishing);
    configurePom(project, publishing, extension);

    // signing
    project.getPluginManager().apply("signing");
    var signing = project.getExtensions().getByType(SigningExtension.class);
    enableSigning(project, signing, publishing);

    // sources/javadoc
    project.getPluginManager().withPlugin("java", plugin -> {
      var java = project.getExtensions().getByType(JavaPluginExtension.class);
      java.withJavadocJar();
      java.withSourcesJar();
    });
  }

  private void configureRepositories(Project project, PublishingExtension publishing) {
    var path = project.getProviders().environmentVariable("BUNDLE_REPO");
    if (path.isPresent()) {
      publishing.repositories(repos -> {
        repos.maven(repo -> {
          repo.setName("Central");
          repo.setUrl(path.map(URI::create));
        });
      });
    }
  }

  private void configurePom(Project project, PublishingExtension publishing, MavenCentralExtension extension) {
    publishing.getPublications().withType(MavenPublication.class, publication -> {
      project.getPluginManager().withPlugin("java-base", plugin -> {
        publication.versionMapping(mapping -> {
          mapping.usage("java-api", variant -> {
            variant.fromResolutionOf("runtimeClasspath");
          });
          mapping.usage("java-runtime", VariantVersionMappingStrategy::fromResolutionResult);
        });
      });

      publication.pom(pom -> {
        pom.getName().set(project.provider(project::getName));
        pom.getDescription().set(project.provider(project::getDescription));
        pom.getUrl().set(extension.mapGitHubUrl("https://github.com/%s/%s"));

        pom.developers(devs -> {
          devs.developer(dev -> {
            dev.getName().set(extension.getDeveloperName());
            dev.getEmail().set(extension.getDeveloperEmail());
          });
        });

        pom.licenses(licenses -> {
          licenses.license(license -> {
            license.getName().set("The Apache Software License, Version 2.0");
            license.getUrl().set("http://www.apache.org/licenses/LICENSE-2.0");
          });
        });

        pom.scm(scm -> {
          scm.getUrl().set(extension.mapGitHubUrl("https://github.com/%s/%s"));
          scm.getConnection().set(extension.mapGitHubUrl("scm:git:git@github.com:%s/%s.git"));
          scm.getDeveloperConnection().set(extension.mapGitHubUrl("scm:git:ssh:git@github.com:%s/%s.git"));
        });
      });
    });
  }

  private void enableSigning(Project project, SigningExtension signing, PublishingExtension publishing) {
    var isCi = project.getProviders().environmentVariable("CI");

    var signingKeyId = project.getProviders().gradleProperty("signingKeyId");
    var signingKey = project.getProviders().gradleProperty("signingKey");
    var signingPassphrase = project.getProviders().gradleProperty("signingPassphrase");

    signing.setRequired(isCi.getOrNull());
    signing.useInMemoryPgpKeys(signingKeyId.getOrNull(), signingKey.getOrNull(), signingPassphrase.getOrNull());
    signing.sign(publishing.getPublications());
  }
}
