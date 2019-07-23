package ai.alice.engine.discord

import ai.alice.api.command.CustomCommandRegistry
import ai.alice.api.service.IEngine
import discord4j.core.DiscordClient
import discord4j.core.event.domain.Event
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class DiscordCustomCommandRegistry(override val engine: DiscordEngine) :
    CustomCommandRegistry<IEngine<DiscordClient, Event>, MessageSpec> {

    val commands: Collection<DiscordCustomCommandSpec>
        get() = runBlocking {
            engine.root.datastore.getCollection<DiscordCustomCommandSpec>("discord_commands")
                .listIndexes<DiscordCustomCommandSpec>().toList()
        }

    override suspend fun rename(old: String, new: String) {
        collection.updateOne(
            DiscordCustomCommandSpec::name eq old.toLowerCase(),
            setValue(DiscordCustomCommandSpec::name, new.toLowerCase())
        )
    }

    suspend fun increment(command: String) {
        get(command)?.let {
            setCount(command, it.count + 1)
        }
    }

    suspend fun decrement(command: String) {
        get(command)?.let {
            setCount(command, it.count - 1)
        }
    }

    suspend fun reset(command: String) = setCount(command, 0)

    suspend fun setCount(command: String, count: Int) {
        collection.updateOne(
            DiscordCustomCommandSpec::name eq command.toLowerCase(),
            setValue(DiscordCustomCommandSpec::count, count)
        )
    }

    override suspend fun register(command: String, component: MessageSpec) {
        if (exist(command)) {
            collection.updateOne(
                DiscordCustomCommandSpec::name eq command.toLowerCase(),
                setValue(DiscordCustomCommandSpec::message, component)
            )
        } else {
            collection.insertOne(DiscordCustomCommandSpec(command.toLowerCase(), component))
        }
    }

    override suspend fun unregister(command: String) {
        collection.deleteOne(DiscordCustomCommandSpec::name eq command.toLowerCase())
    }

    suspend fun get(command: String) =
        collection.findOne(DiscordCustomCommandSpec::name eq command.toLowerCase())

    suspend fun exist(command: String) = get(command) != null

    private val collection
        get() = engine.root.datastore.getCollection<DiscordCustomCommandSpec>("discord_commands")
}
