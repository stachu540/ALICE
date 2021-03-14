dependencies {
  api(project(":api"))
  implementation(project(":engine-discord"))
  implementation(project(":engine-twitch"))
  api(platform("com.fasterxml.jackson:jackson-bom:2.12.2"))
  api("com.fasterxml.jackson.core:jackson-databind")
  api("com.fasterxml.jackson.module:jackson-module-kotlin")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")

}
