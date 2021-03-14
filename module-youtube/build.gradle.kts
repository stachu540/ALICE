plugins {
  `maven-publish`
}

dependencies {
  implementation(project(":engine-discord"))
  implementation(project(":engine-twitch"))
}
