package ai.alice.api.engine.command.cli.parse

import ai.alice.api.engine.command.cli.params.Option

interface OptionParse {
    fun parseShort(option: Option, name: String, argv: List<String>, index: Int, optionIndex: Int): Result
    fun parseLong(option: Option, name: String, argv: List<String>, index: Int, explicitValue: String?): Result

    data class Invocation(val name: String, val values: List<String>)

    data class Result(val consumed: Int, val invocation: Invocation) {
        constructor(consumed: Int, name: String, values: List<String>) : this(consumed, Invocation(name, values))
    }
}