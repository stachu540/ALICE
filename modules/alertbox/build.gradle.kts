plugins {
  `maven-publish`
}

dependencies {
  implementation(projects.engine.twitch)
  implementation(projects.api)
}
