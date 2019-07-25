package ai.alice.commands.discord.custom

import ai.alice.api.CommandOptions
import ai.alice.commands.CustomCommand
import ai.alice.commands.discord.AccessLevel
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.reactive.awaitSingle
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class DiscordCustomCommand(
    override val count: Int,
    override val name: String,
    override val alias: Collection<String>,
    override val access: AccessLevel,
    val message: MessageSpec
) : CustomCommand<MessageCreateEvent> {
    override val description: String? = null
    override val usage: String? = null

    override suspend fun execute(event: MessageCreateEvent, options: CommandOptions) {
        val map = mutableMapOf(
            "count" to count.toString(),
            "time" to Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        ).apply {
            putAll(options.toMap())
        }
        event.message.channel.flatMap { it.createMessage(message.toSpec(map)) }.awaitSingle()
    }
}