package ai.alice.api.engine.command.cli.params.types

import ai.alice.api.engine.command.cli.WrongParameterValueException
import ai.alice.api.engine.command.cli.params.RawArgument
import ai.alice.api.engine.command.cli.params.RawOption
import ai.alice.api.engine.command.cli.params.convert

internal fun valueToInt(it: String): Int {
    return it.toIntOrNull() ?: throw WrongParameterValueException("$it is not a valid integer")
}

/** Convert the argument values to an `Int` */
fun RawArgument.int() = convert { valueToInt(it) }

/** Convert the option values to an `Int` */
fun RawOption.int() = convert { valueToInt(it) }