package ai.alice.api.module

import ai.alice.api.NamedObjectCollection
import ai.alice.api.engine.IEngine
import kotlin.reflect.KClass

interface ModuleRegistry : NamedObjectCollection<IModule<*>> {
    fun <TEngine : IEngine> getByEngine(type: Class<TEngine>): ModuleRegistry
    fun <TEngine : IEngine> getByEngine(type: KClass<TEngine>): ModuleRegistry = getByEngine(type.java)
}

interface IModule<E : IEngine> : Comparable<IModule<IEngine>> {
    fun apply(target: E)
}

inline fun <reified TEngine : IEngine> ModuleRegistry.getByEngine(): ModuleRegistry =
    getByEngine(TEngine::class)
