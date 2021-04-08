plugins {
  `kotlin-dsl`
  `maven-publish`
  id("com.gradle.plugin-publish")
}

gradlePlugin {
  plugins {
    create("engine") {
      id = "${group}.engine"
      displayName = "Alice Engine Plugin"
      description = "Setup your chat engine using this plugin"
      implementationClass = "ai.alice.plugin.gradle.AliceEnginePlugin"
    }
    create("module") {
      id = "${group}.module"
      displayName = "Alice Module Plugin"
      description = "Setup your module for instance"
      implementationClass = "ai.alice.plugin.gradle.AliceModulePlugin"
    }
  }
}

pluginBundle {
  (plugins) {
    "engine" {
      displayName = "Alice Engine Plugin"
      description = "Setup your chat engine using this plugin"
    }
    "module" {
      displayName = "Alice Module Plugin"
      description = "Setup your module for instance"
    }
  }
}

tasks {
  publishPlugins {
    doFirst {
      val key = System.getenv("GRADLE_PUBLISH_KEY")
      val secret = System.getenv("GRADLE_PUBLISH_SECRET")

      if (key == null || secret == null) {
        throw GradleException("gradlePublishKey and/or gradlePublishSecret are not defined environment variables")
      }

      System.setProperty("gradle.publish.key", key)
      System.setProperty("gradle.publish.secret", secret)
    }
  }
}

dependencies {
  api(project(":plugin:api"))
}

publishing.publications.withType<MavenPublication> {
  if (name == "maven") {
    pom {
      name.set("Alice Gradle Plugin Package")
      description.set("Package provides helpers to building implementations for modules and/or chat engines.")
    }
  }
}
