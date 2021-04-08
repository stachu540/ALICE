plugins {
  `maven-publish`
}

dependencies {
  implementation("com.github.ajalt.clikt:clikt:3.1.0")
}

publishing.publications.withType<MavenPublication> {
  pom {
    name.set("Alice API")
    description.set("The API interface for platform.")
  }
}
