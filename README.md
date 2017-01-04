# gradle-defaults

**DISCLAIMER:** This project tracks _my_ own preferences for Gradle defaults. It will follow SemVer for breaking changes (prior to 1.0.0, minors may include breaking changes).

Plugin providing opinionated defaults for Gradle builds.

[![Bintray](https://api.bintray.com/packages/ajoberstar/maven/gradle-defaults/images/download.svg)](https://bintray.com/ajoberstar/maven/gradle-defaults/_latestVersion)
[![Travis](https://img.shields.io/travis/ajoberstar/gradle-defaults.svg?style=flat-square)](https://travis-ci.org/ajoberstar/gradle-defaults)
[![Quality Gate](https://sonarqube.ajoberstar.com/api/badges/gate?key=org.ajoberstar:gradle-defaults)](https://sonarqube.ajoberstar.com/dashboard/index/org.ajoberstar:gradle-defaults)
[![GitHub license](https://img.shields.io/github/license/ajoberstar/gradle-defaults.svg?style=flat-square)](https://github.com/ajoberstar/gradle-defaults/blob/master/LICENSE)

## What does it do?

It's optimized for two use cases:

* A Java library that will be published to Bintray
* A Gradle plugin that will be published to Bintray and the plugin portal

## Usage

In the root project apply the defaults plugin:

```groovy
plugins {
  id 'org.ajoberstar.defaults' version '<version>'
}
```

## Details

What happens when the following plugins are applied:

### Always

In the root project:

- `org.ajoberstar.grgit` applied
- `org.ajoberstar.git-publish` applied
  - Publishes to `gh-pages` branch
  - Publishes content from:
    - `src/gh-pages` to `/`
    - `javadoc` task to `/docs/<taskpath>` from all projects
    - `groovydoc` task to `/docs/<taskpath>` from all projects
- `org.sonarqube` applied
  - `sonar.projectVersion` set to the targeted final version (i.e. strips off `-<whatever>` from the version)
- `org.ajoberstar.semver-vcs-grgit` applied
- `org.ajoberstar.release-experimental` applied
  - `release` depends on `clean`, `build`, `gitPublishPush`, `publish`, and `publishPlugins` (when available) from all projects

In all projects:

- `com.diffplug.spotless` applied
  - Expects a license header in `<root project>/gradle/HEADER`. Will be applied to Java and Groovy files.
  - Java code formatted using Google Code Style
  - Groovy code and Gradle files formatted for 2 space indents, trailing space removed, and newlines at the end of files.
- ordering rules:
  - `clean` runs before everything else
  - all `publishing` tasks run after `build`

### java

In all projects (where applied):

- `jacoco` applied
- `sourcesJar` task added
- `javadocJar` task added
- `maven-publish` applied
  - a `main` publication is added including `jar`, `sourcesJar`, `javadocJar`, and `groovydocJar`
- `org.ajoberstar.bintray` applied **(requires manual config)**

### groovy

In all projects (where applied):

- `groovydocJar` task added

### java-gradle-plugin

In all projects (where applied):

- `org.ajoberstar.stutter` applied **(requires manual config)**
- `com.gradle.plugin-publish` applied **(requires manual config)**
- The default `pluginMaven` publication added by `java-gradle-plugin` is disabled
- All `org.codehaus.groovy` dependencies are excluded from all configurations to avoid conflicts with the `localGroovy()` dependency
