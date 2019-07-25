package ai.alice.commands.discord

import ai.alice.api.CommandCategory
import ai.alice.api.CommandOptions
import ai.alice.api.ICommand
import ai.alice.api.discord.AbstractDiscordCommand
import ai.alice.commands.CommandRegistry
import ai.alice.commands.UnknownCommandException
import ai.alice.commands.discord.custom.DiscordCustomCommand
import ai.alice.commands.discord.defaults.AbstractDiscordDefaultCommand
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitSingle
import org.litote.kmongo.eq
import java.awt.Color
import java.time.Instant

class HelpCommand(private val registry: CommandRegistry) :
    AbstractDiscordCommand(
        "help",
        "Getting Help for specific or any available commands",
        setOf("commands"),
        "[parameters] [command]",
        category = CommandCategory.ROOT_COMMANDS
    ) {
    override suspend fun execute(event: MessageCreateEvent, options: CommandOptions) {
        val custom = options.containOption("custom")
        val root = options.containOption("root-only")
        val access = options.getOption("access")?.let { AccessLevel.valueOf(it.replace('-', '_').toUpperCase()) }
            ?: AccessLevel.EVERYONE
        val command = options.getArgument(1)

        if (command != null) {
            if (registry.contains(command)) {
                val c = registry.get(command)
                if (c == null) throw UnknownCommandException("This command is not exist! '$command'")
                else printUsage(event, c, registry.prefix)
            }
        } else {
            event.message.channel.flatMap {
                it.createMessage {
                    GlobalScope.launch(registry.exception(event)) {
                        val mention = event.message.author.get().mention
                        val commands = when {
                            custom -> registry.custom.getCollection().find(DiscordCustomCommand::access eq access).toList()
                            root -> registry.discordCommands.filter { it is AbstractDiscordDefaultCommand && it.access == access }.toList()
                            else -> registry.discordCommands.filter { it.access == access }
                        }
                        it.setContent("$mention, Here is your available commands!")
                        it.setEmbed {
                            it.setTitle("Available commands")
                            commands.groupBy { it.category }.forEach { e ->
                                it.addField(
                                    e.key.name.replace('_', ' ').capitalize(),
                                    e.value.joinToString("\n") {
                                        "${registry.prefix}${it.name}${it.description?.let { " $it" } ?: ""}"
                                    },
                                    false
                                )
                            }
                        }
                    }
                }
            }.awaitSingle()
        }
    }

    companion object {
        suspend fun printUsage(event: MessageCreateEvent, command: ICommand<MessageCreateEvent>, prefix: String) {
            event.message.channel.flatMap {
                it.createEmbed {
                    it.setTitle("$prefix${command.name}${command.usage?.let { " $it" } ?: ""}")
                    if (command.description != null) {
                        it.setDescription(command.description!!)
                    }
                    if (command.alias.isNotEmpty()) {
                        it.addField(
                            "Aliases",
                            command.alias.joinToString("\n") { "**$prefix$it${command.usage?.let { " $it" } ?: ""}**" },
                            false
                        )
                    }
                    it.addField("Minimal Access Level", command.access.toString().toLowerCase(), false)
                    it.setColor(Color.BLUE)
                    it.setTimestamp(Instant.now())
                }
            }.awaitSingle()
        }
    }
}
