package ai.alice.commands.discord.defaults

import ai.alice.api.CommandCategory
import ai.alice.api.CommandOptions
import ai.alice.api.discord.AbstractDiscordCommand
import ai.alice.commands.CommandRegistry
import ai.alice.commands.UnknownCommandException
import ai.alice.commands.discord.HelpCommand
import discord4j.core.event.domain.message.MessageCreateEvent

class RootCommand(private val registry: CommandRegistry) : AbstractDiscordCommand(
    "alice",
    "Main command master",
    setOf(),
    "<parameters> [commands|system|echo|eval]",
    category = CommandCategory.ROOT_COMMANDS
) {
    override suspend fun execute(event: MessageCreateEvent, options: CommandOptions) {
        val command = options.getArgument(1)
        when (command) {
            "commands" -> doCustomCommand(event, options)
            "system" -> doSysCommand(event, options)
            "echo" -> doEcho(event, options)
            "eval" -> doEval(event, options)
            else -> {
                throw UnknownCommandException("This parameter is not supported").also {
                    HelpCommand.printUsage(event, this, registry.prefix)
                }
            }
        }
    }

    private suspend fun doCustomCommand(event: MessageCreateEvent, options: CommandOptions) {
        throw UnsupportedOperationException("This action is not supported yet!")
    }

    private suspend fun doSysCommand(event: MessageCreateEvent, options: CommandOptions) {
        throw UnsupportedOperationException("This action is not supported yet!")
    }

    private suspend fun doEcho(event: MessageCreateEvent, options: CommandOptions) {
        throw UnsupportedOperationException("This action is not supported yet!")
    }

    private suspend fun doEval(event: MessageCreateEvent, options: CommandOptions) {
        throw UnsupportedOperationException("This action is not supported yet!")
    }
}