package io.aliceplatform.api.engine

import io.aliceplatform.api.Alice
import io.aliceplatform.api.AliceObjectOperator
import io.aliceplatform.api.Consumer
import io.aliceplatform.api.engine.command.Command
import io.aliceplatform.api.engine.command.CommandEvent
import io.aliceplatform.api.engine.command.CommandProvider
import io.aliceplatform.api.objects.NamedObjectCollection

/**
 * Chat Engine Provider
 */
interface EngineProvider : NamedObjectCollection<Engine<*, *, *>>, AliceObjectOperator {
  /**
   * Initialize chat engine before starting them
   */
  fun <F : Engine.Factory<C>, C : Engine.Config> install(factory: F, config: C.() -> Unit = {})
}

/**
 * The Chat Engine
 */
interface Engine<TRoot, TEvent, TCEvent : CommandEvent<TEvent>> : AliceObjectOperator {
  val root: TRoot
  val provider: CommandProvider<TEvent, TCEvent, Engine<TRoot, TEvent, TCEvent>>

  fun <T : TEvent> onEvent(type: Class<T>, consumer: Consumer<T>)
  fun registerCommand(command: Command<TEvent, TCEvent>)
  fun unregisterCommand(name: String, aliased: Boolean = false)

  interface Factory<TConfig : Config> {
    val name: String
    fun init(alice: Alice, config: TConfig.() -> Unit): Engine<*, *, *>
  }

  interface Config {
    val token: String
  }
}
