package ai.alice.internal.config

import ai.alice.api.Transformer
import ai.alice.api.config.ConfigurationProvider
import ai.alice.api.config.RootConfigurationProvider
import ai.alice.api.provider.ObjectProvider
import ai.alice.api.provider.Provider
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.File
import java.util.*

internal class RootParameterConfigurationProvider(
    val mapper: ObjectMapper, private val file: File
) : RootConfigurationProvider {

    var value: ObjectNode? = mapper.readTree(file) as ObjectNode
        private set

    override val isEmpty: Boolean
        get() = getOrNull()?.isEmpty(mapper.serializerProvider) ?: isPresent()

    override fun set(value: JsonNode?) {
        this.value = value as ObjectNode
        store()
    }

    override fun set(value: Provider<JsonNode>) {
        this.value = value.getOrNull() as ObjectNode
        store()
    }

    override fun get() = value ?: throw NoSuchElementException("Content is empty!")

    override fun getOrElse(defaultValue: JsonNode): JsonNode = value ?: defaultValue

    override fun getOrNull(): JsonNode? = value

    override fun isPresent(): Boolean = value != null

    override fun <T : Any> flatMap(transformer: Transformer<Provider<T>, JsonNode>): Provider<T> =
        if (isPresent()) transformer.transform(value!!) else ObjectProvider(null)

    override fun <T : Any> map(transformer: Transformer<T, JsonNode>): Provider<T> =
        ObjectProvider(if (isPresent()) transformer.transform(value!!) else null)

    override fun get(key: String): ConfigurationProvider =
        ParameterConfigurationProvider(key, mapper, this)

    override fun <T> `as`(type: Class<T>): Provider<T> =
        ObjectProvider(if (isPresent()) mapper.treeToValue(value!!, type) else null)

    override fun set(key: String, value: JsonNode) {
        if (isPresent()) {
            (this.value as ObjectNode).set(key, value)
            store()
        }
    }

    override fun set(key: String, value: Provider<Any>) {
        val v = value.map {
            when (it) {
                is JsonNode -> it
                else -> mapper.valueToTree(it)
            }
        }.getOrElse(NullNode.instance)

        set(key, v)
    }

    fun store() {
        if (isPresent()) {
            mapper.writeValue(file, this.value)
        }
    }

    override fun toString(): String =
        "RootConfig[${value!!.textValue() ?: "<null>"}]"
}
