package ai.alice.api.config

import ai.alice.api.AliceObject
import ai.alice.api.Closeable
import ai.alice.api.objects.provider.Provider
import java.io.Serializable

interface Configuration : AliceObject, Closeable {
  fun <T : Any> map(path: String, type: Class<T>): Provider<T>
  fun <T : Any> map(type: Class<T>): Provider<T>
  fun <N : Node> get(path: String): N
}

interface Node {
  val isPrimitive: Boolean
  val isObject: Boolean
  val isArray: Boolean
  val isNull: Boolean

  fun <T : Any> getAs(type: Class<T>): Provider<out Any>
}

interface ArrayNode : Node, Iterable<Node> {
  override fun <T : Any> getAs(type: Class<T>): Provider<Collection<T>>
}

interface ObjectNode : Node, Map<String, Node> {
  override fun <T : Any> getAs(type: Class<T>): Provider<T>
}

interface PrimitiveNode : Node, Serializable {
  val isNumber: Boolean
  val isBoolean: Boolean
  val isNaN: Boolean
  val isString: Boolean

  override fun <T : Any> getAs(type: Class<T>): Provider<T>
}
