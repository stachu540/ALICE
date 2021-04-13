package io.aliceplatform.api.engine

import io.aliceplatform.api.Alice
import io.aliceplatform.api.AliceObjectOperator
import io.aliceplatform.api.Consumer
import io.aliceplatform.api.engine.command.Command
import io.aliceplatform.api.engine.command.CommandEvent
import io.aliceplatform.api.engine.command.CommandProvider
import io.aliceplatform.api.objects.NamedObjectCollection

interface EngineProvider : NamedObjectCollection<Engine<*, *, *>>, AliceObjectOperator {
  fun <F : Engine.Factory> install(factory: F)
}

interface Engine<TRoot, TEvent, TCEvent : CommandEvent<TEvent>> {
  val root: TRoot
  val provider: CommandProvider<TEvent, TCEvent, Engine<TRoot, TEvent, TCEvent>>

  fun <T : TEvent> onEvent(type: Class<T>, consumer: Consumer<T>)
  fun registerCommand(command: Command<TEvent, TCEvent>)
  fun unregisterCommand(name: String, aliased: Boolean = false)

  interface Factory {
    fun init(alice: Alice, config: Config): Engine<*, *, *>
  }

  interface Config {
    val token: String
  }
}
