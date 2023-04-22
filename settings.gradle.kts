pluginManagement {
  plugins {
    id("org.ajoberstar.reckon.settings") version "0.17.0"
    id("com.diffplug.spotless") version "6.18.0"
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

plugins {
  id("org.ajoberstar.reckon.settings")
}

extensions.configure<org.ajoberstar.reckon.gradle.ReckonExtension> {
  setDefaultInferredScope("patch")
  stages("beta", "rc", "final")
  setScopeCalc(calcScopeFromProp().or(calcScopeFromCommitMessages()))
  setStageCalc(calcStageFromProp())
}

rootProject.name = "gradle-defaults"
