rootProject.name = "alice"

include(
    ":api",
    ":server",
    ":client",
    ":debug",

    ":engine:discord",
    ":engine:twitch",
//    ":engine:mixer",
//    ":engine:youtube-gaming",
//    ":engine:teamspeak",
//    ":engine:slack",

    ":descriptor",
    ":plugin:gradle",
    ":plugin:maven"
)