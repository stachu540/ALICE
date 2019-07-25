package ai.alice.modules

import ai.alice.commands.CommandRegistry
import com.typesafe.config.Config
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.message.MessageCreateEvent
import io.jooby.Extension
import io.jooby.Jooby
import io.jooby.put
import io.jooby.require
import kotlinx.coroutines.runBlocking

class DiscordEngine : Extension {
    override fun install(application: Jooby) {
        val config = application.environment.config.getConfig("discord")
        val commands = application.require(CommandRegistry::class)
        application.services.put(DiscordClient::class, DiscordClientBuilder(config.getString("token")).apply {

        }.build().apply {
            eventDispatcher.on(MessageCreateEvent::class.java).subscribe {
                runBlocking {
                    commands.handle(it)
                }
            }
        })
    }
}