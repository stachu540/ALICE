package ai.alice.api

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface ICommand<E> {
    val name: String
    val alias: Collection<String>
    val description: String?
    val usage: String?
    val access: CommandAccess<E>
    val category: CommandCategory

    suspend fun execute(event: E, options: CommandOptions)
}

typealias CommandAccess<E> = (E) -> Boolean

interface CommandOptions {
    val options: Map<String, String>
    val args : Collection<String>

    fun toMap(): Map<String, String>

    fun hasOption(key: String? = null): ReadOnlyProperty<Any, Boolean> = object : ReadOnlyProperty<Any, Boolean> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = containOption(key ?: property.name)
    }

    fun option(key: String? = null): ReadOnlyProperty<Any, String?> = object : ReadOnlyProperty<Any, String?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): String? = getOption(key ?: property.name)
    }

    fun argument(index: Int): ReadOnlyProperty<Any, String?> = object : ReadOnlyProperty<Any, String?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): String? = getArgument(index)
    }

    fun containOption(key: String): Boolean
    fun getOption(key: String): String?
    fun getArgument(index: Int): String?
}