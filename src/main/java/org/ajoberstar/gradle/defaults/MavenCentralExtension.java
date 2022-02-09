package org.ajoberstar.gradle.defaults;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

public class MavenCentralExtension {
  private final Property<String> developerName;
  private final Property<String> developerEmail;
  private final Property<String> githubOwner;
  private final Property<String> githubRepository;

  @Inject
  public MavenCentralExtension(ObjectFactory objectFactory) {
    this.developerName = objectFactory.property(String.class);
    this.developerEmail = objectFactory.property(String.class);
    this.githubOwner = objectFactory.property(String.class);
    this.githubRepository = objectFactory.property(String.class);
  }

  public Property<String> getDeveloperName() {
    return developerName;
  }

  public Property<String> getDeveloperEmail() {
    return developerEmail;
  }

  public Property<String> getGithubOwner() {
    return githubOwner;
  }

  public Property<String> getGithubRepository() {
    return githubRepository;
  }

  Provider<String> mapGitHubUrl(String format) {
    return githubOwner.flatMap(owner -> githubRepository.map(repo -> String.format(format, owner, repo)));
  }
}
