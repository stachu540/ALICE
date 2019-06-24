import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project

fun DependencyHandler.core() {
    add("compile", project(":api"))
    add("compile", "ch.qos.logback:logback-classic:${Versions.logback}")

    add("compile", "com.squareup.okhttp3:okhttp:${Versions.okhttp}")
    add("compile", "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}")

    add("compile", "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${Versions.jackson}")
    add("compile", "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Versions.jackson}")
    add("compile", "org.honton.chas.hocon:jackson-dataformat-hocon:1.1.1")

    add("compile", "com.h2database:h2:${Versions.db_h2}")

    add("compile", "com.github.ajalt:clikt:${Versions.clikt}")
    add("compile", "me.liuwj.ktorm:ktorm-jackson:${Versions.ktorm}")
}

fun DependencyHandler.base() {
    add("compile", kotlin("reflect"))
    add("compile", kotlin("stdlib-jdk8"))
    add("compile", "org.slf4j:slf4j-api:${Versions.slf4j}")
    add("compile", "com.google.guava:guava:${Versions.guava}")

    add("compile", "me.liuwj.ktorm:ktorm-core:${Versions.ktorm}")

    add("compile", "com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}")
    add("compile", "com.fasterxml.jackson.core:jackson-annotations:${Versions.jackson}")
    add("compile", "com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    add("compile", "com.fasterxml.jackson.datatype:jackson-datatype-guava:${Versions.jackson}")
    add("compile", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}")
}

fun DependencyHandler.`test-base`() {
    add("testCompile", kotlin("test-junit5"))
    add("testCompile", "org.spekframework.spek2:spek-dsl-jvm:${Versions.spek}")
    add("testRuntimeOnly", "org.spekframework.spek2:spek-runner-junit5:${Versions.spek}")
}

fun DependencyHandler.`test-core`() {
    `test-base`()
}