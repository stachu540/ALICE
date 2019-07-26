package ai.alice

import ai.alice.commands.CommandRegistry
import ai.alice.modules.CommandRegistryModule
import ai.alice.modules.DiscordEngine
import ai.alice.modules.MongoModule
import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import io.jooby.json.JacksonModule
import io.jooby.require
import io.jooby.runApp
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineClient

fun main(args: Array<String>) {
    runApp(args) {
        install(JacksonModule())
        install(MongoModule())
        install(CommandRegistryModule())
        install(DiscordEngine())

        onStarting {
            runBlocking {
                log.info("Starting A.L.I.C.E")
                require(DiscordClient::class).login().subscribe()
            }
        }

        onStop {
            log.info("Stopping A.L.I.C.E Instance")
            require(CoroutineClient::class).close()
            runBlocking {
                require(DiscordClient::class).logout().subscribe()
            }
        }
    }
}