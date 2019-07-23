package ai.alice.internal.commands.discord

import ai.alice.api.Alice
import ai.alice.api.RootComponent
import ai.alice.api.command.AbstractCommand
import ai.alice.api.config.AliceConfigSpec
import ai.alice.engine.discord.formMessage
import ai.alice.internal.SystemOperator.execute
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.reactive.awaitSingle

class SystemCommand(override val root: Alice) : AbstractCommand<MessageCreateEvent>(
    root.config[AliceConfigSpec.sysCommandName],
    "System command",
    root.config[AliceConfigSpec.sysCommandAlias]
), RootComponent {
    override suspend fun isAccessible(event: MessageCreateEvent): Boolean =
        event.guild.flatMap { g -> event.message.authorAsMember.map { g.ownerId == it.id } }.awaitSingle() ||
                event.message.authorAsMember.flatMap { m -> event.client.applicationInfo.map { it.ownerId == m.id } }.awaitSingle()

    override suspend fun execute(event: MessageCreateEvent, args: List<String>, options: Map<String, String?>) {
        event.message.channel.flatMap { channel ->
            try {
                root.execute(args, options)
                event.message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4C"))
            } catch (th: Throwable) {
                channel.createMessage(th.formMessage())
            }
        }.awaitSingle()
    }
}
