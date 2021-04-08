plugins {
  `kotlin-dsl`
}

repositories {
  mavenLocal()
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation("org.kohsuke:github-api:1.123")
  implementation("com.gradle.publish:plugin-publish-plugin:0.14.0")
  implementation("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")
}
