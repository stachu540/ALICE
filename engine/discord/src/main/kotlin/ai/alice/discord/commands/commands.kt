package ai.alice.discord.commands

import ai.alice.api.engine.command.*
import ai.alice.api.provider.provide
import ai.alice.discord.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.awt.Color
import java.time.Instant

class DiscordCommandEvent(
    override val raw: MessageReceivedEvent,
    override val prefix: String
) : CommandEvent<MessageReceivedEvent, Message> {
    override val message: Message
        get() = raw.message
    override val rawMessage: String
        get() = message.contentRaw
    override val args: Array<String>
        get() = rawMessage.trim().split(" ")
            .drop(1).toTypedArray()
    override val command: String
        get() = rawMessage.trim().split(" ").first()
            .substring(prefix.length).trim().toLowerCase()

    val channel
        get() = raw.channel

    val user
        get() = raw.author

    val author
        get() = provide(raw.member)

    suspend fun respond(message: MessageBuilder.() -> Unit): Message =
        channel.sendMessage(MessageBuilder().apply(message).build()).await()

    override suspend fun respond(message: String): Message =
        channel.sendMessage(message).await()

    suspend fun respondEmbed(message: EmbedBuilder.() -> Unit): Message =
        channel.sendMessage(EmbedBuilder().apply(message).build()).await()

    suspend fun dm(message: MessageBuilder.() -> Unit): Message =
        user.openPrivateChannel().flatMap {
            it.sendMessage(MessageBuilder().apply(message).build())
        }.await()

    override suspend fun dm(message: String): Message =
        user.openPrivateChannel().flatMap {
            it.sendMessage(message)
        }.await()

    suspend fun dmEmbed(message: EmbedBuilder.() -> Unit): Message =
        user.openPrivateChannel().flatMap {
            it.sendMessage(EmbedBuilder().apply(message).build())
        }.await()
}

class DiscordCommandProvider(
    defaultPrefix: String,
    prefixHandler: (MessageReceivedEvent) -> String?
) : AbstractCommandProvider<MessageReceivedEvent, DiscordCommandEvent>(PrefixHandler(defaultPrefix, prefixHandler)) {
    override suspend fun convert(event: MessageReceivedEvent): DiscordCommandEvent? =
        prefix[event].let {
            if (event.message.contentRaw.startsWith(it)) DiscordCommandEvent(event, it) else null
        }
}

class PrefixHandler(
    override val default: String,
    private val prefixHandler: (MessageReceivedEvent) -> String?
) : Prefix<MessageReceivedEvent> {
    override fun get(event: MessageReceivedEvent): String =
        prefixHandler(event) ?: default
}

class DiscordCommand(
    names: List<String>,
    description: String?,
    access: CommandAccess<DiscordCommandEvent>,
    group: CommandGroup<DiscordCommandEvent>,
    private val exec: suspend Command<DiscordCommandEvent>.(DiscordCommandEvent) -> Unit
) : Command<DiscordCommandEvent>(names.first(), names.drop(1).toTypedArray(), description, access, group) {
    override suspend fun execute(event: DiscordCommandEvent) {
        exec(this, event)
    }

    override suspend fun DiscordCommandEvent.message(message: String?, isError: Boolean) {
        respondEmbed {
            setDescription(message)
            setTimestamp(Instant.now())
            setColor(if (isError) Color.RED else Color.ORANGE)
        }
    }

    override suspend fun DiscordCommandEvent.error(exception: Exception) {
        respondEmbed {
            setColor(Color.RED)
            setTimestamp(Instant.now())
            setTitle("${exception.javaClass.simpleName}: ${exception.message}")
            var cause = exception.cause
            while (cause != null) {
                addField(cause.javaClass.simpleName, cause.message, false)
                cause = cause.cause
            }
        }
    }

    override suspend fun DiscordCommandEvent.usage() {
        respondEmbed {
            setColor(Color.ORANGE)
            setTimestamp(Instant.now())
            setTitle("$prefix${name}")
            setDescription(description)
            setFooter("Group: ${group.displayName}")
            if (alias.isNotEmpty()) addField("Aliases", alias.joinToString { "$prefix$it" }, false)
        }
    }
}