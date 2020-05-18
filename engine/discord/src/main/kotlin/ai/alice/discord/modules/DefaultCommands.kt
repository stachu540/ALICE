package ai.alice.discord.modules

import ai.alice.api.engine.command.Command
import ai.alice.api.engine.command.cli.params.argument
import ai.alice.api.engine.command.cli.params.option
import ai.alice.api.engine.command.cli.params.optional
import ai.alice.api.engine.module.Module
import ai.alice.api.exception.AliceCommandException
import ai.alice.discord.Discord
import ai.alice.discord.commands.DiscordCommandEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.Instant

@FlowPreview
@ExperimentalCoroutinesApi
class DefaultCommands : Module<Discord> {
    override suspend fun handle(engine: Discord) {
        engine.command("help", "h", group = Groups.GENERAL) { event ->
            val name by argument("COMMAND").optional()
            val group by option("--group")

            when {
                name != null -> {
                    event.commandHelp(engine.provider[name!!.toLowerCase()])
                }
                group != null -> {
                    event.respondEmbed {
                        setColor(Color.ORANGE)
                        setTimestamp(Instant.now())
                        engine.provider.filter {
                            engine.alice.async {
                                it.group.name == group!!.toLowerCase() && it.group(event) && it.access(
                                    event
                                )
                            }.getCompleted()
                        }.map {
                            setTitle("List of available commands by group: `${it.group.displayName}`")
                            "${event.prefix}${it.name}${if (it.description != null) " - ${it.description}" else ""}"
                        }.cut(1024).map {
                            MessageEmbed.Field(null, it, false)
                        }.forEach {
                            addField(it)
                        }
                    }
                }
                else -> {
                    event.respondEmbed {
                        setColor(Color.ORANGE)
                        setTimestamp(Instant.now())
                        setTitle("List of available commands")
                        engine.provider.groupBy { it.group }.map { (group, cmd) ->
                            group.displayName to
                                    cmd.map { "${event.prefix}${it.name}${if (it.description != null) " - ${it.description}" else ""}" }
                                        .cut(1024)
                        }.flatMap {
                            it.second.mapIndexed { index, value ->
                                MessageEmbed.Field(if (index == 0) it.first else null, value, false)
                            }
                        }.forEach {
                            addField(it)
                        }
                    }
                }
            }
        }
        engine.command("test", "t", group = Groups.DEBUG) {
            it.respond("Sending message test \uD83D\uDE0E \uD83D\uDE0E \uD83D\uDE0E \uD83D\uDE0E")
        }
    }

    private suspend fun DiscordCommandEvent.commandHelp(command: Command<DiscordCommandEvent>?) {
        command?.run {
            if (!access(this@commandHelp)) throw AliceCommandException("Cannot get access to this command: \"$prefix${name}\"")
            else if (!group(this@commandHelp)) throw AliceCommandException("Cannot get access to this group: \"${group.displayName}\"")
            else usage()
        }
    }

    private fun List<String>.cut(length: Int) = map { it to it.length }.let {
        val cmds = mutableListOf<String>()
        var builder = StringBuilder(length)
        it.forEach { (c, l) ->
            if ((builder.length + l) > length) {
                cmds += builder.toString()
                builder = builder.clear()
            } else {
                builder.append(c)
            }
        }
        if (builder.isNotEmpty()) {
            cmds += builder.toString()
        }
        cmds
    }
}