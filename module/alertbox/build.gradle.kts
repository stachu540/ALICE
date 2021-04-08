plugins {
  `maven-publish`
}

dependencies {
  implementation(project(":engine:twitch"))
  implementation(project(":api"))
}
