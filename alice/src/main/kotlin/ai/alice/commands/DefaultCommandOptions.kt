package ai.alice.commands

import ai.alice.api.CommandOptions

class DefaultCommandOptions(args: Collection<String>) : CommandOptions {
    override val options: Map<String, String> = args.filter { it.startsWith("--") }.map {
        it.split('=').let {
            Pair(it.first(), if (it.last() == it.first()) "" else it.last())
        }
    }.toMap()

    override val args: List<String> = args.filter { a -> options.keys.any { a.contains(it) } }

    override fun toMap(): Map<String, String> = args.mapIndexed(::Pair).toMap()
        .mapKeys { it.key.toString() }
        .toMutableMap().apply {
            putAll(options)
        }

    override fun containOption(key: String): Boolean = options.containsKey(key)

    override operator fun get(key: String): String? = options[key]

    override operator fun get(index: Int): String? = args[index]
}
