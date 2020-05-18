import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.kotlin

fun DependencyHandler.springBoot(artifact: String, version: String? = null) =
    "org.springframework.boot:spring-boot-$artifact${if (version != null && version.isNotBlank()) ":$version" else ""}"

fun DependencyHandler.kotlinx(artifact: String, version: String? = null) =
    "org.jetbrains.kotlinx:kotlinx-$artifact${if (version != null && version.isNotBlank()) ":$version" else ""}"

val DependencyHandler.clikt
    get() = "com.github.ajalt:clikt:${Version.clikt}"

val DependencyHandler.`kotlin-stdlib`
    get() = add("api", kotlin("stdlib-jdk8"))

val DependencyHandler.`kotlin-reflect`
    get() = add("api", kotlin("reflect"))
val DependencyHandler.`kotlin-test`
    get() = add("testImplementation", kotlin("test"))
val DependencyHandler.`kotlin-test-junit5`
    get() = add("testImplementation", kotlin("test-junit5"))

val DependencyHandler.slf4j
    get() = add("api", "org.slf4j:slf4j-api:${Version.slf4j}")

val DependencyHandler.`junit-jupiter`
    get() = "org.junit.jupiter:junit-jupiter:${Version.junit5}"

val DependencyHandler.config
    get() = "com.typesafe:config:${Version.config}"

val DependencyHandler.tornadofx
    get() = add("implementation", "no.tornado:tornadofx:${Version.tornado}")

val DependencyHandler.logback: String
    get() = "ch.qos.logback:logback-classic:${Version.logback}"

val DependencyHandler.discord
    get() = "net.dv8tion:JDA:${Version.discord}"

// JDBC drivers

val DependencyHandler.mysql
    get() = "mysql:mysql-connector-java:${Version.mysql}"
val DependencyHandler.postgres
    get() = "org.postgresql:postgresql:${Version.postgres}"
val DependencyHandler.sqlite
    get() = "org.xerial:sqlite-jdbc:${Version.sqlite}"
val DependencyHandler.sqlserver
    get() = "com.microsoft.sqlserver:mssql-jdbc:${Version.sqlserver}"
val DependencyHandler.h2
    get() = "com.h2database:h2:${Version.h2}"