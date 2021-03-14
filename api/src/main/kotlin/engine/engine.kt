package ai.alice.api.engine

import ai.alice.api.Alice
import ai.alice.api.AliceInstanceObject
import ai.alice.api.Consumer
import ai.alice.api.engine.command.Command
import ai.alice.api.engine.command.CommandEvent
import ai.alice.api.engine.command.CommandProvider
import ai.alice.api.engine.module.ModuleContainer
import ai.alice.api.objects.NamedObjectContainer

interface EngineContainer : NamedObjectContainer<Engine<*, *, *, *>> {
  fun <TFactory : Engine.Factory<TConfig, *>, TConfig : Engine.Config> install(factory: TFactory, configure: TConfig.() -> Unit)
}

interface Engine<TEvent, TCEvent : CommandEvent<TEvent>, TCommand : Command<TCEvent, TEvent>, TEngine : Engine<TEvent, TCEvent, TCommand, TEngine>> :
  Consumer<TEvent>, AliceInstanceObject {
  val modules: ModuleContainer<TEngine>
  val commands: CommandProvider<TEvent, TCEvent, TCommand, TEngine>

  interface Factory<TConfig : Config, TEngine : Engine<*, *, *, TEngine>> {
    val name: String

    fun configure(configure: TConfig.() -> Unit): TConfig
    fun register(alice: Alice, config: TConfig): TEngine
  }

  interface Config {
    val token: String

  }
}
