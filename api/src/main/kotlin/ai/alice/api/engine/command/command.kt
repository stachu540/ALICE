package ai.alice.api.engine.command

import ai.alice.api.engine.command.cli.PrintMessage
import ai.alice.api.engine.command.cli.PrintUsage
import ai.alice.api.engine.command.cli.params.Argument
import ai.alice.api.engine.command.cli.params.Option
import ai.alice.api.engine.command.cli.parse.GlobalParse
import ai.alice.api.exception.AliceCommandException

abstract class Command<TEvent : CommandEvent<*, *>>(
    val name: String,
    val alias: Array<String>,
    val description: String?,
    val access: CommandAccess<TEvent>,
    val group: CommandGroup<TEvent>
) {
    internal val options = mutableSetOf<Option>()
    internal val arguments = mutableSetOf<Argument>()

    internal operator fun plusAssign(option: Option) {
        options += option
    }

    internal operator fun plusAssign(argument: Argument) {
        arguments += argument
    }

    suspend operator fun invoke(event: TEvent) {
        try {
            parse(event.args)
            execute(event)
        } catch (_: PrintUsage) {
            event.usage()
        } catch (msg: PrintMessage) {
            event.message(msg.message)
        } catch (t: Exception) {
            event.error(t)
        }
    }

    private fun parse(argv: Array<String>) {
        GlobalParse.parse(argv.toList(), this)
    }

    abstract suspend fun execute(event: TEvent)

    abstract suspend fun TEvent.message(message: String?, isError: Boolean = false)
    abstract suspend fun TEvent.error(exception: Exception)
    abstract suspend fun TEvent.usage()
}

interface CommandEvent<TMessage, MSG> {
    val raw: TMessage
    val message: MSG
    val rawMessage: String
    val command: String
    val args: Array<String>
    val prefix: String

    suspend fun respond(message: String): MSG
    suspend fun dm(message: String): MSG
}

typealias CommandAccess<TEvent> = suspend (TEvent) -> Boolean

data class CommandGroup<TEvent : CommandEvent<*, *>> internal constructor(
    val name: String,
    val displayName: String,
    private val access: CommandAccess<TEvent>
) {
    suspend operator fun invoke(event: TEvent): Boolean = access(event)

    override fun toString(): String = "Group($displayName)"
}

interface CommandProvider<TMessage, TEvent : CommandEvent<TMessage, *>> : Iterable<Command<TEvent>> {
    val prefix: Prefix<TMessage>

    val names: Collection<String>

    operator fun get(name: String): Command<TEvent>?

    operator fun plusAssign(command: Command<TEvent>) = add(command)
    operator fun minusAssign(name: String) = remove(name)

    fun add(command: Command<TEvent>)
    fun remove(name: String, alias: Boolean = false)

    suspend operator fun invoke(event: TMessage)
}

interface Prefix<TMessage> {
    val default: String
    operator fun get(event: TMessage): String
}

fun <TEvent : CommandEvent<*, *>> group(
    name: String,
    displayName: String? = null,
    access: CommandAccess<TEvent> = { true }
): CommandGroup<TEvent> {
    val n: String = name.replace(Regex("([A-Z])"), "_${"$"}1").toLowerCase().let {
        if (it.startsWith("_")) it.substring(1) else it
    }

    val dn: String = displayName ?: name.replace(Regex("([A-Z])"), " $1").trim().capitalize()

    return CommandGroup<TEvent>(n, dn, access)
}

fun <TEvent : CommandEvent<*, *>> access(access: CommandAccess<TEvent>) = access
abstract class AbstractCommandProvider<TMessage, TEvent : CommandEvent<TMessage, *>>(override val prefix: Prefix<TMessage>) :
    CommandProvider<TMessage, TEvent> {

    private val commands = mutableSetOf<Command<TEvent>>()

    override val names: Collection<String>
        get() = commands.flatMap { (it.alias + it.name).toList() }
            .map { it.toLowerCase() }

    override operator fun iterator(): Iterator<Command<TEvent>> = commands.iterator()

    override fun add(command: Command<TEvent>) {
        val cmds = (command.alias + command.name)
            .map { it.toLowerCase() }
            .filter { it in names }

        if (cmds.isNotEmpty()) {
            throw AliceCommandException("Cannot register already existed commands: $cmds")
        } else {
            commands += command
        }
    }

    override fun remove(name: String, alias: Boolean) {
        commands.removeIf {
            it.name.toLowerCase() == name.toLowerCase()
        }
    }

    override fun get(name: String): Command<TEvent>? =
        commands.firstOrNull {
            it.name == name.toLowerCase() || it.alias.any { it.toLowerCase() == name.toLowerCase() }
        }

    override suspend fun invoke(event: TMessage) {
        convert(event)?.let { e ->
            commands.firstOrNull {
                it.name.toLowerCase() == e.command.toLowerCase() ||
                        e.command.toLowerCase() in it.alias.map { it.toLowerCase() }
            }?.invoke(e)
        }
    }

    protected abstract suspend fun convert(event: TMessage): TEvent?
}