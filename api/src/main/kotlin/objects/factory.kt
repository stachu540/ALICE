package io.aliceplatform.api.objects

import io.aliceplatform.api.AliceObject
import io.aliceplatform.api.Supplier

interface ObjectFactory : ProviderFactory, CollectionFactory, AliceObject {
  fun <T : Any> convert(data: Any?, type: Class<T>): Provider<T>
}

interface ProviderFactory {
  fun <T : Any> of(target: Supplier<T>): Provider<T>
  fun <T : Any> ofNullable(target: T?): Provider<T>
  fun <T : Any> of(target: T): Provider<T>
  fun <T : Any> empty(): Provider<T>

  fun <T : Any> many(vararg values: T): IterableProvider<T>
  fun <T : Any> many(values: Iterable<T>): IterableProvider<T>

  fun <K : Any, V : Any> map(index: Map<K, V>): MapProvider<K, V>
  fun <K : Any, V : Any> map(vararg index: Pair<K, V>): MapProvider<K, V>
}

interface CollectionFactory {
  fun <T : Any> list(vararg values: T): ObjectCollection<T>
  fun <T : Any> list(values: Collection<T>): ObjectCollection<T>
  fun <T : Any> named(vararg values: Pair<String, T>): NamedObjectCollection<T>
  fun <T : Any> named(values: Map<String, T>): NamedObjectCollection<T>
}

