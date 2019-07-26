package ai.alice.commands

import ai.alice.api.CommandOptions
import ai.alice.api.ICommand
import ai.alice.api.discord.DiscordCommand
import ai.alice.commands.discord.HelpCommand
import ai.alice.commands.discord.defaults.RootCommand
import com.typesafe.config.Config
import discord4j.core.event.domain.message.MessageCreateEvent
import io.jooby.Kooby
import io.jooby.require
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineDatabase
import java.awt.Color
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

class CommandRegistry(internal val kooby: Kooby) {
    internal val database: CoroutineDatabase = kooby.require()
    internal val config: Config = kooby.environment.config

    val prefix
        get() = config.getString("discord.prefix")

    val custom = CustomCommands(this)

    internal val discordCommands = mutableSetOf<DiscordCommand>()

    init {
        registerDefaults()
        autoRegister()
    }

    fun handle(event: MessageCreateEvent) {
        GlobalScope.launch(exception(event)) {
            val discordConf = config.getConfig("discord")
            val mention = event.client.selfId.map { "<@${it.asString()}>" }.get()

            event.message.content.ifPresent {
                if (it.startsWith(mention) || it.startsWith(discordConf.getString("prefix"))) {
                    val cOpt = parseOptions(it.let {
                        if (it.startsWith(mention))
                            it.substring("$mention ".length)
                        else
                            it.substring(discordConf.getString("prefix").length)
                    }.split(' '))
                    runBlocking {
                        cOpt[0]?.let { o ->
                            if (custom.contains(o.toLowerCase())) custom.get(o.toLowerCase())
                            else discordCommands.first { it.name == o.toLowerCase() || it.alias.contains(o.toLowerCase()) }
                        }?.execute(event, cOpt)
                    }
                }
            }
        }

    }

    private fun parseOptions(args: List<String>): CommandOptions = DefaultCommandOptions(args)

    @Suppress("UNCHECKED_CAST")
    private fun getEventType(command: ICommand<*>): Type =
        (command::class.java.genericSuperclass as ParameterizedType).actualTypeArguments[0]

    private fun registerDefaults() {
        discordCommands.add(HelpCommand(this))
        discordCommands.add(RootCommand(this))
    }

    @Suppress("UNCHECKED_CAST")
    private fun autoRegister() {
        ServiceLoader.load(ICommand::class.java).forEach {
            val type = getEventType(it)
            when {
                MessageCreateEvent::class.java.isAssignableFrom(type as Class<*>) ->
                    discordCommands.add(it as ICommand<MessageCreateEvent>)
            }
        }
    }

    fun exception(event: MessageCreateEvent): CoroutineExceptionHandler = CoroutineExceptionHandler { _, th ->
        event.message.channel.flatMap {
            it.createEmbed {
                var cause = th.cause
                it.setTitle("ERROR: ${th.javaClass.simpleName}")
                if (th.message != null) {
                    it.setDescription(th.message!!)
                }

                while (cause != null) {
                    it.addField(cause.javaClass.simpleName, cause.localizedMessage, false)
                    cause = cause.cause
                }

                it.setColor(Color.RED)
            }
        }.subscribe()
        th.printStackTrace()
    }

    fun contains(command: String): Boolean = discordCommands.any { it.name == command || it.alias.contains(command) }

    suspend fun get(name: String) =
        custom.get(name) ?: discordCommands.firstOrNull { it.name == name || it.alias.contains(name) }
}