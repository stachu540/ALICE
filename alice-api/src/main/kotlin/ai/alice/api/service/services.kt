package ai.alice.api.service

import ai.alice.api.Alice
import ai.alice.api.RootComponent
import kotlin.reflect.KClass

interface ServiceManager<T : Any> : Collection<T>, RootComponent {
    fun get(id: String): NamedProvider<T>
    fun <R : T> get(id: String, type: Class<R>): NamedProvider<R>
    fun <R : T> get(id: String, type: KClass<R>): NamedProvider<R> = get(id, type.java)

    fun <R : T> use(type: Class<R>): Provider<R>
    fun <R : T> use(type: KClass<R>): Provider<R> = use(type.java)

    fun register(factory: IFactory<out T>)
    fun register(id: String, factory: (Alice) -> T) = register(SimpleFactory(id) {
        factory(
            it
        )
    })

    fun <R : T> unregister(type: Class<R>): Boolean
    fun <R : T> unregister(type: KClass<R>): Boolean = unregister(type.java)
}

interface IFactory<T> {
    val id: String
    fun apply(root: Alice): T
}

class SimpleFactory<T>(
    override val id: String,
    private val apply: (Alice) -> T
) : IFactory<T> {
    override fun apply(root: Alice): T = this.apply.invoke(root)
}