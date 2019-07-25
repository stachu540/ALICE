package ai.alice.commands

import ai.alice.commands.discord.custom.DiscordCustomCommand
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class CustomCommands(
    internal val registry: CommandRegistry
) {
    suspend fun getCollection(): CoroutineCollection<DiscordCustomCommand> {
        if (!registry.database.listCollectionNames().contains("discord_commands")) {
            registry.database.createCollection("discord_commands")
        }

        return registry.database.getCollection<DiscordCustomCommand>("discord_commands")
    }

    suspend fun get(command: String) = getCollection().findOne(DiscordCustomCommand::name eq command)
}