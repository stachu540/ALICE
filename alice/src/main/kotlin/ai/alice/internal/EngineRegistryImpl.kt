package ai.alice.internal

import ai.alice.api.Alice
import ai.alice.api.service.EngineRegistry
import ai.alice.api.service.IEngine

class EngineRegistryImpl(override val root: Alice) : ServiceManagerImpl<IEngine<*, *>>(root), EngineRegistry {
    override suspend fun startAll() {
        services.values.forEach { it.start() }
    }

    override suspend fun <E : IEngine<*, *>> start(type: Class<E>) {
        services.values.firstOrNull { type.isAssignableFrom(it::class.java) }?.start()
    }

    override suspend fun stopAll(force: Boolean) {
        services.values.forEach { it.stop(force) }
    }

    override suspend fun <E : IEngine<*, *>> stop(force: Boolean, type: Class<E>) {
        services.values.firstOrNull { type.isAssignableFrom(it::class.java) }?.stop(force)
    }
}
