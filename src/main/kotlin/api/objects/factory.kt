package io.aliceplatform.api.objects

import io.aliceplatform.api.AliceObject
import io.aliceplatform.api.Supplier

interface ObjectFactory : ProviderFactory, CollectionFactory, AliceObject {
  fun <T : Any> convert(data: Any?, type: Class<T>): Provider<T>
}

interface ProviderFactory {
  fun <T : Any> of(target: T): Provider<T>
  fun <T : Any> of(target: Supplier<T>): Provider<T>
  fun <T : Any> ofNullable(target: T?): Provider<T>
  fun <T : Any> empty(): Provider<T>
}

interface CollectionFactory {
  fun <T : Any> list(vararg values: T): ObjectCollection<T>
  fun <T : Any> list(values: Collection<T>): ObjectCollection<T>
  fun <T : Any> named(vararg values: Named<T>): NamedObjectCollection<T>
  fun <T : Any> named(values: Collection<Named<T>>): NamedObjectCollection<T>
  fun <T : Any> named(vararg values: Pair<String, T>): NamedObjectCollection<T>
  fun <T : Any> named(values: Map<String, T>): NamedObjectCollection<T>
}

interface Named<T : Any> {
  val name: String
}

class NamedInstance<T : Any>(
  override val name: String,
  val instance: T
) : Named<T>
