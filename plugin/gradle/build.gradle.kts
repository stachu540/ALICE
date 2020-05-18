import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.11.0"
    id("com.gorylenko.gradle-git-properties")
}

dependencies {
    gradleTestKit()

    `kotlin-test`
    `kotlin-test-junit5`
    api(project(":descriptor"))
    api("com.typesafe:config:${Version.config}")
}

gradlePlugin {
    plugins {
        create("alice") {
            id = "ai.alice"
            displayName = "Alice Gradle Plugin"
            description = "Creates resolver to the all plugins in one place"
            implementationClass = "ai.alice.plugin.gradle.AliceGradlePlugin"
        }
        create("aliceEngine") {
            id = "ai.alice.engine"
            displayName = "Alice Engine Plugin"
            description = "Create a single instance engine resolver."
            implementationClass = "ai.alice.plugin.gradle.AliceEnginePlugin"
        }
        create("aliceModule") {
            id = "ai.alice.module"
            displayName = "Alice Module Plugin"
            description = "Creates a module resolver with required engine."
            implementationClass = "ai.alice.plugin.gradle.module.AliceModulePlugin"
        }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
        }
    }
}