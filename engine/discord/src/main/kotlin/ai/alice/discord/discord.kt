package ai.alice.discord

import ai.alice.api.Alice
import ai.alice.api.config
import ai.alice.api.engine.Conditional
import ai.alice.api.engine.Engine
import ai.alice.api.engine.command.Command
import ai.alice.api.engine.command.CommandAccess
import ai.alice.api.engine.command.CommandGroup
import ai.alice.api.engine.module.AbstractModuleProvider
import ai.alice.api.store.IdObject
import ai.alice.discord.commands.DiscordCommand
import ai.alice.discord.commands.DiscordCommandEvent
import ai.alice.discord.commands.DiscordCommandProvider
import ai.alice.discord.modules.DefaultCommands
import com.typesafe.config.ConfigValueType
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.requests.RestAction
import java.nio.channels.AlreadyConnectedException
import java.nio.channels.NotYetConnectedException
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass
import kotlin.reflect.cast

@FlowPreview
@ExperimentalCoroutinesApi
@Conditional(DiscordConditionLauncher::class)
class Discord constructor(override val alice: Alice) :
    Engine<MessageReceivedEvent, DiscordCommandEvent, Discord> {
    private var jda: JDA? = null

    private val config: DiscordConfig by alice.config("alice.engines.discord")

    private val listener = ChannelEventListener(alice.broadcast { })

    private val jdaBuilder = JDABuilder.createDefault(null)
        .setToken(config.token)
        .addEventListeners(listener)

    override val provider: DiscordCommandProvider = DiscordCommandProvider(config.prefix) {
        alice.async {
            alice.dataStore.create(DiscordPrefix::class).get(it.guild.idLong)
                .map { it.prefix }.awaitOrNull()
        }.getCompleted()
    }
    override val modules: DiscordModuleProvider = DiscordModuleProvider(this)

    override val isActive: Boolean
        get() = jda != null && jda!!.status == JDA.Status.CONNECTED

    init {
        alice.launch {
            listener.channel.asFlow().filter { it is MessageReceivedEvent }
                .map { it as MessageReceivedEvent }
                .collect { provider(it) }
        }.start()
    }

    @ExperimentalStdlibApi
    override fun <E : Any> on(type: KClass<E>, handler: suspend (E) -> Unit) {
        alice.launch {
            listener.channel.asFlow().filter { type.isInstance(it) }.map { type.cast(it) }
                .collect(handler)
        }.start()
    }

    override fun command(
        vararg names: String,
        description: String?,
        accessor: CommandAccess<DiscordCommandEvent>,
        group: CommandGroup<DiscordCommandEvent>,
        exec: suspend Command<DiscordCommandEvent>.(DiscordCommandEvent) -> Unit
    ) {
        provider.add(DiscordCommand(names.toList(), description, accessor, group, exec))
    }

    override suspend fun start() {
        if (this.jda != null) {
            throw AlreadyConnectedException()
        } else {
            this.jda = withContext(alice.coroutineContext) {
                jdaBuilder.build().awaitReady().also {
                    modules.initialize()
                }
            }
        }
    }

    override fun stop() {
        this.jda?.shutdownNow()?.also {
            this.jda = null
        } ?: throw NotYetConnectedException()
    }
}

class DiscordConditionLauncher : Engine.ValidatorLauncher {
    override fun handle(engine: Engine<*, *, *>): Boolean =
        with(engine.alice.configuration) {
            hasPath("alice.engine.discord") && hasPath("alice.engine.discord.token") &&
                    with(getValue("alice.engine.discord.token")) {
                        valueType() == ConfigValueType.STRING && (this.unwrapped() as String).isNotBlank()
                    }
        }
}

@FlowPreview
@ExperimentalCoroutinesApi
class DiscordModuleProvider(engine: Discord) : AbstractModuleProvider<Discord>(engine) {
    override fun initialize() {
        apply<DefaultCommands>()
    }
}

data class DiscordConfig(
    val token: String,
    val prefix: String
) {
    data class Prefix(
        val default: String = "!",
        val type: Type
    ) {
        enum class Type {
            CACHE, STORE
        }
    }
}

suspend fun <T> RestAction<T>.await(): T = suspendCoroutine { continuation ->
    queue({
        continuation.resume(it)
    }, {
        continuation.resumeWithException(it)
    })
}

@Entity
@Table(name = "discord_prefix")
data class DiscordPrefix(
    @Id
    override val id: Long,
    val prefix: String
) : IdObject<Long>

@ExperimentalCoroutinesApi
class ChannelEventListener(
    val channel: BroadcastChannel<GenericEvent> = ConflatedBroadcastChannel()
) : EventListener {

    override fun onEvent(event: GenericEvent) {
        channel.sendBlocking(event)
    }
}