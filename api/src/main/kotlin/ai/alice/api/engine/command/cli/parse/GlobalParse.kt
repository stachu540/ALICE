package ai.alice.api.engine.command.cli.parse

import ai.alice.api.engine.command.Command
import ai.alice.api.engine.command.cli.InvalidArgumentValueCountException
import ai.alice.api.engine.command.cli.OptionNotFoundException
import ai.alice.api.engine.command.cli.ParameterIsMissingException
import ai.alice.api.engine.command.cli.UsageException
import ai.alice.api.engine.command.cli.params.Argument
import ai.alice.api.engine.command.cli.params.Option
import java.lang.reflect.ParameterizedType

object GlobalParse {
    fun parse(argv: List<String>, command: Command<*>, startingArgI: Int = 0) {
        val optionsByName = HashMap<String, Option>()
        val arguments = command.arguments
        val longNames = mutableSetOf<String>()

        for (option in command.options) {
            for (name in option.names) {
                optionsByName[name] = option
                if (name.length > 2) longNames += name
            }
        }

        val positionalArgs = ArrayList<String>()
        var i = startingArgI
        var canParseOptions = true
        val invocations = mutableListOf<Pair<Option, OptionParse.Invocation>>()
        loop@ while (i <= argv.lastIndex) {
            val tok = argv[i]
            val normTok = tok.split('=')[0]
            when {
                canParseOptions && tok == "--" -> {
                    i += 1
                    canParseOptions = false
                }
                canParseOptions && ('=' in tok || normTok in longNames || normTok.matches(Regex("^--(.+)$"))) -> {
                    val (opt, result) = parseLongOpt(argv, tok, i, optionsByName)
                    invocations += opt to result.invocation
                    i += result.consumed
                }
                canParseOptions && tok.length >= 2 && tok.matches(Regex("^-(.+)$")) -> {
                    val (count, invokes) = parseShortOpt(argv, tok, i, optionsByName)
                    invocations += invokes
                    i += count
                }
                else -> {
                    positionalArgs += tok
                    i++
                }
            }
        }

        val invocationsByOption = invocations.groupBy({ it.first }, { it.second })

        try {
            val optionsInit = mutableListOf<Option>()
            invocationsByOption.forEach { (o, inv) ->
                o.finalize(command, inv)
                optionsInit += o
            }
            command.options.filter { it !in optionsInit }.forEach {
                it.finalize(command, emptyList())
            }

            parseArguments(positionalArgs, arguments.toList()).forEach { (a, v) -> a.finalize(command, v) }

            command.options.forEach { it.postValidate(command) }
            command.arguments.forEach { it.postValidate(command) }

        } catch (e: UsageException) {
            if (e.command == null) e.command = command
            throw e
        }
    }

    private fun Class<*>.getActualTypeArguments(index: Int): Class<*>? = with(genericSuperclass) {
        if (this is ParameterizedType) actualTypeArguments[index] as Class<*> else null
    }

    private fun parseLongOpt(
        tokens: List<String>,
        tok: String,
        index: Int,
        optionsByName: Map<String, Option>
    ): Pair<Option, OptionParse.Result> {
        val eqIndex = tok.indexOf('=')
        val (name, value) = if (eqIndex >= 0) {
            tok.substring(0, eqIndex) to tok.substring(eqIndex + 1)
        } else {
            tok to null
        }
        val option = optionsByName[name] ?: throw OptionNotFoundException(
            name,
            optionsByName.keys.filter { it.startsWith(name) })
        val result = option.parser.parseLong(option, name, tokens, index, value)
        return option to result
    }

    private fun parseShortOpt(
        tokens: List<String>,
        tok: String,
        index: Int,
        optionsByName: Map<String, Option>
    ): Pair<Int, List<Pair<Option, OptionParse.Invocation>>> {
        val prefix = tok[0].toString()
        val invocations = mutableListOf<Pair<Option, OptionParse.Invocation>>()
        for ((i, opt) in tok.withIndex()) {
            if (i == 0) continue

            val name = prefix + opt
            val option = optionsByName[name] ?: throw OptionNotFoundException(name)
            val result = option.parser.parseShort(option, name, tokens, index, i)
            invocations += option to result.invocation
            if (result.consumed > 0) return result.consumed to invocations
        }

        throw IllegalStateException("Error parsing short option ${tokens[index]}: no parser consumed value.")
    }

    private fun parseArguments(
        positionalArgs: List<String>,
        arguments: List<Argument>
    ): Map<Argument, List<String>> {
        val out = linkedMapOf<Argument, List<String>>().withDefault { listOf() }

        val endSize = arguments.asReversed()
            .takeWhile { it.size > 0 }
            .sumBy { it.size }

        var i = 0
        for (argument in arguments) {
            val remain = positionalArgs.size - i
            val consumed = when {
                argument.size <= 0 -> maxOf(if (argument.required) 1 else 0, remain - endSize)
                argument.size > 0 && !argument.required && remain == 0 -> 0
                else -> argument.size
            }

            if (consumed > remain) {
                if (remain == 0) throw ParameterIsMissingException(argument)
                else throw InvalidArgumentValueCountException(argument)
            }
            out[argument] = out.getValue(argument) + positionalArgs.subList(i, i + consumed)
            i += consumed
        }

        val excess = positionalArgs.size - i
        if (excess > 0) {
            throw UsageException(
                "Got unexpected extra argument${if (excess == 1) "" else "s"} " +
                        positionalArgs.slice(i..positionalArgs.lastIndex)
                            .joinToString(" ", "(", ")", 3)
            )
        }

        return out
    }
}