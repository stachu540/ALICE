package ai.alice.api.engine.command.cli.params.types

import ai.alice.api.engine.command.cli.WrongParameterValueException
import ai.alice.api.engine.command.cli.params.RawArgument
import ai.alice.api.engine.command.cli.params.RawOption
import ai.alice.api.engine.command.cli.params.convert

private fun valueToFloat(it: String): Float {
    return it.toFloatOrNull() ?: throw WrongParameterValueException("$it is not a valid floating point value")
}

/** Convert the argument values to a `Float` */
fun RawArgument.float() = convert { valueToFloat(it) }

/** Convert the option values to a `Float` */
fun RawOption.float() = convert { valueToFloat(it) }