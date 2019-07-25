package ai.alice.api.discord

import ai.alice.api.CommandAccess
import ai.alice.api.CommandCategory
import ai.alice.api.ICommand
import discord4j.core.event.domain.message.MessageCreateEvent

typealias DiscordCommand = ICommand<MessageCreateEvent>

abstract class AbstractDiscordCommand(
    override val name: String,
    override val description: String? = null,
    override val alias: Collection<String> = setOf(),
    override val usage: String? = null,
    override val access: CommandAccess<MessageCreateEvent> = { true },
    override val category: CommandCategory
) : DiscordCommand