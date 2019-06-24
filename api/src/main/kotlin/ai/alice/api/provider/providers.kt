package ai.alice.api.provider

import ai.alice.api.Action
import ai.alice.api.Transformer
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class NamedObjectProviderSpec<T> internal constructor(override val name: String, value: T?) :
    ObjectProvider<T>(value), NamedObjectProvider<T> {
    override fun configure(action: Action<T>) {
        if (isPresent()) action.execute(value!!)
    }
}

open class ObjectProvider<S>(protected var value: S?) : Provider<S> {

    override fun get(): S = value ?: throw NoSuchElementException("Content is empty!")

    override fun getOrElse(defaultValue: S): S = value ?: defaultValue

    override fun getOrNull(): S? = value

    override fun isPresent(): Boolean = value != null

    override fun <T : Any> flatMap(transformer: Transformer<Provider<T>, S>): Provider<T> =
        if (isPresent()) transformer.transform(value!!) else ObjectProvider(null)

    override fun <T : Any> map(transformer: Transformer<T, S>): Provider<T> =
        ObjectProvider(if (isPresent()) transformer.transform(value!!) else null)

    override fun set(value: S?) {
        this.value = value
    }

    override fun set(value: Provider<S>) {
        this.value = value.getOrNull()
    }
}

interface Provider<T> : ReadWriteProperty<Any, T?> {
    @Throws(NoSuchElementException::class)
    fun get(): T

    fun getOrElse(defaultValue: T): T
    fun getOrNull(): T?
    fun isPresent(): Boolean
    fun <S : Any> flatMap(transformer: Transformer<Provider<S>, T>): Provider<S>
    fun <S : Any> flatMap(transformer: (T) -> Provider<S>): Provider<S> = flatMap(object :
        Transformer<Provider<S>, T> {
        override fun transform(`in`: T): Provider<S> = transformer(`in`)
    })

    fun <S : Any> map(transformer: Transformer<S, T>): Provider<S>
    fun <S : Any> map(transformer: (T) -> S): Provider<S> = map(object :
        Transformer<S, T> {
        override fun transform(`in`: T): S = transformer(`in`)
    })

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T? = getOrNull()
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {

    }

    fun set(value: T?)
    fun set(value: Provider<T>)

    fun default(defaultValue: T): ReadOnlyProperty<Any, T> = object :
        ReadOnlyProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T = getOrElse(defaultValue)
    }
}

interface NamedObjectProvider<T> : Provider<T> {
    val name: String
    fun configure(action: Action<T>)

    companion object {
        operator fun <T : Any> invoke(name: String, value: T?): NamedObjectProvider<T> =
            NamedObjectProviderSpec(name, value)
    }
}