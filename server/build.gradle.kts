plugins {
    application
    kotlin("kapt")
    kotlin("plugin.jpa") version Version.kotlin
    kotlin("plugin.allopen") version Version.kotlin
    kotlin("plugin.noarg") version Version.kotlin
}

application {
    mainClassName = "ai.alice.LightPriestKt"
    applicationDefaultJvmArgs = listOf()
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

dependencies {
    `kotlin-test`
    `kotlin-test-junit5`
    api(project(":api"))

    kapt("com.google.auto.service:auto-service:1.0-rc6")
    implementation("com.google.auto.service:auto-service:1.0-rc6")

    implementation(enforcedPlatform("com.fasterxml.jackson:jackson-bom:${Version.jackson}"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("org.hibernate:hibernate-core:${Version.hibernanate}")

    // class drivers
    implementation(mysql)
    implementation(postgres)
    implementation(sqlite)
    implementation(sqlserver)
    implementation(h2)
}