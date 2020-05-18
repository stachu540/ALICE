plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.3.71"
}

repositories {
    jcenter()
}

dependencies {
    gradleApi()
    implementation(kotlin("stdlib-jdk8"))
}