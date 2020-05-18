package ai.alice.api.engine.command.cli.params.types

import ai.alice.api.engine.command.cli.WrongParameterValueException
import ai.alice.api.engine.command.cli.params.RawArgument
import ai.alice.api.engine.command.cli.params.RawOption
import ai.alice.api.engine.command.cli.params.convert

internal fun valueToLong(it: String): Long {
    return it.toLongOrNull() ?: throw WrongParameterValueException("$it is not a valid integer")
}

/** Convert the argument values to a `Long` */
fun RawArgument.long() = convert { valueToLong(it) }

/** Convert the option values to a `Long` */
fun RawOption.long() = convert { valueToLong(it) }