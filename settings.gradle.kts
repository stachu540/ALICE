rootProject.name = "alice"

include(
  ":api", ":server",
  ":engine:discord", ":engine:twitch",
  ":modules:basics", ":modules:moderation",
  ":plugin:api", ":plugin:maven", ":plugin:gradle"
)

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
