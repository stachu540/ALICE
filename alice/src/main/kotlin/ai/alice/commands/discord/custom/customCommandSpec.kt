package ai.alice.commands.discord.custom

import ai.alice.commands.replacing
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import java.awt.Color
import java.time.Instant


data class MessageSpec(
    var content: String?,
    var embed: EmbedSpec?,
    var tts: Boolean = false
) {
    fun toSpec(map: Map<String, String>) : (MessageCreateSpec) -> Unit = {
        if (content != null) {
            it.setContent(content!!.replacing(map))
        }

        if (embed != null) {
            it.setEmbed(embed!!.toSpec(map))
        }

        if (tts) {
            it.setTts(tts)
        }
    }
}

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