rootProject.name = "alice"

include(
  ":api", ":server",
  ":engine:discord", ":engine:twitch",
  ":modules:basics", ":modules:moderation"
)

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
