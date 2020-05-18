package ai.alice.api.engine.command.cli.params

import ai.alice.api.engine.command.Command
import ai.alice.api.engine.command.cli.parse.OptionParse
import ai.alice.api.exception.AliceCommandException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Option {
    val names: Set<String>
    val description: String?
    val hidden: Boolean
    val size: Int

    val parser: OptionParse

    fun finalize(command: Command<*>, argv: List<OptionParse.Invocation>)
    fun postValidate(command: Command<*>)
}

interface OptionDelegate<T> : ReadOnlyProperty<Any?, T>, Option {
    val value: T

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, T>

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

internal fun inferOptionNames(names: Set<String>, propertyName: String): Set<String> {
    if (names.isNotEmpty()) {
        val invalidName = names.find { !it.matches(Regex("\\p{Punct}{1,2}[\\w-_]+")) }
        require(invalidName == null) { "Invalid option name \"$invalidName\"" }
        return names
    }
    val normalizedName = propertyName.split(Regex("(?<=[a-z])(?=[A-Z])"))
        .joinToString("-", prefix = "--") { it.toLowerCase() }
    return setOf(normalizedName)
}

internal fun <EachT, AllT> deprecationTransformer(
    message: String? = "",
    error: Boolean = false,
    transformAll: OptionCallsTransformer<EachT, AllT>
): OptionCallsTransformer<EachT, AllT> = {
    if (it.isNotEmpty()) {
        val msg = when (message) {
            null -> ""
            "" -> "${if (error) "ERROR" else "WARNING"}: option ${option.names.maxBy { o -> o.length }} is deprecated"
            else -> message
        }
        if (error) {
            throw AliceCommandException(msg)
        } else if (message != null) {
            message(msg)
        }
    }
    transformAll(it)
}

internal fun Option.longestName(): String? = names.maxBy { it.length }
