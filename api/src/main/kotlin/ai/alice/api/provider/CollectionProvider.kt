package ai.alice.api.provider

import ai.alice.api.AliceObject
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Objects
import kotlin.NoSuchElementException
import kotlin.coroutines.resume
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.safeCast

interface CollectionProvider<T : Any> : Iterable<T>, AliceObject {
    val size: Int
    fun isEmpty(): Boolean
    fun contains(type: KClass<T>): Boolean
    operator fun <R : T> get(type: KClass<R>): Provider<R>
}

inline fun <reified R : T, T : Any> CollectionProvider<T>.getting(): Provider<R> = get(R::class)

interface Provider<T> : ReadOnlyProperty<Any?, T> {
    val isPresent: Boolean

    @Throws(NoSuchElementException::class)
    fun get(): T
    fun getOrNull(): T?
    fun getOrElse(other: T): T = getOrNull() ?: other
    fun <X : Throwable> getOrThrow(exception: () -> X): T = getOrNull() ?: throw exception()
    fun ifPresent(action: (T) -> Unit)
    fun <R : Any> castTo(type: KClass<R>): Provider<R>

    fun <R> map(mapper: (T) -> R): Provider<R>
    fun <R> flatMap(mapper: (T) -> Provider<R>): Provider<R>

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()

    suspend fun await(): T = suspendCancellableCoroutine {
        try {
            it.resume(get())
        } catch (e: NoSuchElementException) {
            it.cancel(e)
        }
    }

    suspend fun awaitOrNull(): T? = suspendCancellableCoroutine {
        it.resume(getOrNull())
    }
}

fun <T> provide(value: T?): Provider<T> = provide(value) { "No value presents" }
fun <T> provide(value: T?, lazyMessage: () -> String = { "No value presents" }): Provider<T> =
    DelegatedProvider(value, NoSuchElementException(lazyMessage()))

fun <T> provide(value: T?, exception: Exception? = null): Provider<T> =
    DelegatedProvider(value, NoSuchElementException("No value presents").also {
        if (exception != null) {
            it.addSuppressed(exception)
        }
    })

private class DelegatedProvider<T>(private val value: T?, private val exception: Exception) : Provider<T> {
    override val isPresent: Boolean
        get() = value != null

    @Throws(NoSuchElementException::class)
    override fun get(): T = getOrThrow { exception }

    override fun getOrNull(): T? = value

    override fun ifPresent(action: (T) -> Unit) {
        value?.let(action)
    }

    override fun <R> map(mapper: (T) -> R): Provider<R> =
        if (value == null) provide(null, exception) else provide(value.let(mapper), exception)

    override fun <R> flatMap(mapper: (T) -> Provider<R>): Provider<R> =
        value?.let(mapper) ?: provide<R>(null, exception)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other is Provider<*> -> Objects.equals(value, (other as DelegatedProvider<*>).value)
        else -> false
    }

    override fun hashCode(): Int {
        return Objects.hashCode(value)
    }

    override fun toString(): String {
        return "Provider" + (if (value != null) "[$value]" else ".empty")
    }

    @ExperimentalStdlibApi
    override fun <R : Any> castTo(type: KClass<R>): Provider<R> =
        provide(
            type.safeCast(value),
            if (value == null) exception else ClassCastException("Cannot cast to ${type.qualifiedName}")
        )
}