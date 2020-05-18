package ai.alice.api.engine.command.cli.parse

import ai.alice.api.engine.command.cli.params.Option

object ValuedOptionParse : OptionParse {
    override fun parseShort(
        option: Option, name: String, argv: List<String>,
        index: Int, optionIndex: Int
    ): OptionParse.Result {
        val opt = argv[index]
        val hasIncludedValue = optionIndex != opt.lastIndex
        val explicitValue = if (hasIncludedValue) opt.substring(optionIndex + 1) else null

        return parseLong(option, name, argv, index, explicitValue)
    }

    override fun parseLong(
        option: Option, name: String, argv: List<String>,
        index: Int, explicitValue: String?
    ): OptionParse.Result {
        require(option.size > 0) {
            "This parser can only be used with a fixed number of arguments. Try the flag parser instead."
        }
        val hasIncludedValue = explicitValue != null
        val consumed = if (hasIncludedValue) option.size else option.size + 1
        val endIndex = index + consumed - 1

        if (endIndex > argv.lastIndex) {
            throw IndexOutOfBoundsException()
        }

        val invocation = if (option.size > 1) {
            var args = argv.slice((index + 1)..endIndex)
            if (explicitValue != null) args = listOf(explicitValue) + args
            OptionParse.Invocation(name, args)
        } else {
            OptionParse.Invocation(name, listOf(explicitValue ?: argv[index + 1]))
        }

        return OptionParse.Result(consumed, invocation)
    }
}