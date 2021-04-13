apply<PropertiesPlugin>()

dependencies {
  api(platform("com.fasterxml.jackson:jackson-bom:2.12.3"))

  api(project(":api"))
  implementation(project(":engine:discord"))
  implementation(project(":engine:twitch"))

  api("com.fasterxml.jackson.core:jackson-databind")
  api("com.fasterxml.jackson.module:jackson-module-kotlin")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
}

projectProperties {
  prefix.set("alice.platform")
  propertiesLocation.set("META-INF/system.properties")
}
