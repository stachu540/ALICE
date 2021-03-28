plugins {
  signing
  `maven-publish`
  kotlin("jvm") version "1.4.32"
  id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

apply<GitHubPlugin>()

allprojects {
  apply(plugin = "org.jlleitschuh.gradle.ktlint")
  repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
  }

  pluginManager.withPlugin("maven-publish") {
    publishing.publications.withType<MavenPublication> {

    }
  }

  ktlint {
    debug.set(true)
  }

  tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      kotlinOptions.jvmTarget = "1.8"
    }
  }
}

subprojects {
  apply(plugin = "kotlin")

  pluginManager.withPlugin("maven-publish") {
    apply(plugin = "signing")

    java {
      withSourcesJar()
      withJavadocJar()
    }

    val maven by publishing.publications.creating(MavenPublication::class) {
      artifactId = project.artifactId
      from(components["java"])
    }

    signing {
      sign(maven)
    }
  }

  dependencies {

  }
}

tasks {
  wrapper {
    distributionType = Wrapper.DistributionType.ALL
  }
}
