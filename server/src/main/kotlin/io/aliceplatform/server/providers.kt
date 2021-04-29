package io.aliceplatform.server

import io.aliceplatform.api.Transformer
import io.aliceplatform.api.engine.Engine
import io.aliceplatform.api.engine.EngineProvider
import io.aliceplatform.api.modules.Module
import io.aliceplatform.api.modules.ModuleProvider
import io.aliceplatform.api.objects.ObjectCollection
import io.aliceplatform.api.objects.Provider
import java.util.concurrent.atomic.AtomicBoolean

class EngineProviderImpl(override val alice: DefaultAliceInstance) : EngineProvider {
  private val engines = mutableMapOf<String, Engine<*, *, *>>()

  override fun <F : Engine.Factory<C>, C : Engine.Config> install(factory: F, config: C.() -> Unit) {
    engines[factory.name] = factory.init(alice, config)
  }

  override fun named(name: String): Provider<Engine<*, *, *>> =
    alice.objects.of {
      engines[name] ?: throw NullPointerException("No Engine have been installed in this name!")
    }

  override fun <R : Engine<*, *, *>> named(name: String, type: Class<R>): Provider<R> =
    named(name).map { type.cast(it) }

  override val size: Int
    get() = engines.size
  override val isEmpty: Boolean
    get() = engines.isEmpty()

  override fun <R : Engine<*, *, *>> ofType(type: Class<R>): Provider<R> =
    alice.objects.of {
      engines.values.firstOrNull { type.isInstance(it) }?.let { type.cast(it) }
        ?: throw NullPointerException("No Engine have been installed in this type!")
    }

  override fun <R : Any> map(transformer: Transformer<Engine<*, *, *>, R>): ObjectCollection<R> =
    alice.objects.list(engines.values.map(transformer::transform))

  override fun <R : Any> flatMap(transformer: Transformer<Engine<*, *, *>, Iterable<R>>): ObjectCollection<R> =
    alice.objects.list(engines.values.flatMap(transformer::transform))

  override fun iterator(): Iterator<Engine<*, *, *>> =
    engines.values.iterator()

  override fun run() {
    engines.values.forEach {
      it.run()
    }
  }

  override fun close() {
    engines.values.forEach {
      it.close()
    }
  }
}

class ModuleProviderImpl(override val alice: DefaultAliceInstance) : ModuleProvider {
  private val active = AtomicBoolean(false)
  val modulesLoaded = mutableMapOf<String, Module<*>>()
  val modulesToApply = mutableMapOf<String, () -> Module<*>>()

  override fun <TConfig> install(module: Module<TConfig>, configure: TConfig.() -> Unit) {
    if (active.get()) {
      modulesLoaded[module.name] = module.also { it.apply(alice, module.configure(configure)) }
    } else {
      modulesToApply[module.name] = { module.also { it.apply(alice, module.configure(configure)) } }
    }
  }

  override fun named(name: String): Provider<Module<*>> =
    alice.objects.of { modulesLoaded[name] ?: throw NullPointerException("Module of name $name is not exist") }

  override fun <R : Module<*>> named(name: String, type: Class<R>): Provider<R> =
    named(name).map { type.cast(it) }

  override val size: Int
    get() = (modulesLoaded + modulesToApply).size
  override val isEmpty: Boolean
    get() = (modulesLoaded + modulesToApply).isEmpty()

  override fun <R : Module<*>> ofType(type: Class<R>): Provider<R> {
    TODO("Not yet implemented")
  }

  override fun <R : Any> map(transformer: Transformer<Module<*>, R>): ObjectCollection<R> {
    TODO("Not yet implemented")
  }

  override fun <R : Any> flatMap(transformer: Transformer<Module<*>, Iterable<R>>): ObjectCollection<R> {
    TODO("Not yet implemented")
  }

  override fun iterator(): Iterator<Module<*>> =
    modulesLoaded.values.iterator()

  internal fun init() {
    active.set(true)
    if (modulesToApply.isNotEmpty()) {
      for ((name, mod) in modulesToApply) {
        modulesLoaded[name] = mod()
        modulesToApply.remove(name)
      }
    }
  }
}
