# gradle-defaults

Convention plugins for use in my projects.

![](https://github.com/ajoberstar/gradle-defaults/workflows/.github/workflows/ci.yaml/badge.svg)

## What does it do?

This project tracks _my own_ preferences for Gradle defaults. It will follow SemVer for breaking changes (prior to 1.0.0, minors may include breaking changes).

**DISCLAIMER** This is probably not useful for anyone but me.

## Usage

In the root project apply the defaults plugin:

```groovy
plugins {
  id 'org.ajoberstar.defaults.java-library' version '<version>'
  id 'org.ajoberstar.defaults.locking' version '<version>'
  id 'org.ajoberstar.defaults.maven-central' version '<version>'
  id 'org.ajoberstar.defaults.spotless' version '<version>'
}
```
