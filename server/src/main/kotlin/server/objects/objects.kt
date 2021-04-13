package io.aliceplatform.server.objects

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import io.aliceplatform.api.Supplier
import io.aliceplatform.api.objects.IterableProvider
import io.aliceplatform.api.objects.MapProvider
import io.aliceplatform.api.objects.NamedObjectCollection
import io.aliceplatform.api.objects.ObjectCollection
import io.aliceplatform.api.objects.ObjectFactory
import io.aliceplatform.api.objects.Provider
import io.aliceplatform.server.DefaultAliceInstance
import io.aliceplatform.server.registerAliceModules

class ObjectFactoryImpl(
  override val alice: DefaultAliceInstance
) : ObjectFactory {

  private val mapper = JsonMapper()
    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    .registerAliceModules(alice)
    .findAndRegisterModules()

  override fun <T : Any> convert(data: Any?, type: Class<T>): Provider<T> =
    of { mapper.convertValue(data, type) }

  override fun <T : Any> of(target: T): Provider<T> = of { target }

  override fun <T : Any> of(target: Supplier<T>): Provider<T> =
    ProviderImpl(target.runCatching { get() })

  override fun <T : Any> ofNullable(target: T?): Provider<T> =
    of { target ?: throw NullPointerException("No value presents") }

  override fun <T : Any> empty(): Provider<T> = of { throw NullPointerException("No value presents") }

  override fun <T : Any> many(vararg values: T): IterableProvider<T> =
    many(values.toList())

  override fun <T : Any> many(values: Iterable<T>): IterableProvider<T> {
    TODO("Not yet implemented")
  }

  override fun <K : Any, V : Any> map(index: Map<K, V>): MapProvider<K, V> {
    TODO("Not yet implemented")
  }

  override fun <K : Any, V : Any> map(vararg index: Pair<K, V>): MapProvider<K, V> =
    map(index.toMap())

  override fun <T : Any> list(vararg values: T): ObjectCollection<T> = list(values.toList())

  override fun <T : Any> list(values: Collection<T>): ObjectCollection<T> =
    DefaultObjectCollection<T>(this).also {
      it.set(values)
    }

  override fun <T : Any> named(vararg values: Pair<String, T>): NamedObjectCollection<T> = named(values.toMap())

  override fun <T : Any> named(values: Map<String, T>): NamedObjectCollection<T> =
    DefaultNamedObjectCollection<T>(this).also {
      it.set(values)
    }
}
