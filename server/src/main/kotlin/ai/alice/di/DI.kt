package ai.alice.di

import kotlin.reflect.KClass

typealias Factory<T> = ClassContext.() -> T

object ClassOperator : ClassContext {
    private val factories = mutableMapOf<Class<*>, Factory<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(type: Class<T>): T = factories[factories.keys.firstOrNull { type.isAssignableFrom(it) }
        ?: throw ClassNotFoundException("Cannot find a type of register class: \"${type.canonicalName}\"")]?.let {
        (it as Factory<T>)(this)
    } ?: throw ClassNotFoundException("Current class is not register: \"${type.canonicalName}\"")

    fun <T> register(type: Class<T>, value: Factory<T>) {
        factories[type] = value
    }

    fun <T : Any> register(type: KClass<T>, value: Factory<T>) {
        register(type.java, value)
    }

    inline fun <reified T> register(noinline value: Factory<T>) {
        register(T::class.java, value)
    }
}

interface ClassContext {
    fun <T> get(type: Class<T>): T
    fun <T : Any> get(type: KClass<T>): T = get(type.java)

    fun <T> inject(type: Class<T>): Lazy<T> = lazy { get(type) }
    fun <T : Any> inject(type: KClass<T>): Lazy<T> = inject(type.java)
}

inline fun <reified T> ClassContext.get(): T = get(T::class.java)
inline fun <reified T> ClassContext.inject(): Lazy<T> = inject(T::class.java)