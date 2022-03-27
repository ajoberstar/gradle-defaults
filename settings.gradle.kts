pluginManagement {
  plugins {
    id("org.ajoberstar.reckon") version "0.16.1"
    id("com.diffplug.spotless") version "6.3.0"
  }

  repositories {
    mavenCentral()
  }

  resolutionStrategy {
    eachPlugin {
      if (target.id.id == "com.diffplug.spotless") {
        useModule("com.diffplug.spotless:spotless-plugin-gradle:${target.version}")
      }
    }
  }
}

rootProject.name = "gradle-defaults"
