package ai.alice.api

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LateInit<T>(private val lazyMessage: () -> String) :
    ReadWriteProperty<Any, T> {
    private var _value: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T =
        _value ?: throw NoSuchElementException(lazyMessage())

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        _value = value
    }
}