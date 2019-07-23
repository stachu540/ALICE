package ai.alice.engine.discord

import ai.alice.api.command.CommandRegistry
import ai.alice.api.command.ICommand
import ai.alice.api.config.AliceConfigSpec
import ai.alice.api.service.IEngine
import discord4j.core.DiscordClient
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking

class DiscordCommandRegistry(override val engine: DiscordEngine) :
    CommandRegistry<IEngine<DiscordClient, Event>, MessageCreateEvent> {

    private val registry: MutableSet<ICommand<MessageCreateEvent>> = mutableSetOf()

    override val custom: DiscordCustomCommandRegistry = DiscordCustomCommandRegistry(engine)

    override fun register(command: ICommand<MessageCreateEvent>) {
        registry.add(command)
    }

    override fun unregister(command: String) {
        registry.removeIf { it.name == command }
    }

    internal suspend fun handle(event: MessageCreateEvent) {
        val mentioned = event.message.userMentions.collectList().awaitSingle()
            .firstOrNull { engine.client.applicationInfo.awaitSingle().id == it.id }
        val prefix = engine.config[AliceConfigSpec.EngineSpec.DiscordConfigSpec.botPrefix]
        event.message.content.ifPresent {
            val args = if (mentioned != null && it.startsWith(mentioned.mention)) {
                it.replace("${mentioned.mention} ", "").split(' ').toList()
            } else if (it.startsWith(prefix)) {
                it.substring(prefix.length).split(' ').toList()
            } else listOf()

            if (args.isNotEmpty()) {
                val command = args[0]
                val options = args.filter { it.startsWith("--") }.map {
                    it.split('=').let {
                        val k = it.first()
                        val v = it.last()
                        Pair(k, if (v == k) null else v)
                    }
                }.toMap()
                val arguments = args.filter { !it.startsWith("--") }

                runBlocking {
                    if (custom.exist(command.toLowerCase())) {
                        custom.get(command.toLowerCase())?.handle(event, options, arguments)
                    } else {
                        registry.firstOrNull { it.name.toLowerCase() == command.toLowerCase() }?.let {  }
                    }
                }
            }
        }
    }
}