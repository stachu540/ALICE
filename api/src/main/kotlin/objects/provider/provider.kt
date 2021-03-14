package ai.alice.api.objects.provider

import ai.alice.api.Consumer
import ai.alice.api.Supplier
import ai.alice.api.Transformer
import kotlin.properties.ReadOnlyProperty


interface ProviderFactory {
  fun <T : Any> empty(): Provider<T>
  fun <T : Any> of(`object`: T): Provider<T>
  fun <T : Any> of(`object`: Supplier<T>): Provider<T>
  fun <T : Any> ofNullable(`object`: T?): Provider<T>

  fun <T : Any> list(vararg objects: T): IterableProvider<T>
  fun <T : Any> list(objects: Iterable<T>): IterableProvider<T>
  fun <K : Any, V : Any> map(objects: Map<K, V>): MapProvider<K, V>
  fun <K : Any, V : Any> map(vararg objects: Pair<K, V>): MapProvider<K, V>
}

interface Provider<T : Any> : ReadOnlyProperty<Any?, T> {
  val isPresent: Boolean

  @Throws(NullPointerException::class, IllegalArgumentException::class)
  fun get(): T
  fun getOrNull(): T?
  fun getOrElse(default: T): T
  fun ifPresent(consumer: Consumer<T>)
  fun <R : Any> map(transformer: Transformer<T, R>): Provider<R>
  fun <R : Any> flatMap(transformer: Transformer<T, Provider<R>>): Provider<R>
}

interface IterableProvider<T : Any> : Provider<Iterable<T>>, Iterable<T> {
  /**
   * Returns the number of entries in this iteration.
   */
  val size: Int

  /**
   * Returns `true` if iteration is empty (contains no elements), `false` otherwise.
   */
  val isEmpty: Boolean

  fun <R : Any> mapIterable(transformer: Transformer<T, R>): IterableProvider<R>
  fun <R : Any> flatMapIterable(transformer: Transformer<T, Iterable<R>>): IterableProvider<R>
}

interface MapProvider<K : Any, V : Any> : Provider<Map<K, V>> {
  /**
   * Returns the number of key/value pairs in the map.
   */
  val size: Int

  /**
   * Returns `true` if the map is empty (contains no elements), `false` otherwise.
   */
  val isEmpty: Boolean

  /**
   * Returns a read-only [Set] of all keys in this map.
   */
  val keys: Set<K>

  /**
   * Returns a read-only [Collection] of all values in this map. Note that this collection may contain duplicate values.
   */
  val values: Collection<V>

  /**
   * Returns a read-only [Set] of all key/value pairs in this map.
   */
  val entries: Set<Map.Entry<K, V>>
}
