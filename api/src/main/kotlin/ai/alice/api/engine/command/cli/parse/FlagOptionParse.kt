package ai.alice.api.engine.command.cli.parse

import ai.alice.api.engine.command.cli.params.Option

object FlagOptionParse : OptionParse {
    override fun parseLong(
        option: Option, name: String, argv: List<String>,
        index: Int, explicitValue: String?
    ): OptionParse.Result {
        if (explicitValue != null) throw IndexOutOfBoundsException()
        return OptionParse.Result(1, name, emptyList())
    }

    override fun parseShort(
        option: Option, name: String, argv: List<String>,
        index: Int, optionIndex: Int
    ): OptionParse.Result {
        val consumed = if (optionIndex == argv[index].lastIndex) 1 else 0
        return OptionParse.Result(consumed, name, emptyList())
    }
}