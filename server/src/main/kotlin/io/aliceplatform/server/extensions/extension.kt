package io.aliceplatform.server.extensions

import io.aliceplatform.api.Transformer
import io.aliceplatform.api.extensions.ExtensionsProvider
import io.aliceplatform.api.objects.ObjectCollection
import io.aliceplatform.api.objects.Provider
import io.aliceplatform.server.DefaultAliceInstance

class ExtensionProviderImpl(
  override val alice: DefaultAliceInstance
) : ExtensionsProvider {
  private val extensions = mutableMapOf<String, Any>()

  override val size: Int
    get() = extensions.size
  override val isEmpty: Boolean
    get() = extensions.isEmpty()

  override fun <T : Any> create(name: String, type: Class<T>, vararg values: Any): Provider<T> =
    alice.objects.of {
      type.getConstructor(*values.map { it.javaClass }.toTypedArray()).newInstance(values)
    }.apply {
      ifPresent {
        register(name, it)
      }
    }

  override fun <T : Any> register(name: String, value: T) {
    if (extensions.containsKey(name)) {
      throw IllegalArgumentException("This name has been taken! \"$name\"")
    } else {
      extensions[name] = value
    }
  }

  override fun named(name: String): Provider<Any> =
    alice.objects.of { extensions[name] ?: throw NullPointerException("No valued extension present") }

  override fun <R : Any> named(name: String, type: Class<R>): Provider<R> =
    named(name).map { type.cast(it) }

  override fun <R : Any> ofType(type: Class<R>): Provider<R> =
    alice.objects.of {
      extensions.values.firstOrNull { type.isInstance(it) }?.let { type.cast(it) }
        ?: throw NullPointerException("No valued extension present")
    }

  override fun <R : Any> map(transformer: Transformer<Any, R>): ObjectCollection<R> =
    alice.objects.list(extensions.values.map(transformer::transform))

  override fun <R : Any> flatMap(transformer: Transformer<Any, Iterable<R>>): ObjectCollection<R> =
    alice.objects.list(extensions.values.flatMap(transformer::transform))

  override fun iterator(): Iterator<Any> =
    extensions.values.iterator()
}
