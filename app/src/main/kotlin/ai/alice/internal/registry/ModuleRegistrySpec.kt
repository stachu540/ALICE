package ai.alice.internal.registry

import ai.alice.api.UnknownDomainObjectException
import ai.alice.api.engine.IEngine
import ai.alice.api.module.IModule
import ai.alice.api.module.ModuleRegistry
import ai.alice.api.provider.NamedObjectProvider
import ai.alice.internal.AliceEngine
import java.util.*

class ModuleRegistrySpec(val rootEngine: AliceEngine) : ModuleRegistry {
    override val asMap: SortedMap<String, IModule<*>> = sortedMapOf()

    override fun <TEngine : IEngine> getByEngine(type: Class<TEngine>): ModuleRegistry =
        filter { type.isAssignableFrom(it.javaClass) } as ModuleRegistry

    override fun named(name: String): NamedObjectProvider<IModule<*>> =
        NamedObjectProvider(name, getByName(name))

    @Suppress("UNCHECKED_CAST")
    override fun <S : IModule<*>> named(name: String, type: Class<S>): NamedObjectProvider<S> =
        named(name).flatMap {
            if (type.isAssignableFrom(it.javaClass)) NamedObjectProvider(name, it as S)
            else throw UnknownDomainObjectException("Cannot cast to '${type.simpleName}' when is '${it.javaClass.simpleName}'")
        } as NamedObjectProvider<S>
}
