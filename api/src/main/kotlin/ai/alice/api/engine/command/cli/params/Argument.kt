package ai.alice.api.engine.command.cli.params

import ai.alice.api.LateInit
import ai.alice.api.engine.command.Command
import ai.alice.api.engine.command.cli.PrintMessage
import ai.alice.api.engine.command.cli.UsageException
import ai.alice.api.engine.command.cli.WrongParameterValueException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Argument {
    val name: String
    val description: String?
    val required: Boolean
    val size: Int

    fun finalize(command: Command<*>, argv: List<String>)
    fun postValidate(command: Command<*>)
}

interface ArgumentDelegate<T> : ReadOnlyProperty<Any?, T>, Argument {
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, T>
}

class ArgumentTransformContext(val argument: Argument, val command: Command<*>) : Argument by argument {
    fun fail(message: String): Nothing = throw WrongParameterValueException(message)
    fun message(message: String): Nothing = throw PrintMessage(message)
    inline fun require(value: Boolean, lazyMessage: () -> String = { "invalid value" }) {
        if (!value) fail(lazyMessage())
    }
}

typealias ArgumentValueTransformer<T> = ArgumentTransformContext.(String) -> T

typealias ArgumentCallTransformer<ALL, EACH> = ArgumentTransformContext.(List<EACH>) -> ALL

typealias ArgumentValidator<ALL> = ArgumentTransformContext.(ALL) -> Unit

class ArgumentProcessor<ALL, VALUE>(
    name: String,
    override val size: Int,
    override val required: Boolean,
    override val description: String?,
    val command: Command<*>,
    val transformValue: ArgumentValueTransformer<VALUE>,
    val transformAll: ArgumentCallTransformer<ALL, VALUE>,
    val validator: ArgumentValidator<ALL>
) : ArgumentDelegate<ALL> {

    init {
        require(size != 0) { "Arguments cannot have 0 values size" }
    }

    override var name: String = name
        private set
    internal var value: ALL by LateInit { "Cannot read from argument delegate before parsing command line" }
        private set

    override fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, ALL> =
        apply {
            if (name.isBlank()) name = property.name.split(Regex("[\\-A-Z]")).filter { it.isNotBlank() }
                .joinToString("_") { it.toUpperCase() }
            command += this
        }

    override fun getValue(thisRef: Any?, property: KProperty<*>): ALL = value

    override fun finalize(command: Command<*>, argv: List<String>) {
        val ctx = ArgumentTransformContext(this, command)
        value = transformAll(ctx, argv.map { transformValue(ctx, it) })
    }

    override fun postValidate(command: Command<*>) {
        validator(ArgumentTransformContext(this, command), value)
    }

    fun <A, V> clone(
        transformValue: ArgumentValueTransformer<V>,
        transformAll: ArgumentCallTransformer<A, V>,
        validator: ArgumentValidator<A>,
        name: String = this.name,
        size: Int = this.size,
        required: Boolean = this.required,
        description: String? = this.description
    ) = ArgumentProcessor(name, size, required, description, command, transformValue, transformAll, validator)

    fun clone(
        validator: ArgumentValidator<ALL> = this.validator,
        name: String = this.name,
        size: Int = this.size,
        required: Boolean = this.required,
        description: String? = this.description
    ) = ArgumentProcessor(name, size, required, description, command, transformValue, transformAll, validator)
}

internal typealias RawArgument = ArgumentProcessor<String, String>

@PublishedApi
internal fun <T : Any> defaultArgumentAllProcessor(): ArgumentCallTransformer<T, T> = { it.single() }

@PublishedApi
internal fun <T> defaultArgumentValidator(): ArgumentValidator<T> = {}

fun Command<*>.argument(name: String? = null, description: String? = null): RawArgument = ArgumentProcessor(
    name ?: "",
    1,
    true,
    description,
    this,
    { it },
    defaultArgumentAllProcessor(),
    defaultArgumentValidator()
)

fun <ALLIN, VALUE, ALLOUT> ArgumentProcessor<ALLIN, VALUE>.transformAll(
    size: Int? = null,
    required: Boolean? = null,
    transform: ArgumentCallTransformer<ALLOUT, VALUE>
) =
    clone(
        transformValue, transform, defaultArgumentValidator(),
        size = size ?: this.size,
        required = required ?: this.required
    )

fun <ALL : Any, VALUE> ArgumentProcessor<ALL, VALUE>.optional() =
    transformAll(required = false) { if (it.isEmpty()) null else transformAll(it) }

fun <T : Any> ArgumentProcessor<T, T>.multiple(required: Boolean = false) =
    transformAll(-1, required) { it as Collection<T> }

fun <T : Any> ArgumentProcessor<Collection<T>, T>.list() =
    transformAll(-1) { it.toList() }

fun <T : Any> ArgumentProcessor<Collection<T>, T>.set() =
    transformAll(-1) { it.toSet() }

fun <T : Any> ArgumentProcessor<T, T>.pair() =
    transformAll(2) { it[0] to it[1] }

fun <T : Any> ArgumentProcessor<T, T>.triple() =
    transformAll(3) { Triple(it[0], it[1], it[2]) }

fun <T : Any> ArgumentProcessor<T, T>.default(value: T): ArgumentDelegate<T> =
    transformAll(required = false) { it.firstOrNull() ?: value }

inline fun <T : Any> ArgumentProcessor<T, T>.defaultLazy(crossinline value: () -> T): ArgumentDelegate<T> =
    transformAll(required = false) { it.firstOrNull() ?: value() }

inline fun <T : Any> RawArgument.convert(crossinline conversion: ArgumentValueTransformer<T>): ArgumentProcessor<T, T> {
    val conv: ArgumentValueTransformer<T> = {
        try {
            conversion(it)
        } catch (err: UsageException) {
            err.argument = argument
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "")
        }
    }

    return clone(conv, defaultArgumentAllProcessor(), defaultArgumentValidator())
}

fun <ALL : Any, VALUE> ArgumentProcessor<ALL, VALUE>.validate(validator: ArgumentValidator<ALL>): ArgumentDelegate<ALL> =
    clone(validator)

@JvmName("nullableValidate")
fun <ALL : Any, VALUE> ArgumentProcessor<ALL?, VALUE>.validate(validator: ArgumentValidator<ALL>): ArgumentDelegate<ALL?> =
    clone({ if (it != null) validator(it) })