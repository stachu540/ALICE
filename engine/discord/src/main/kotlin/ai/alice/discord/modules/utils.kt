package ai.alice.discord.modules

import ai.alice.api.engine.command.group
import ai.alice.discord.await
import ai.alice.discord.commands.DiscordCommandEvent

object Groups {
    val GENERAL = group<DiscordCommandEvent>("general", "General")
    val DEBUG = group<DiscordCommandEvent>("debug", "Debug") { event ->
        event.raw.jda.retrieveApplicationInfo().map { it.owner == event.user }.await()
    }
}