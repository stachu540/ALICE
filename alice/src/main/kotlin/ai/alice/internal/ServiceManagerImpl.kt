package ai.alice.internal

import ai.alice.api.Alice
import ai.alice.api.service.IFactory
import ai.alice.api.service.NamedProvider
import ai.alice.api.service.Provider
import ai.alice.api.service.ServiceManager
import ai.alice.internal.providers.EmptyNamedProvider
import ai.alice.internal.providers.EmptyProvider
import ai.alice.internal.providers.SingleNamedProvider
import ai.alice.internal.providers.SingleProvider

abstract class ServiceManagerImpl<T : Any> internal constructor(
    override val root: Alice,
    internal val services: MutableMap<String, T> = mutableMapOf()
) : ServiceManager<T>, Collection<T> by services.values {
    override fun get(id: String): NamedProvider<T> =
        services[id]?.let { SingleNamedProvider(id, it) } ?: EmptyNamedProvider(id)

    @Suppress("UNCHECKED_CAST")
    override fun <R : T> get(id: String, type: Class<R>): NamedProvider<R> =
        (services[id]?.run {
            if (type.isAssignableFrom(this::class.java))
                SingleNamedProvider(id, this as R)
            else EmptyNamedProvider<R>(id)
        } ?: EmptyNamedProvider(id))

    @Suppress("UNCHECKED_CAST")
    override fun <R : T> use(type: Class<R>): Provider<R> =
        services.values.firstOrNull { type.isAssignableFrom(it::class.java) }?.let { SingleProvider(it as R) } ?:
            EmptyProvider()

    override fun register(factory: IFactory<out T>) {
        services[factory.id] = factory.apply(root)
    }

    override fun <R : T> unregister(type: Class<R>): Boolean =
        services.toList().firstOrNull { type.isAssignableFrom(it.second::class.java) }
            ?.let { services.remove(it.first, it.second) } ?: false
}