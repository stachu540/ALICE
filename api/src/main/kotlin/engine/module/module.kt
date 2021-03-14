package ai.alice.api.engine.module

import ai.alice.api.engine.Engine
import ai.alice.api.objects.NamedObjectContainer

interface ModuleContainer<TEngine : Engine<*, *, *, TEngine>> : NamedObjectContainer<Module<TEngine>> {
  val engine: TEngine

  fun apply(module: Module<TEngine>)
}

interface Module<TEngine : Engine<*, *, *, TEngine>> {
  fun apply(engine: TEngine)
}
