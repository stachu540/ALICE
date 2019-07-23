package ai.alice.api.service

import java.util.function.Consumer
import java.util.function.Function
import kotlin.properties.ReadOnlyProperty

interface Provider<T> : ReadOnlyProperty<Any, T> {
    @Throws(IllegalAccessException::class)
    fun get(): T

    fun getOrNull(): T?

    fun getOrElse(default: T): T

    fun apply(consumer: T.() -> Unit): Unit
    fun <R> map(mapper: T.() -> R?): Provider<R>

    fun apply(consumer: Consumer<T>) = apply { consumer.accept(this) }
    fun <R> map(mapper: Function<T, R?>): Provider<R> = map { mapper.apply(this) }
}

interface NamedProvider<T> : Provider<T> {
    val name: String
}