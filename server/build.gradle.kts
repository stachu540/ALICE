plugins {
  application
}

application {
  mainClassName = "io.aliceplatform.LauncherKt"
}

apply<PropertiesPlugin>()

projectProperties {
  prefix.set("alice.platform")
  propertiesLocation.set("META-INF/alice/system.properties")
}

dependencies {
  api(projects.api)
  api(libs.bundles.server)
  api(libs.bundles.jackson)
}
