plugins {
  `maven-publish`
}

dependencies {
  api(project(":plugin:api"))
  implementation("org.apache.maven:maven-plugin-api:3.3.9")
  implementation("org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.0")
  implementation("org.apache.maven:maven-core:3.8.1")
}
