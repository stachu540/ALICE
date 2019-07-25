package ai.alice.commands.discord.defaults

import ai.alice.api.discord.DiscordCommand
import ai.alice.commands.discord.AccessLevel

abstract class AbstractDiscordDefaultCommand(
    override val name: String,
    override val description: String? = null,
    override val alias: Collection<String> = setOf(),
    override val usage: String? = null,
    override val access: AccessLevel = AccessLevel.EVERYONE
) : DiscordCommand