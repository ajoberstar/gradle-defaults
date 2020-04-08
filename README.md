# gradle-defaults

Plugin providing my opinionated defaults for Gradle builds.

[![Download](https://api.bintray.com/packages/ajoberstar/maven/gradle-defaults/images/download.svg)](https://bintray.com/ajoberstar/maven/gradle-defaults/_latestVersion)
![](https://github.com/ajoberstar/gradle-defaults/workflows/.github/workflows/build.yaml/badge.svg)

## What does it do?

This project tracks _my own_ preferences for Gradle defaults. It will follow SemVer for breaking changes (prior to 1.0.0, minors may include breaking changes).

**DISCLAIMER** This is probably not useful for anyone but me.

## Usage

In the root project apply the defaults plugin:

```groovy
plugins {
  id 'org.ajoberstar.defaults' version '<version>'
}
```

## Details

View the [DefaultsPlugin](https://github.com/ajoberstar/gradle-defaults/blob/master/src/main/groovy/org/ajoberstar/gradle/defaults/DefaultsPlugin.groovy) source for what is configured.
