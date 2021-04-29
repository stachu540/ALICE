package io.aliceplatform.server.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import io.aliceplatform.api.AliceObject
import io.aliceplatform.api.config.ArrayNode
import io.aliceplatform.api.config.BooleanNode
import io.aliceplatform.api.config.Node
import io.aliceplatform.api.config.NullNode
import io.aliceplatform.api.config.NumberNode
import io.aliceplatform.api.config.ObjectNode
import io.aliceplatform.api.config.PrimitiveNode
import io.aliceplatform.api.config.StringNode
import io.aliceplatform.api.objects.BooleanProvider
import io.aliceplatform.api.objects.NumberProvider
import io.aliceplatform.api.objects.ObjectFactory
import io.aliceplatform.api.objects.Provider
import io.aliceplatform.server.DefaultAliceInstance
import io.aliceplatform.server.toJacksonVersion
import kotlin.reflect.KClass

// Node

abstract class AbstractNode : Node {
  override val isObject: Boolean
    get() = this is ObjectNode
  override val isArray: Boolean
    get() = this is ArrayNode
  override val isPrimitive: Boolean
    get() = this is PrimitiveNode
  override val isNull: Boolean
    get() = this is NullNode
}

class ObjectNodeImpl(
  private val nodes: Map<String, Node>
) : AbstractNode(), ObjectNode, Map<String, Node> by nodes

class ArrayNodeImpl(
  private val nodes: List<Node>
) : AbstractNode(), ArrayNode, List<Node> by nodes

abstract class AbstractPrimitiveNode(
  protected val objects: ObjectFactory
) : AbstractNode(), PrimitiveNode {
  override val isNumber: Boolean
    get() = this is NumberNode
  override val isBoolean: Boolean
    get() = this is BooleanNode
  override val isString: Boolean
    get() = this is StringNode
}

class BooleanNodeImpl(
  objects: ObjectFactory,
  internal val value: Boolean
) : AbstractPrimitiveNode(objects), BooleanNode {
  override fun get(): BooleanProvider =
    objects.of(value)
}

class NumberNodeImpl(
  objects: ObjectFactory,
  internal val value: Number
) : AbstractPrimitiveNode(objects), NumberNode {
  override fun get(): NumberProvider =
    objects.of(value)
}

class StringNodeImpl(
  objects: ObjectFactory,
  internal val value: String
) : AbstractPrimitiveNode(objects), StringNode {
  override fun get(): Provider<String> =
    objects.of(value)
}

class NullNodeImpl(
  objects: ObjectFactory
) : AbstractPrimitiveNode(objects), NullNode {
  override fun get(): Provider<Nothing> =
    objects.empty()
}

// Serializer

class AliceNodesModule(
  override val alice: DefaultAliceInstance
) : SimpleModule("AliceNodeModule", alice.version.toJacksonVersion()), AliceObject {
  init {
    addSerializer(NullNode::class, NullNodeSerializer())
    addSerializer(StringNode::class, StringNodeSerializer())
    addSerializer(NumberNode::class, NumberNodeSerializer())
    addSerializer(BooleanNode::class, BooleanNodeSerializer())
    addSerializer(ArrayNode::class, ArrayNodeSerializer())
    addSerializer(ObjectNode::class, ObjectNodeSerializer())

    addDeserializer(NullNode::class, NullNodeDeserializer(alice))
    addDeserializer(StringNode::class, StringNodeDeserializer(alice))
    addDeserializer(NumberNode::class, NumberNodeDeserializer(alice))
    addDeserializer(BooleanNode::class, BooleanNodeDeserializer(alice))
    addDeserializer(ArrayNode::class, ArrayNodeDeserializer())
    addDeserializer(ObjectNode::class, ObjectNodeDeserializer())
  }

  fun <T : Any> addSerializer(type: KClass<T>, serializer: JsonSerializer<T>) =
    addSerializer(type.java, serializer)

  fun <T : Any> addDeserializer(type: KClass<T>, serializer: JsonDeserializer<T>) =
    addDeserializer(type.java, serializer)
}

private class NullNodeDeserializer(
  override val alice: DefaultAliceInstance
) : JsonDeserializer<NullNode>(), AliceObject {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NullNode =
    if (p.isNaN) getNullValue(ctxt) else getNullValue(ctxt)

  override fun getNullValue(ctxt: DeserializationContext): NullNode =
    NullNodeImpl(alice.objects)
}

private class StringNodeDeserializer(
  override val alice: DefaultAliceInstance
) : JsonDeserializer<StringNode>(), AliceObject {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): StringNode =
    StringNodeImpl(alice.objects, p.valueAsString)

}

private class NumberNodeDeserializer(
  override val alice: DefaultAliceInstance
) : JsonDeserializer<NumberNode>(), AliceObject {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NumberNode =
    NumberNodeImpl(alice.objects, p.numberValue)

}

private class BooleanNodeDeserializer(
  override val alice: DefaultAliceInstance
) : JsonDeserializer<BooleanNode>(), AliceObject {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BooleanNode =
    BooleanNodeImpl(alice.objects, p.valueAsBoolean)
}

private class ArrayNodeDeserializer : JsonDeserializer<ArrayNode>() {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ArrayNode =
    if (p.isExpectedStartArrayToken) {
      ArrayNodeImpl(
        p.codec.readValue(p, ctxt.typeFactory.constructCollectionType(List::class.java, Node::class.java))
      )
    } else {
      throw JsonParseException(p, "Could not processing as array type")
    }
}

private class ObjectNodeDeserializer : JsonDeserializer<ObjectNode>() {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ObjectNode =
    if (p.isExpectedStartObjectToken) {
      ObjectNodeImpl(
        p.codec.readValue(p, ctxt.typeFactory.constructMapType(Map::class.java, String::class.java, Node::class.java))
      )
    } else {
      throw JsonParseException(p, "Could not processing as object type")
    }
}

private class NullNodeSerializer : JsonSerializer<NullNode>() {
  override fun serialize(value: NullNode, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeNull()
  }
}

private class StringNodeSerializer : JsonSerializer<StringNode>() {
  override fun serialize(value: StringNode, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeString((value as StringNodeImpl).value)
  }

}

private class NumberNodeSerializer : JsonSerializer<NumberNode>() {
  override fun serialize(value: NumberNode, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeNumber((value as NumberNodeImpl).value.toString())
  }

}

private class BooleanNodeSerializer : JsonSerializer<BooleanNode>() {
  override fun serialize(value: BooleanNode, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeBoolean((value as BooleanNodeImpl).value)
  }
}

private class ArrayNodeSerializer : JsonSerializer<ArrayNode>() {
  override fun serialize(value: ArrayNode, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeStartArray()
    value.forEach {
      serializers.defaultSerializeValue(it, gen)
    }
    gen.writeEndArray()
  }
}

private class ObjectNodeSerializer : JsonSerializer<ObjectNode>() {
  override fun serialize(value: ObjectNode, gen: JsonGenerator, serializers: SerializerProvider) {
    if (value.isEmpty() || value.isNull) return
    gen.writeStartObject()
    value.forEach {
      gen.writeFieldName(it.key)
      serializers.defaultSerializeValue(it.value, gen)
    }
    gen.writeEndObject()
  }
}
