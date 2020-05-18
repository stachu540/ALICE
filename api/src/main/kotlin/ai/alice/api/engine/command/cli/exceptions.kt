package ai.alice.api.engine.command.cli

import ai.alice.api.engine.command.Command
import ai.alice.api.engine.command.cli.params.Argument
import ai.alice.api.engine.command.cli.params.Option
import ai.alice.api.exception.AliceCommandException

class PrintUsage(val command: Command<*>) : AliceCommandException()
class PrintMessage(message: String) : AliceCommandException(message)

open class UsageException private constructor(
    val text: String? = null,
    var paramName: String? = null,
    var option: Option? = null,
    var argument: Argument? = null,
    var command: Command<*>? = null
) : AliceCommandException() {
    constructor(text: String, paramName: String? = null, command: Command<*>? = null) : this(
        text,
        paramName,
        null,
        null,
        command
    )

    constructor(text: String, argument: Argument, command: Command<*>? = null) : this(
        text,
        null,
        null,
        argument,
        command
    )

    constructor(text: String, option: Option, command: Command<*>? = null) : this(text, null, option, null, command)

    override val message: String?
        get() = formatMessage()

    protected open fun formatMessage() = text ?: ""

    protected fun inferParamName() = when {
        paramName != null -> paramName!!
        option != null -> option?.names?.maxBy { it.length } ?: ""
        argument != null -> argument!!.name
        else -> ""
    }
}

open class WrongParameterValueException : UsageException {
    constructor(text: String, command: Command<*>? = null) : super(text, null, command)
    constructor(text: String, paramName: String, command: Command<*>? = null) : super(text, paramName, command)
    constructor(text: String, argument: Argument, command: Command<*>? = null) : super(text, argument, command)
    constructor(text: String, option: Option, command: Command<*>? = null) : super(text, option, command)

    override fun formatMessage(): String {
        if (inferParamName().isEmpty()) return "Invalid value: $text"
        return "Invalid value for \"${inferParamName()}\": $text"
    }
}

open class ParameterIsMissingException : UsageException {
    constructor(argument: Argument, command: Command<*>? = null) : super("", argument, command) {
        this.paramType = "argument"
    }

    constructor(option: Option, command: Command<*>? = null) : super("", option, command) {
        this.paramType = "option"
    }

    private val paramType: String

    override fun formatMessage(): String {
        return "Missing $paramType \"${inferParamName()}\"."
    }
}

open class OptionNotFoundException(
    protected val givenName: String,
    protected val possibilities: List<String> = emptyList(),
    command: Command<*>? = null
) : UsageException("", command = command) {

    override fun formatMessage(): String {
        return "no such option: \"$givenName\"." + when {
            possibilities.size == 1 -> " Did you mean \"${possibilities[0]}\"?"
            possibilities.size > 1 -> possibilities.joinToString(
                prefix = " (Possible options: ", postfix = ")"
            )
            else -> ""
        }
    }
}

open class InvalidOptionValueCountException(
    option: Option,
    private val givenName: String,
    command: Command<*>? = null
) : UsageException("", option, command) {
    override fun formatMessage(): String {
        return when (option!!.size) {
            0 -> "$givenName option does not take a value"
            1 -> "$givenName option requires an argument"
            else -> "$givenName option requires ${option!!.size} arguments"
        }
    }
}


open class InvalidArgumentValueCountException(
    argument: Argument,
    command: Command<*>? = null
) : UsageException("", argument, command) {
    override fun formatMessage(): String {
        return "argument ${inferParamName()} takes ${argument!!.size} values"
    }
}