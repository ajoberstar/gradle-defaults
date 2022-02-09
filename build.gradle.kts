plugins {
  id("java-gradle-plugin")
  id("maven-publish")
  id("com.diffplug.spotless")
  id("org.ajoberstar.reckon")
}

group = "org.ajoberstar"

reckon {
  scopeFromProp()
  stageFromProp("beta", "final")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.diffplug.spotless:spotless-plugin-gradle:6.2.1")
}

gradlePlugin {
  plugins {
    create("javaLibraryPlugin") {
      id = "org.ajoberstar.defaults.java-library"
      displayName = "ajoberstar's java-library convention"
      implementationClass = "org.ajoberstar.gradle.defaults.JavaLibraryConventionPlugin"
    }
    create("lockingPlugin") {
      id = "org.ajoberstar.defaults.locking"
      displayName = "ajoberstar's locking convention"
      implementationClass = "org.ajoberstar.gradle.defaults.LockingConventionPlugin"
    }
    create("mavenCentralPlugin") {
      id = "org.ajoberstar.defaults.maven-central"
      displayName = "ajoberstar's Maven Central convention"
      implementationClass = "org.ajoberstar.gradle.defaults.MavenCentralConventionPlugin"
    }
    create("spotlessPlugin") {
      id = "org.ajoberstar.defaults.spotless"
      displayName = "ajoberstar's Spotless convention"
      implementationClass = "org.ajoberstar.gradle.defaults.SpotlessConventionPlugin"
    }
  }
}

publishing {
  repositories {
    maven {
      name = "CentralReleases"
      url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
      credentials {
        username = providers.environmentVariable("OSSRH_USERNAME")
                .forUseAtConfigurationTime().orNull
        password = providers.environmentVariable("OSSRH_PASSWORD")
                .forUseAtConfigurationTime().orNull
      }
    }

    maven {
      name = "CentralSnapshots"
      url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
      credentials {
        username = providers.environmentVariable("OSSRH_USERNAME")
                .forUseAtConfigurationTime().orNull
        password = providers.environmentVariable("OSSRH_PASSWORD")
                .forUseAtConfigurationTime().orNull
      }
    }
  }

  publications {
    withType<MavenPublication> {
      versionMapping {
        usage("java-api") {
          fromResolutionOf("runtimeClasspath")
        }
        usage("java-runtime") {
          fromResolutionResult()
        }
      }

      pom {
        name.set("org.ajoberstar.defaults")
        description.set("ajoberstar's Gradle convention plugins")
        url.set("https://github.com/ajoberstar/gradle-defaults")

        developers {
          developer {
            name.set("Andrew Oberstar")
            email.set("ajoberstar@gmail.com")
          }
        }

        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0")
          }
        }

        scm {
          url.set("https://github.com/ajoberstar/gradle-defaults")
          connection.set("scm:git:git@github.com:ajoberstar/gradle-defaults.git")
          developerConnection.set("scm:git:ssh:git@github.com:ajoberstar/gradle-defaults.git")
        }
      }
    }
  }
}

spotless {
  java {
    importOrder("java", "javax", "");
    eclipse().configFile(rootProject.file("gradle/eclipse-java-formatter.xml"))
  }
}
