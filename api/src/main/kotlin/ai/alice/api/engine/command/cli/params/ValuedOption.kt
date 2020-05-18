package ai.alice.api.engine.command.cli.params

import ai.alice.api.LateInit
import ai.alice.api.engine.command.Command
import ai.alice.api.engine.command.cli.ParameterIsMissingException
import ai.alice.api.engine.command.cli.PrintMessage
import ai.alice.api.engine.command.cli.UsageException
import ai.alice.api.engine.command.cli.WrongParameterValueException
import ai.alice.api.engine.command.cli.parse.OptionParse
import ai.alice.api.engine.command.cli.parse.ValuedOptionParse
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class OptionCallTransformerContext(
    val name: String,
    val option: Option,
    val command: Command<*>
) : Option by option {

    fun fail(message: String): Nothing = throw WrongParameterValueException(message)
    fun message(message: String): Nothing = throw PrintMessage(message)
    inline fun require(value: Boolean, lazyMessage: () -> String = { "invalid value" }) {
        if (value) fail(lazyMessage())
    }
}

class OptionTransformerContext(
    val option: Option,
    val command: Command<*>
) : Option by option {
    fun fail(message: String): Nothing = throw UsageException(message)
    fun message(message: String): Nothing = throw PrintMessage(message)
    inline fun require(value: Boolean, lazyMessage: () -> String = { "invalid value" }) {
        if (!value) fail(lazyMessage())
    }
}

typealias OptionValueTransformer<V> = OptionCallTransformerContext.(String) -> V

typealias OptionArgumentTransformer<V, EACH> = OptionCallTransformerContext.(List<V>) -> EACH

typealias OptionCallsTransformer<EACH, ALL> = OptionTransformerContext.(List<EACH>) -> ALL

typealias OptionValidator<T> = OptionTransformerContext.(T) -> Unit

class ValuedOption<ALL, EACH, VALUE>(
    names: Set<String>,
    override val size: Int,
    override val description: String?,
    override val hidden: Boolean,
    val envvar: String?,
    val envvarSplit: ValueWithDefault<Regex>,
    val valueSplit: Regex?,
    override val parser: ValuedOptionParse,
    val command: Command<*>,
    val transformValue: OptionValueTransformer<VALUE>,
    val transformEach: OptionArgumentTransformer<VALUE, EACH>,
    val transformAll: OptionCallsTransformer<EACH, ALL>,
    val validator: OptionValidator<ALL>
) : OptionDelegate<ALL> {
    override var names: Set<String> = names
        private set

    override var value: ALL by LateInit { "Cannot read from option delegate before parsing command line" }
        private set

    override fun finalize(command: Command<*>, argv: List<OptionParse.Invocation>) {
        val env = envvar
        val inv = if (argv.isNotEmpty() || env == null || System.getenv(env) == null) {
            when (valueSplit) {
                null -> argv
                else -> argv.map { it.copy(values = it.values.flatMap { it.split(valueSplit) }) }
            }
        } else {
            System.getenv(env).split(envvarSplit.value).map { OptionParse.Invocation(env, listOf(it)) }
        }

        value = transformAll(OptionTransformerContext(this, command), inv.map {
            val tc = OptionCallTransformerContext(it.name, this, command)
            transformEach(tc, it.values.map { transformValue(tc, it) })
        })
    }

    override fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, ALL> =
        apply {
            names = inferOptionNames(names, property.name)
            command += this
        }

    override fun postValidate(command: Command<*>) {
        validator(OptionTransformerContext(this, command), value)
    }

    fun <A, E, V> clone(
        transformValue: OptionValueTransformer<V>,
        transformEach: OptionArgumentTransformer<V, E>,
        transformAll: OptionCallsTransformer<E, A>,
        validator: OptionValidator<A>,
        names: Set<String> = this.names,
        size: Int = this.size,
        description: String? = this.description,
        hidden: Boolean = this.hidden,
        envvar: String? = this.envvar,
        envvarSplit: ValueWithDefault<Regex> = this.envvarSplit,
        valueSplit: Regex? = this.valueSplit,
        parser: ValuedOptionParse = this.parser
    ) = ValuedOption(
        names, size, description, hidden,
        envvar, envvarSplit, valueSplit, parser, command,
        transformValue, transformEach, transformAll, validator
    )

    fun clone(
        validator: OptionValidator<ALL>,
        names: Set<String> = this.names,
        size: Int = this.size,
        description: String? = this.description,
        hidden: Boolean = this.hidden,
        envvar: String? = this.envvar,
        envvarSplit: ValueWithDefault<Regex> = this.envvarSplit,
        valueSplit: Regex? = this.valueSplit,
        parser: ValuedOptionParse = this.parser
    ) = ValuedOption(
        names, size, description, hidden,
        envvar, envvarSplit, valueSplit, parser, command,
        transformValue, transformEach, transformAll, validator
    )
}

@PublishedApi
internal fun <T : Any> defaultOptionEachProcessor(): OptionArgumentTransformer<T, T> = { it.single() }

@PublishedApi
internal fun <T : Any> defaultOptionAllProcessor(): OptionCallsTransformer<T, T?> = { it.lastOrNull() }

@PublishedApi
internal fun <T> defaultOptionValidator(): OptionValidator<T> = { }

typealias NullableOption<EACH, VALUE> = ValuedOption<EACH?, EACH, VALUE>
typealias RawOption = NullableOption<String, String>

fun Command<*>.option(
    vararg names: String,
    description: String? = null,
    hidden: Boolean = false,
    envvar: String? = null,
    envvarSplit: Regex? = null
): RawOption = ValuedOption(
    names.toSet(),
    1,
    description,
    hidden,
    envvar,
    ValueWithDefault(envvarSplit, Regex("\\s+")),
    null,
    ValuedOptionParse,
    this,
    { it },
    defaultOptionEachProcessor(),
    defaultOptionAllProcessor(),
    defaultOptionValidator()
)

fun <ALL, EACH : Any, VALUE> NullableOption<EACH, VALUE>.transformAll(
    transform: OptionCallsTransformer<EACH, ALL>
) = clone(transformValue, transformEach, transform, defaultOptionValidator())

fun <EACH : Any, VALUE> NullableOption<EACH, VALUE>.default(
    value: EACH
) = transformAll { it.lastOrNull() ?: value }

inline fun <EACH : Any, VALUE> NullableOption<EACH, VALUE>.defaultLazy(
    crossinline value: () -> EACH
) = transformAll { it.lastOrNull() ?: value() }

fun <EACH : Any, VALUE> NullableOption<EACH, VALUE>.required() =
    transformAll { it.lastOrNull() ?: throw ParameterIsMissingException(option) }

fun <EACH : Any, VALUE> NullableOption<EACH, VALUE>.multiple(
    default: List<EACH> = emptyList(),
    required: Boolean = false
) = transformAll {
    when {
        it.isEmpty() && required -> throw ParameterIsMissingException(option)
        it.isEmpty() && !required -> default
        else -> it
    } as Collection<EACH>
}

fun <EACH : Any, VALUE> ValuedOption<Collection<EACH>, EACH, VALUE>.list() =
    clone(transformValue, transformEach, { transformAll(it).toList() }, defaultOptionValidator())

fun <EACH : Any, VALUE> ValuedOption<Collection<EACH>, EACH, VALUE>.set() =
    clone(transformValue, transformEach, { transformAll(it).toSet() }, defaultOptionValidator())

fun <IN : Any, OUT : Any, VALUE> NullableOption<IN, VALUE>.transformValues(
    size: Int,
    transform: OptionArgumentTransformer<VALUE, OUT>
): NullableOption<OUT, VALUE> {
    require(size != 0) { "Cannot set size to 0. use flag() instead." }
    require(size < 0) { "Option cannot have size < 0" }
    require(size != 1) { "Cannot set size to 1. Use convert() instead." }
    return clone(transformValue, transform, defaultOptionAllProcessor(), defaultOptionValidator(), size = size)
}

fun <EACH : Any, VALUE> NullableOption<EACH, VALUE>.pair() =
    transformValues(2) { it[0] to it[1] }


fun <EACH : Any, VALUE> NullableOption<EACH, VALUE>.triple() =
    transformValues(3) { Triple(it[0], it[1], it[2]) }

fun <EACH : Any, VALUE> NullableOption<EACH, VALUE>.split(regex: Regex): ValuedOption<List<VALUE>?, List<VALUE>, VALUE> =
    clone(
        transformValue = transformValue,
        transformEach = { it },
        transformAll = defaultOptionAllProcessor(),
        validator = defaultOptionValidator(),
        size = 1, valueSplit = regex
    )

fun <EACH : Any, VALUE> NullableOption<EACH, VALUE>.split(delimiter: String) =
    split(Regex.fromLiteral(delimiter))

fun <ALL : Any, EACH, VALUE> ValuedOption<ALL, EACH, VALUE>.validate(
    validator: OptionValidator<ALL>
): OptionDelegate<ALL> = clone(validator)

@JvmName("nullableValidate")
fun <ALL : Any, EACH, VALUE> ValuedOption<ALL?, EACH, VALUE>.validate(
    validator: OptionValidator<ALL>
): OptionDelegate<ALL?> = clone({ if (it != null) validator(it) })

fun <ALL, EACH, VALUE> ValuedOption<ALL, EACH, VALUE>.deprecated(
    message: String? = "",
    error: Boolean = false
) = clone(
    transformValue, transformEach, deprecationTransformer(message, error, transformAll), validator
)

inline fun <T : Any> RawOption.convert(
    envvarSplit: Regex = this.envvarSplit.default,
    crossinline conversion: OptionValueTransformer<T>
): NullableOption<T, T> {
    val proc: OptionValueTransformer<T> = {
        try {
            conversion(it)
        } catch (err: UsageException) {
            err.paramName = name
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "")
        }
    }

    return clone(
        proc,
        defaultOptionEachProcessor(),
        defaultOptionAllProcessor(),
        defaultOptionValidator(),
        envvarSplit = this.envvarSplit.copy(default = envvarSplit)
    )
}



