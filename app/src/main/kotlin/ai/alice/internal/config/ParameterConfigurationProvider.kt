package ai.alice.internal.config

import ai.alice.api.Action
import ai.alice.api.Transformer
import ai.alice.api.config.ConfigurationProvider
import ai.alice.api.config.RootConfigurationProvider
import ai.alice.api.provider.ObjectProvider
import ai.alice.api.provider.Provider
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode


class ParameterConfigurationProvider internal constructor(
    override val name: String,
    protected val mapper: ObjectMapper,
    override val root: RootConfigurationProvider
) : ConfigurationProvider {

    override val isEmpty: Boolean
        get() = getOrNull()?.isEmpty(mapper.serializerProvider) ?: isPresent()

    override fun set(value: JsonNode?) {
        root.set(name, value ?: NullNode.instance)
    }

    override fun set(value: Provider<JsonNode>) {
        set(value.getOrElse(NullNode.instance))
    }

    override fun get(): JsonNode = root.map {
        it.get(name)
    }.get()

    override fun getOrElse(defaultValue: JsonNode): JsonNode = root.map { it.get(name) }.getOrElse(defaultValue)

    override fun getOrNull(): JsonNode? = root.map { it.get(name) }.getOrNull()

    override fun isPresent(): Boolean = root.map { it.get(name) }.isPresent()

    override fun <T : Any> flatMap(transformer: Transformer<Provider<T>, JsonNode>): Provider<T> =
        if (isPresent()) transformer.transform(get()) else ObjectProvider(null)

    override fun <T : Any> map(transformer: Transformer<T, JsonNode>): Provider<T> =
        ObjectProvider(root.map(transformer).getOrNull())

    override fun get(key: String): ConfigurationProvider =
        ParameterConfigurationProvider("$name.$key", mapper, root)

    override fun <T : Any> `as`(type: Class<T>): Provider<T> =
        map { mapper.treeToValue(it, type) as T }

    override fun configure(action: Action<JsonNode>) {
        if (isPresent()) {
            action.execute(get())
        }
    }

    override fun toString(): String =
        "Config[$name] = ${getOrNull()?.textValue() ?: "<null>"}]"
}