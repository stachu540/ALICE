package ai.alice.internal.objects

import ai.alice.api.Supplier
import ai.alice.api.objects.NamedObjectContainer
import ai.alice.api.objects.ObjectContainer
import ai.alice.api.objects.ObjectFactory
import ai.alice.api.objects.provider.IterableProvider
import ai.alice.api.objects.provider.MapProvider
import ai.alice.api.objects.provider.Provider
import com.fasterxml.jackson.databind.ObjectMapper

class ObjectFactoryImpl : ObjectFactory {

  private val mapper: ObjectMapper = ObjectMapper()
    .findAndRegisterModules()

  override fun <T : Any> convert(`object`: Any, type: Class<T>): Provider<T> =
    of { mapper.convertValue(`object`, type) }

  override fun <T : Any> empty(): Provider<T> =
    ofNullable(null)

  override fun <T : Any> of(`object`: T): Provider<T> =
    ofNullable(`object`)

  override fun <T : Any> of(`object`: Supplier<T>): Provider<T> {
    TODO("Not yet implemented")
  }

  override fun <T : Any> ofNullable(`object`: T?): Provider<T> =
    of { `object` ?: throw NullPointerException("No value presents.") }

  override fun <T : Any> list(vararg objects: T): IterableProvider<T> {
    TODO("Not yet implemented")
  }

  override fun <T : Any> list(objects: Iterable<T>): IterableProvider<T> {
    TODO("Not yet implemented")
  }

  override fun <K : Any, V : Any> map(objects: Map<K, V>): MapProvider<K, V> {
    TODO("Not yet implemented")
  }

  override fun <K : Any, V : Any> map(vararg objects: Pair<K, V>): MapProvider<K, V> {
    TODO("Not yet implemented")
  }

  override fun <T : Any> create(vararg container: T): ObjectContainer<T> {
    TODO("Not yet implemented")
  }

  override fun <T : Any> create(container: Collection<T>): ObjectContainer<T> {
    TODO("Not yet implemented")
  }

  override fun <T : Any> named(container: Map<String, T>): NamedObjectContainer<T> {
    TODO("Not yet implemented")
  }

  override fun <T : Any> named(vararg container: Pair<String, T>): NamedObjectContainer<T> {
    TODO("Not yet implemented")
  }

}
