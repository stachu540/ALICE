package ai.alice.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.typesafe.config.*

object ConfigObjectFormat {
    class Serializer : JsonSerializer<ConfigValue>() {
        override fun serialize(value: ConfigValue, generator: JsonGenerator, provider: SerializerProvider) {
            provider.defaultSerializeValue(value.unwrapped(), generator)
        }
    }

    class Deserializer : JsonDeserializer<ConfigValue>() {
        override fun deserialize(parser: JsonParser, context: DeserializationContext): ConfigValue =
            ConfigFactory.parseString(parser.text, ConfigParseOptions.defaults().setSyntax(ConfigSyntax.JSON))
                .root()
    }
}