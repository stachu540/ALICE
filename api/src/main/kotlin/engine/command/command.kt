package ai.alice.api.engine.command

import ai.alice.api.Consumer
import ai.alice.api.Predicate
import ai.alice.api.engine.Engine
import kotlin.jvm.Throws

interface CommandProvider<TEvent, TCEvent : CommandEvent<TEvent>, TCommand : Command<TCEvent, TEvent>, TEngine : Engine<TEvent, TCEvent, TCommand, TEngine>> :
  Consumer<TEvent> {
  val engine: TEngine
  fun register(command: TCommand)
  fun unregister(command: String, aliased: Boolean = false)
}

interface Command<TCEvent : CommandEvent<TEvent>, TEvent> {
  val name: String
  val aliases: Array<String>
  val description: String?
  val accessor: Predicate<TCEvent>
  val group: Group<TCEvent, TEvent>

  @Throws(Exception::class)
  fun execute(event: TCEvent)

  interface Group<TCEvent : CommandEvent<TEvent>, TEvent> : Predicate<TCEvent> {
    val order: Int
    val name: String
    val displayName: String
  }
}

interface CommandEvent<TEvent> {
  val raw: TEvent
  val message: String
  val command: String
  val argv: Array<String>
}
