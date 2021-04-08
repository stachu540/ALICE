package io.aliceplatform.api.objects

import io.aliceplatform.api.Transformer

interface ObjectCollection<T : Any> : Iterable<T> {
  val size: Int

  val isEmpty: Boolean

  fun <R : T> ofType(type: Class<R>): Provider<R>

  fun <R : Any> map(transformer: Transformer<T, R>): ObjectCollection<R>

  fun <R : Any> flatMap(transformer: Transformer<T, Iterable<R>>): ObjectCollection<R>
}

interface NamedObjectCollection<T : Any> : ObjectCollection<T> {
  fun named(name: String): Provider<T>
  fun <R : T> named(name: String, type: Class<R>): Provider<R>
}
