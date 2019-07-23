package ai.alice.engine.discord

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import org.apache.commons.text.StringSubstitutor
import java.awt.Color
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class DiscordCustomCommandSpec(
    val name: String,
    val message: MessageSpec,
    val count: Int = 0
) {
    fun handle(event: MessageCreateEvent, options: Map<String, String?>, arguments: List<String>) {
        val map = mutableMapOf(
            "count" to count.toString(),
            "time" to Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        ).apply {
            putAll(arguments.mapIndexed(::Pair).toMap().mapKeys { it.key.toString() })
        }
    }
}

data class MessageSpec(
    var content: String?,
    var embed: EmbedSpec?,
    var tts: Boolean = false
)

data class EmbedSpec(
    val fields: Collection<Field>,
    val author: Author?,
    val thumbnail: String?,
    val image: String?,
    val footer: Footer?,
    val color: Color?,
    val timestamp: Instant?,
    val url: String?,
    val description: String?,
    val title: String?
) {
    data class Field(
        val name: String,
        val value: String,
        val inline: Boolean = false
    )

    data class Author(
        val name: String,
        val url: String? = null,
        val iconUrl: String? = null
    )

    data class Footer(
        val text: String,
        val iconUrl: String? = null
    )

    fun toSpec(map: Map<String, String>): EmbedCreateSpec.() -> Unit = {
        if (title != null) {
            setTitle(title.replacing(map))
        }

        if (description != null) {
            setDescription(description.replacing(map))
        }

        if (url != null) {
            setUrl(url.replacing(map))
        }

        if (timestamp != null) {
            setTimestamp(timestamp)
        }

        if (color != null) {
            setColor(color)
        }

        if (footer != null) {
            setFooter(footer.text.replacing(map), footer.iconUrl?.replacing(map))
        }

        if (image != null) {
            setImage(image.replacing(map))
        }

        if (thumbnail != null) {
            setThumbnail(thumbnail.replacing(map))
        }

        if (author != null) {
            setAuthor(author.name.replacing(map), author.url?.replacing(map), author.iconUrl?.replacing(map))
        }

        if (fields.isNotEmpty()) {
            fields.forEach {
                addField(it.name.replacing(map), it.value.replacing(map), it.inline)
            }
        }
    }
}

fun String.replacing(map: Map<String, String>): String = StringSubstitutor(map, "{", "}").replace(this)

fun Throwable.formMessage(): (MessageCreateSpec) -> Unit = {
    it.setEmbed {
        it.setTitle(this::class.java.simpleName)

        if (message != null) {
            it.setDescription(message!!)
        }

        val cause = cause
        while (cause != null) {
            it.addField(this::class.java.simpleName, cause.message ?: "", false)
        }

        it.setColor(Color.getColor("#D8000C"))
    }
}