import org.ajoberstar.grgit.Grgit
import java.text.SimpleDateFormat
import java.util.*

plugins {
    application
    `maven-publish`
    kotlin("jvm") version Version.kotlin
    id("com.jfrog.bintray") version Version.bintray
    id("org.jetbrains.dokka") version Version.dokka
    id("org.ajoberstar.grgit") version "4.0.2"
    id("com.gorylenko.gradle-git-properties") version "2.2.2"
}

allprojects {
    repositories {
        jcenter()
    }

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "com.jfrog.bintray")
    apply(plugin = "org.ajoberstar.grgit")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    plugins.withId("application") {
        application.applicationName = artifactId

        tasks.withType<CreateStartScripts> {
            applicationName = project.name
            optsEnvironmentVar = "ALICE_${project.name.toUpperCase()}_OPTS"
            exitEnvironmentVar = "ALICE_${project.name.toUpperCase()}_EXIT_CONSOLE"
        }

        dependencies {
            implementation(clikt)
        }
    }

    plugins.withId("com.gorylenko.gradle-git-properties") {

        val grgit: Grgit by project.extensions

        gitProperties {
            gitPropertiesName = "project.properties"
            dateFormatTimeZone = "GMT"
            keys = emptyList<String>()

            customProperty("name", "Alice")
            customProperty("version", "${project.version}")
            customProperty("commit-id", grgit.head()?.id)
            customProperty("commit-id-abbrev", grgit.head()?.abbreviatedId)
            customProperty("created-at", SimpleDateFormat(dateFormat).apply {
                timeZone = TimeZone.getTimeZone(dateFormatTimeZone)
            }.format(Date()))
        }
    }

    plugins.withId("maven-publish") {
        publishing.publications.withType<MavenPublication> {
            artifactId = project.artifactId
            pom {
                default(displayName)
            }
        }
    }

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions.jvmTarget = "1.8"
        }
        withType<Test> {
            testLogging {
                showStandardStreams = true
            }
            useJUnitPlatform {
                includeEngines("spek2")
            }
        }
    }

    dependencies {
        slf4j
        `kotlin-stdlib`
        `kotlin-reflect`

        testImplementation(logback)
        testImplementation(`junit-jupiter`)
        testImplementation("io.mockk:mockk:1.10.0")
        testImplementation(kotlinx("coroutines-test", Version.coroutines))
        testRuntimeOnly("org.junit.platform:junit-platform-runner:1.6.2")
        testImplementation("org.spekframework.spek2:spek-dsl-jvm:${Version.spek}")
        testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:${Version.spek}")
    }
}

tasks {
    wrapper {
        gradleVersion = "6.5"
        distributionType = Wrapper.DistributionType.ALL
    }

    withType<CreateStartScripts> {
        enabled = false
    }
}