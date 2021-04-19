package io.aliceplatform.server.objects

import io.aliceplatform.api.Transformer
import io.aliceplatform.api.dsl.of
import io.aliceplatform.api.objects.NamedObjectCollection
import io.aliceplatform.api.objects.ObjectCollection
import io.aliceplatform.api.objects.ObjectFactory
import io.aliceplatform.api.objects.Provider

open class DefaultObjectCollection<T : Any> internal constructor(
  private val objects: ObjectFactory
) : ObjectCollection<T> {
  internal var collections = mutableListOf<T>()
    private set

  fun set(collections: Collection<T>) {
    this.collections = collections.toMutableList()
  }

  override fun iterator(): Iterator<T> = collections.iterator()

  override val size: Int
    get() = collections.size
  override val isEmpty: Boolean
    get() = collections.isEmpty()

  override fun <R : T> ofType(type: Class<R>): Provider<R> =
    objects.ofNullable(collections.firstOrNull { type.isInstance(it) })
      .map { type.cast(it) }

  override fun <R : Any> map(transformer: Transformer<T, R>): ObjectCollection<R> =
    DefaultObjectCollection<R>(objects).also {
      it.set(this.collections.map(transformer::transform))
    }

  override fun <R : Any> flatMap(transformer: Transformer<T, Iterable<R>>): ObjectCollection<R> =
    DefaultObjectCollection<R>(objects).also {
      it.set(this.collections.flatMap(transformer::transform))
    }
}

open class DefaultNamedObjectCollection<T : Any> internal constructor(
  private val objects: ObjectFactory
) : NamedObjectCollection<T> {
  internal var collections = mutableMapOf<String, T>()
    private set

  fun set(collections: Map<String, T>) {
    this.collections = collections.mapKeys { it.key.toLowerCase() }.toMutableMap()
  }

  override fun iterator(): Iterator<T> = collections.values.iterator()

  override val size: Int
    get() = collections.size
  override val isEmpty: Boolean
    get() = collections.isEmpty()

  override fun <R : T> ofType(type: Class<R>): Provider<R> =
    objects.ofNullable(collections.values.firstOrNull { type.isInstance(it) })
      .map { type.cast(it) }

  override fun <R : Any> map(transformer: Transformer<T, R>): ObjectCollection<R> =
    DefaultObjectCollection<R>(objects).also {
      it.set(collections.values.map(transformer::transform))
    }

  override fun <R : Any> flatMap(transformer: Transformer<T, Iterable<R>>): ObjectCollection<R> =
    DefaultObjectCollection<R>(objects).also {
      it.set(collections.values.flatMap(transformer::transform))
    }

  override fun named(name: String): Provider<T> =
    objects.ofNullable(collections[name])

  override fun <R : T> named(name: String, type: Class<R>): Provider<R> =
    objects.of<R>(collections[name].runCatching {
      if (this == null) throw NullPointerException("No value presents")
      else type.cast(this)
    })
}
