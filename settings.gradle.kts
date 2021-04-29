rootProject.name = "alice"

include(
  ":api", ":server",
  ":engine:discord", ":engine:twitch",
  ":modules:basics", ":modules:moderation",
  ":modules:alertbox", ":modules:youtube"
)

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
