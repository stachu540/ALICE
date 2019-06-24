import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    versions
    `kotlin-jvm`
    `kotlin-jpa`
}

allprojects {
    repositories {
        jcenter()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-jpa")

    dependencies {
        implementation(kotlin("reflect"))
        implementation(kotlin("stdlib-jdk8"))
    }

    tasks {
        withType<KotlinCompile> {
            incremental = true
            kotlinOptions.jvmTarget = "1.8"
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "5.4.1"
    distributionType = Wrapper.DistributionType.ALL
}