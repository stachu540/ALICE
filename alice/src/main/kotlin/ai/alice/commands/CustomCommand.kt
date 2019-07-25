package ai.alice.commands

import ai.alice.api.CommandCategory
import ai.alice.api.ICommand

interface CustomCommand<T> : ICommand<T> {
    override val category: CommandCategory
        get() = CommandCategory.CUSTOM
    val count: Int
}