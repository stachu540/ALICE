package io.aliceplatform.api.config

import io.aliceplatform.api.objects.BooleanProvider
import io.aliceplatform.api.objects.ListProvider
import io.aliceplatform.api.objects.MapProvider
import io.aliceplatform.api.objects.NumberProvider
import io.aliceplatform.api.objects.Provider

interface Node {
  val isObject: Boolean
  val isArray: Boolean
  val isPrimitive: Boolean
  val isNull: Boolean

  fun get(): Provider<out Any>
}

interface ObjectNode : Node, Map<String, Node> {
  override fun get(): MapProvider<String, Any>
}

interface ArrayNode : Node, List<Node> {
  override fun get(): ListProvider<Any>
}

interface PrimitiveNode : Node {
  val isNumber: Boolean
  val isBoolean: Boolean
  val isString: Boolean
}

interface BooleanNode : PrimitiveNode {
  override fun get(): BooleanProvider
}

interface NumberNode : PrimitiveNode {
  override fun get(): NumberProvider
}

interface StringNode : PrimitiveNode {
  override fun get(): Provider<String>
}

interface NullNode : Node {
  override fun get(): Provider<Nothing>
}
