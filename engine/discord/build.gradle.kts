plugins {
  `maven-publish`
}

dependencies {
  implementation(projects.api)
  api(libs.discord)
}
