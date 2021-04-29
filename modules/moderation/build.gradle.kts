plugins {
  `maven-publish`
}

dependencies {
  implementation(projects.engine.discord)
  implementation(projects.engine.twitch)
  implementation(projects.api)
}
