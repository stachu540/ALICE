package ai.alice.internal.providers

import ai.alice.api.service.Provider
import kotlin.reflect.KProperty

class EmptyProvider<T> : Provider<T> {
    override fun get(): T = throw IllegalAccessException("Provided content is empty")

    override fun getOrNull(): T? = null

    override fun getOrElse(default: T): T = default

    override fun apply(consumer: T.() -> Unit) {}

    override fun <R> map(mapper: T.() -> R?): Provider<R> = EmptyProvider()

    override fun getValue(thisRef: Any, property: KProperty<*>): T = get()
}