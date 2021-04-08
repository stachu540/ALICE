@file:Suppress("UnstableApiUsage")

plugins {
  signing
  `maven-publish`
  kotlin("jvm") version "1.4.32"
  id("org.jetbrains.dokka") version "1.4.30"
  id("com.coditory.manifest") version "0.1.13"
  id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
  id("io.gitlab.arturbosch.detekt") version "1.16.0"
}

allprojects {
  apply(plugin = "io.gitlab.arturbosch.detekt")
  apply(plugin = "org.jlleitschuh.gradle.ktlint")

  repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
  }

  ktlint {
    debug.set(true)
  }

  detekt {
    ignoreFailures = true
  }
}

subprojects {
  apply(plugin = "kotlin")
  apply(plugin = "com.coditory.manifest")

  base {
    archivesBaseName = this@subprojects.project.artifactId
  }

  tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      kotlinOptions {
        jvmTarget = "1.8"
//        moduleName = project.artifactId
      }
    }
  }

  pluginManager.withPlugin("maven-publish") {
    apply(plugin = "org.jetbrains.dokka")

    java {
      withSourcesJar()
      withJavadocJar()
    }

    tasks {
      val defaultManifest = manifest

      withType<Jar> {
        dependsOn(defaultManifest)
        archiveBaseName.set(project.artifactId)
        manifest.from(File(buildDir, "resources/main/META-INF/MANIFEST.MF"))
      }

      named<Jar>("javadocJar") {
        dependsOn(dokkaJavadoc)
        from(dokkaJavadoc.flatMap { it.outputDirectory })
      }

      create<Jar>("kdocJar") {
        group = "build"
        dependsOn(dokkaHtml)
        archiveClassifier.set("kdoc")
        from(dokkaHtml.flatMap { it.outputDirectory })
      }
    }

    publishing {
      val maven by publications.creating(MavenPublication::class) {
        artifactId = base.archivesBaseName
        from(components["java"])
        artifact(tasks.named<Jar>("kdocJar"))
      }

      repositories {
        maven {
          name = "Central"
          setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
          credentials {
            username = System.getenv("OSSRH_USERNAME")
            password = System.getenv("OSSRH_PASSPHRASE")
          }
        }
        maven {
          name = "GitHubPackages"
          setUrl("https://maven.pkg.github.com/stachu540/ALICE")
          credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
          }
        }
      }
      if (!isSnapshot) {
        apply(plugin = "signing")
        signing {
          useInMemoryPgpKeys(project.signingKey.orNull, project.signingPassphrase.orNull)
          sign(maven)
        }
      }
    }
  }

  dependencies {
    api("org.slf4j:slf4j-api:1.7.30")
  }
}

apply<GitHubPlugin>()
