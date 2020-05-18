package ai.alice.api.engine.command.cli.params.types

import ai.alice.api.engine.command.cli.params.*

private fun mvar(choices: Iterable<String>): String {
    return choices.joinToString("|", prefix = "[", postfix = "]")
}

private fun errorMessage(choice: String, choices: Map<String, *>): String {
    return "invalid choice: $choice. (choose from ${choices.keys.joinToString(", ")})"
}

fun <T : Any> RawArgument.choice(choices: Map<String, T>): ArgumentProcessor<T, T> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return convert { choices[it] ?: fail(errorMessage(it, choices)) }
}

fun <T : Any> RawArgument.choice(vararg choices: Pair<String, T>) =
    choice(choices.toMap())

fun <T : Any> RawArgument.choice(vararg choices: String) =
    choice(choices.associateBy { it })

inline fun <reified T : Enum<T>> RawArgument.enum(key: (T) -> String = { it.name }) =
    choice(enumValues<T>().associateBy(key))

fun <T : Any> RawOption.choice(choices: Map<String, T>): NullableOption<T, T> {
    require(choices.isNotEmpty()) { "Must specify at lease one choice" }
    return convert { choices[it] ?: fail(errorMessage(it, choices)) }
}

fun <T : Any> RawOption.choice(vararg choices: Pair<String, T>) =
    choice(choices.toMap())

fun <T : Any> RawOption.choice(vararg choices: String) =
    choice(choices.associateBy { it })

inline fun <reified T : Enum<T>> RawOption.enum(key: (T) -> String = { it.name }) =
    choice(enumValues<T>().associateBy(key))
