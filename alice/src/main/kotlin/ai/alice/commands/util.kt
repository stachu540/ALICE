package ai.alice.commands

import discord4j.core.spec.MessageCreateSpec
import org.apache.commons.text.StringSubstitutor
import java.awt.Color

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

fun String.replacing(map: Map<String, String>): String = StringSubstitutor(map, "{", "}").replace(this)