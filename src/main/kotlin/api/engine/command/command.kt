package io.aliceplatform.api.engine.command

import io.aliceplatform.api.AliceObject
import io.aliceplatform.api.Predicate
import io.aliceplatform.api.engine.Engine

interface CommandProvider<TEvent, TCEvent : CommandEvent<TEvent>, TEngine : Engine<*, TEvent, TCEvent>> : AliceObject {
  val engine: TEngine
  val prefixManager: PrefixManager<TEvent>
  val names: Set<String>
  val commands: Set<Command<TEvent, TCEvent>>

  fun register(command: Command<TEvent, TCEvent>)
  fun unregister(name: String, aliased: Boolean = false)

  fun handle(event: TEvent)
}

interface PrefixManager<TEvent> {
  val default: String
  fun get(event: TEvent): String
}

interface CommandEvent<out TEvent> {
  val root: TEvent
  val message: String
  val command: String
  val argv: Array<String>
  val prefix: String

  val sender: Any
  val channel: Any
}

interface Command<TEvent, TCEvent : CommandEvent<TEvent>> {
  val name: String
  val alias: Array<String>
  val description: String?
  val access: Predicate<TCEvent>
  val group: Group<TEvent, TCEvent>

  @Throws(Exception::class)
  fun execute(event: TCEvent)

  interface Group<TEvent, TCEvent : CommandEvent<TEvent>> : Predicate<TCEvent> {
    val name: String
    val displayName: String
  }
}
