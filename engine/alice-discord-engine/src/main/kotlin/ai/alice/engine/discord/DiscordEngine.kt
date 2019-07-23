package ai.alice.engine.discord

import ai.alice.api.Alice
import ai.alice.api.command.CommandRegistry
import ai.alice.api.config.AliceConfigSpec
import ai.alice.api.service.IEngine
import com.uchuhimo.konf.Config
import discord4j.core.DiscordClient
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.reactive.awaitSingle

class DiscordEngine(override val root: Alice, override val client: DiscordClient) : IEngine<DiscordClient, Event> {
    override val config: Config
        get() = root.config.withPrefix("alice.engine.discord")
    override val commands: CommandRegistry<IEngine<DiscordClient, Event>, MessageCreateEvent> = DiscordCommandRegistry(this)

    override val isRunning: Boolean
        get() = client.isConnected

    override fun <R : Event> doOn(event: Class<R>, consumer: R.() -> Unit) {
        client.eventDispatcher.on(event).subscribe(consumer)
    }

    override suspend fun start() {
        client.login().awaitSingle()
    }

    override suspend fun stop(force: Boolean) {
        client.logout().awaitSingle()
    }
}
