package ai.alice.api.config

import ai.alice.api.provider.NamedObjectProvider
import ai.alice.api.provider.Provider
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import kotlin.reflect.KClass

interface RootConfigurationProvider : Provider<JsonNode> {
    val isEmpty: Boolean

    val asString
        get() = map { it.textValue() }

    val asInteger
        get() = map { it.intValue() }

    val asBoolean
        get() = map { it.booleanValue() }

    val asLong
        get() = map { it.longValue() }

    val asDouble
        get() = map { it.doubleValue() }

    fun get(key: String): ConfigurationProvider

    fun set(key: String, value: JsonNode)
    fun set(key: String, value: String) = set(key, TextNode(value))
    fun set(key: String, value: Int) = set(key, IntNode(value))
    fun set(key: String, value: Boolean) = set(key, BooleanNode.valueOf(value))
    fun set(key: String, value: Long) = set(key, LongNode(value))
    fun set(key: String, value: Double) = set(key, DoubleNode(value))

    fun set(key: String, value: Provider<Any>)

    fun <T> `as`(type: Class<T>): Provider<T>
    fun <T : Any> `as`(type: KClass<T>): Provider<T> = `as`(type.java)
}

interface ConfigurationProvider : NamedObjectProvider<JsonNode> {
    val root: RootConfigurationProvider
    val isEmpty: Boolean

    val asString
        get() = map { it.textValue() }

    val asInteger
        get() = map { it.intValue() }

    val asBoolean
        get() = map { it.booleanValue() }

    val asLong
        get() = map { it.longValue() }

    val asDouble
        get() = map { it.doubleValue() }

    fun get(key: String): ConfigurationProvider

    fun <T : Any> `as`(type: Class<T>): Provider<T>
    fun <T : Any> `as`(type: KClass<T>): Provider<T> = `as`(type.java)
}

inline fun <reified T : Any> ConfigurationProvider.`as`() = `as`(T::class)