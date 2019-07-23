package ai.alice.internal.providers

import ai.alice.api.service.NamedProvider
import ai.alice.api.service.Provider
import kotlin.reflect.KProperty

class SingleNamedProvider<T>(
    override val name: String,
    private val value: T
) : NamedProvider<T> {
    override fun get(): T = value

    override fun getOrNull(): T? = value

    override fun getOrElse(default: T): T = value

    override fun apply(consumer: T.() -> Unit) {
        this.value.apply(consumer)
    }

    override fun <R> map(mapper: T.() -> R?): Provider<R> =
        this.value.run(mapper)?.let { SingleProvider(it) } ?: EmptyProvider()


    override fun getValue(thisRef: Any, property: KProperty<*>): T = value
}