package ai.alice.api.engine.command.cli.params.types

import ai.alice.api.engine.command.cli.WrongParameterValueException
import ai.alice.api.engine.command.cli.params.RawArgument
import ai.alice.api.engine.command.cli.params.RawOption
import ai.alice.api.engine.command.cli.params.convert

private fun valueToDouble(it: String): Double {
    return it.toDoubleOrNull() ?: throw WrongParameterValueException("$it is not a valid floating point value")
}

fun RawArgument.double() = convert { valueToDouble(it) }

fun RawOption.double() = convert { valueToDouble(it) }