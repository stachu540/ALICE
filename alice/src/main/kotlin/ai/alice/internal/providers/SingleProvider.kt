package ai.alice.internal.providers

import ai.alice.api.service.Provider
import kotlin.reflect.KProperty

class SingleProvider<T>(private val value: T) : Provider<T> {
    override fun get(): T = value

    override fun getOrNull(): T? = value

    override fun getOrElse(default: T): T = value

    override fun apply(consumer: T.() -> Unit) {
        value.apply(consumer)
    }

    override fun <R> map(mapper: T.() -> R?): Provider<R> =
        value.run(mapper)?.let { SingleProvider(it) } ?: EmptyProvider()

    override fun getValue(thisRef: Any, property: KProperty<*>): T = value
}