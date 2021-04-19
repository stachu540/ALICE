plugins {
  signing
  application
  `maven-publish`
  kotlin("jvm") version "1.4.32"
  id("org.jetbrains.dokka") version "1.4.30"
  id("com.coditory.manifest") version "0.1.14"
  id("io.gitlab.arturbosch.detekt") version "1.16.0"
  id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
  id("com.github.johnrengelman.shadow") version "6.1.0"
}

allprojects {
  apply(plugin = "io.gitlab.arturbosch.detekt")
  apply(plugin = "org.jlleitschuh.gradle.ktlint")

  repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") // see https://github.com/detekt/detekt/discussions/3458#discussioncomment-350337 -> https://github.com/Kotlin/kotlinx.html/issues/81#issuecomment-806925929
    mavenCentral()
  }

  ktlint {
    debug.set(true)
  }

  detekt {
    ignoreFailures = true
  }

  tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      kotlinOptions {
        jvmTarget = "1.8"
      }
    }

    withType<Test> {
      useJUnitPlatform()
    }
  }
}

subprojects {
  if (File(projectDir, "build.gradle.kts").exists()) {
    apply(plugin = "kotlin")
    apply(plugin = "com.coditory.manifest")
    apply(plugin = "com.github.johnrengelman.shadow")

    base {
      archivesBaseName = artifactId
    }

    tasks {
      withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("shaded")
      }
    }

    pluginManager.withPlugin("maven-publish") {
      apply(plugin = "signing")
      apply(plugin = "org.jetbrains.dokka")

      java {
        withSourcesJar()
        withJavadocJar()
      }

      tasks {
        val defaultManifest = manifest

        withType<Jar> {
          dependsOn(defaultManifest)
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
          signing {
            useInMemoryPgpKeys(project.signingKey.orNull, project.signingPassphrase.orNull)
            sign(maven)
          }
        }
      }

    }

    dependencies {
      testImplementation(rootProject.libs.bundles.test)
      testRuntimeOnly(rootProject.libs.bundles.testRuntime)
    }
  }
}

apply<GitHubPlugin>()

