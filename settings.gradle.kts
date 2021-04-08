rootProject.name = "alice"

include(
  ":server", ":api",
  ":engine:discord", ":engine:twitch",
  ":module:youtube", ":module:alertbox",
  ":plugin:gradle", ":plugin:maven", ":plugin:api"
)
