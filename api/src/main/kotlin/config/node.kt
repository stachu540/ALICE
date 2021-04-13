package io.aliceplatform.api.config

import io.aliceplatform.api.objects.BooleanProvider
import io.aliceplatform.api.objects.NumberProvider
import io.aliceplatform.api.objects.Provider

/**
 * Main node of objects
 */
interface Node {
  /**
   * It is [ObjectNode]
   */
  val isObject: Boolean
  /**
   * It is [ArrayNode]
   */
  val isArray: Boolean
  /**
   * It is [PrimitiveNode]
   */
  val isPrimitive: Boolean
  /**
   * It is [NullNode]
   */
  val isNull: Boolean
}

/**
 * Object node
 */
interface ObjectNode : Node, Map<String, Node>

/**
 * Array node
 */
interface ArrayNode : Node, List<Node>

/**
 * Primitive node
 */
interface PrimitiveNode : Node {
  /**
   * primitive is [NumberNode]
   */
  val isNumber: Boolean
  /**
   * primitive is [BooleanNode]
   */
  val isBoolean: Boolean
  /**
   * primitive is [StringNode]
   */
  val isString: Boolean

  /**
   * Returns a specific primitive type value
   */
  fun get(): Provider<out Any>
}

/**
 * Boolean node
 */
interface BooleanNode : PrimitiveNode {
  /**
   * Returns a Boolean value
   */
  override fun get(): BooleanProvider
}

/**
 * Numeric node
 */
interface NumberNode : PrimitiveNode {
  /**
   * Returns a Numeric value
   */
  override fun get(): NumberProvider
}

/**
 * Stringify node
 */
interface StringNode : PrimitiveNode {
  /**
   * Returns a String valued
   */
  override fun get(): Provider<String>
}

/**
 * Nullable node
 */
interface NullNode : PrimitiveNode {
  /**
   * Returns [Nothing] because it is `null`
   */
  override fun get(): Provider<Nothing>
}
