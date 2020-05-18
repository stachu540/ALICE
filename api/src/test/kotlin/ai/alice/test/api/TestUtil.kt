package ai.alice.test.api

import ai.alice.api.engine.command.Command
import ai.alice.api.engine.command.CommandAccess
import ai.alice.api.engine.command.CommandEvent
import ai.alice.api.engine.command.CommandGroup
import ai.alice.test.api.synthetic.SyntheticCommand

object TestUtil {
    fun <CE : CommandEvent<TMessage, *>, TMessage> createCommand(
        name: String,
        alias: Array<String> = emptyArray(),
        description: String? = null,
        accessor: CommandAccess<CE> = { true },
        group: CommandGroup<CE> = ai.alice.api.engine.command.group("uncategorized"),
        exec: suspend Command<CE>.(CE) -> Unit
    ): Command<CE> = SyntheticCommand(
        name, alias, description, accessor, group, exec
    )
}