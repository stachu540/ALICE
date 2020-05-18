package ai.alice.api.engine.command.cli.params

data class ValueWithDefault<out T>(val explicit: T?, val default: T) {
    val value: T get() = explicit ?: default
}