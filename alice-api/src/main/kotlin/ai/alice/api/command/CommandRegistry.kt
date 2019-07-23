package ai.alice.api.command

import ai.alice.api.service.IEngine
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// TODO: Annotated Commands

interface CommandRegistry<E : IEngine<*, *>, V : Any> {
    val engine: E
    val custom: CustomCommandRegistry<E, *>
    fun register(command: ICommand<V>)
    fun unregister(command: String)
}

interface CustomCommandRegistry<E : IEngine<*, *>, C> {
    val engine: E
    suspend fun rename(old: String, new: String)
    suspend fun register(command: String, component: C)
    suspend fun unregister(command: String)
}

interface ICommand<E> {
    val name: String
    val description: String?
    val aliases: Collection<String>
    suspend fun isAccessible(event: E): Boolean
    suspend fun execute(event: E, args: List<String>, options: Map<String, String?>)
}

abstract class AbstractCommand<E>(
    override val name: String,
    override val description: String? = null,
    override val aliases: Collection<String> = setOf()
) : ICommand<E> {
    @PublishedApi
    internal val args = mutableListOf<String>()
    @PublishedApi
    internal val options = mutableMapOf<String, String>()
    @PublishedApi
    internal val mapper = ObjectMapper().findAndRegisterModules()

    protected fun addArgs(vararg args: String) = apply {
        this.args.addAll(args)
    }

    protected fun addArgs(args: Collection<String>) = apply {
        this.args += args
    }

    protected fun addOptions(vararg options: Pair<String, String>) = apply {
        this.options.putAll(options)
    }

    protected fun addOption(key: String, value: String) = apply {
        this.options[key] = value
    }

    inline fun <reified E> argument(position: Int): ReadOnlyProperty<Any, E> =
        object : ReadOnlyProperty<Any, E> {
            override fun getValue(thisRef: Any, property: KProperty<*>): E =
                mapper.readValue(args[position])
        }

    inline fun <reified E> option(name: String? = null): ReadOnlyProperty<Any, E?> =
        object : ReadOnlyProperty<Any, E?> {
            override fun getValue(thisRef: Any, property: KProperty<*>): E? =
                options[name ?: property.name]?.let { mapper.readValue<E>(it) }
        }
}