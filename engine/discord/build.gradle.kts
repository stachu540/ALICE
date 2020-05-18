plugins {
    kotlin("kapt")
    kotlin("plugin.jpa") version Version.kotlin
    kotlin("plugin.allopen") version Version.kotlin
    kotlin("plugin.noarg") version Version.kotlin
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

dependencies {
    implementation(project(":api"))
    api("net.dv8tion:JDA:${Version.discord}")
}