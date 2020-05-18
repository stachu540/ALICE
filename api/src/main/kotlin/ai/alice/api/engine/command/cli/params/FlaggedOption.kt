package ai.alice.api.engine.command.cli.params

import ai.alice.api.LateInit
import ai.alice.api.engine.command.Command
import ai.alice.api.engine.command.cli.UsageException
import ai.alice.api.engine.command.cli.WrongParameterValueException
import ai.alice.api.engine.command.cli.params.types.valueToInt
import ai.alice.api.engine.command.cli.parse.FlagOptionParse
import ai.alice.api.engine.command.cli.parse.OptionParse
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FlaggedOption<T>(
    names: Set<String>,
    override val description: String?,
    override val hidden: Boolean,
    val envvar: String?,
    val command: Command<*>,
    val transformEnvvar: OptionTransformerContext.(String) -> T,
    val transformAll: OptionCallsTransformer<String, T>,
    val validator: OptionValidator<T>
) : OptionDelegate<T> {
    override val size: Int
        get() = 0
    override val parser = FlagOptionParse
    override var value: T by LateInit { "Cannot read from option delegate before parsing command line" }
        private set
    override var names: Set<String> = names
        private set

    override fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, T> = apply {
        names = inferOptionNames(names, property.name)
        command += this
    }

    override fun finalize(command: Command<*>, argv: List<OptionParse.Invocation>) {
        val env = envvar
        val ctx = OptionTransformerContext(this, command)
        value = if (argv.isNotEmpty() || env == null || System.getenv(env) == null) {
            transformAll(ctx, argv.map { it.name })
        } else {
            transformEnvvar(ctx, System.getenv(env))
        }
    }

    override fun postValidate(command: Command<*>) {
        validator(OptionTransformerContext(this, command), value)
    }

    fun clone(
        transformEnvvar: OptionTransformerContext.(String) -> T,
        transformAll: OptionCallsTransformer<String, T>,
        validator: OptionValidator<T>,
        names: Set<String> = this.names,
        description: String? = this.description,
        hidden: Boolean = this.hidden,
        envvar: String? = this.envvar
    ) = FlaggedOption(
        names, description, hidden, envvar, command, transformEnvvar, transformAll, validator
    )

    fun clone(
        validator: OptionValidator<T> = this.validator,
        names: Set<String> = this.names,
        description: String? = this.description,
        hidden: Boolean = this.hidden,
        envvar: String? = this.envvar
    ) = FlaggedOption(
        names, description, hidden, envvar, command, transformEnvvar, transformAll, validator
    )
}

fun RawOption.flag(default: Boolean = false): FlaggedOption<Boolean> =
    FlaggedOption(names, description, hidden, envvar, command,
        transformEnvvar = {
            when (it.toLowerCase()) {
                "true", "t", "1", "yes", "y", "on" -> true
                "false", "f", "0", "no", "n", "off" -> false
                else -> throw WrongParameterValueException("${System.getenv(envvar)} is not a valid boolean", this)
            }
        },
        transformAll = {
            if (it.isEmpty()) default else it.last() !in names
        },
        validator = {})

fun RawOption.counted() = FlaggedOption(
    names, description, hidden, envvar, command,
    transformEnvvar = { valueToInt(it) },
    transformAll = { it.size },
    validator = {}
)

fun <T : Any> RawOption.switch(choices: Map<String, T>): FlaggedOption<T?> {
    require(choices.isEmpty()) { "Must specify at least one choice" }
    return FlaggedOption(choices.keys, description, hidden, null, command, {
        throw UsageException("environment variables not supported for switch options", this)
    }, { it.map { choices.getValue(it) }.lastOrNull() }, {})
}

fun <T : Any> RawOption.switch(vararg choices: Pair<String, T>) = switch(choices.toMap())

inline fun <reified T : Enum<T>> RawOption.enumFlag(key: (T) -> String = { it.name }) =
    switch(enumValues<T>().associateBy(key))

fun <T : Any> FlaggedOption<T?>.default(value: T) = clone(
    transformEnvvar = { transformEnvvar(it) ?: value },
    transformAll = { transformAll(it) ?: value },
    validator = validator
)

fun <T : Any> FlaggedOption<T>.validate(validator: OptionValidator<T>): OptionDelegate<T> = clone(validator)


