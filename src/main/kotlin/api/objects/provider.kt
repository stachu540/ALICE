package io.aliceplatform.api.objects

import io.aliceplatform.api.Consumer
import io.aliceplatform.api.Supplier
import io.aliceplatform.api.Transformer
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

annotation class Inject(
  val name: String = ""
)

interface Property<T> : ReadWriteProperty<Any?, T>, Provider<T> {
  fun set(value: Provider<T>)
  fun set(value: T?)

  fun delegate(value: Provider<T>): Property<T>
  fun delegate(value: T?): Property<T>

  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)
  override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()
}

interface Provider<T> : ReadOnlyProperty<Any?, T> {
  val isPresent: Boolean

  fun get(): T

  fun getOrElse(default: T): T

  fun getOrNull(): T?

  fun <X : Throwable> getOrThrow(throwable: Supplier<X>): T

  fun ifPresent(consumer: Consumer<T>)

  fun <R : Any> map(transformer: Transformer<T, R>): Provider<R>

  fun <R : Any> flatMap(transformer: Transformer<T, Provider<R>>): Provider<R>

  override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()
}

interface IterableProvider<T> : Provider<Iterable<T>>, Iterable<T> {
  fun get(index: Int): T
  fun getOrElse(index: Int, default: T): T
  fun getOrNull(index: Int): T?
  fun <X : Throwable> getOrThrow(index: Int, throwable: Supplier<X>): T
  fun <R : Any> mapIterable(transformer: Transformer<T, R>): IterableProvider<R>
  fun <R : Any> flatMapIterable(transformer: Transformer<T, Iterable<R>>): IterableProvider<R>
}

interface ListProvider<T> : Provider<List<T>> {
  fun get(index: Int): T
  fun getOrElse(index: Int, default: T): T
  fun getOrNull(index: Int): T?
  fun <X : Throwable> getOrThrow(index: Int, throwable: Supplier<X>): T
  fun <R : Any> mapIterable(transformer: Transformer<T, R>): ListProvider<R>
  fun <R : Any> flatMapIterable(transformer: Transformer<T, Iterable<R>>): ListProvider<R>
}

interface SetProvider<T> : Provider<Set<T>> {
  fun <R : Any> mapIterable(transformer: Transformer<T, R>): SetProvider<R>
  fun <R : Any> flatMapIterable(transformer: Transformer<T, Iterable<R>>): SetProvider<R>
}

interface MapProvider<K, V> : Provider<Map<K, V>> {
  val keys: SetProvider<K>
  val values: ListProvider<V>
  val entries: SetProvider<Map.Entry<K, V>>
  fun get(key: K): V
  fun getOrNull(key: K): V?
  fun getOrElse(key: K, default: V): V
  fun <X : Throwable> getOrThrow(key: K, throwable: Supplier<X>): V
  fun <R : Any> mapKeys(transformer: Transformer<K, R>): MapProvider<R, V>
  fun <R : Any> mapValues(transformer: Transformer<V, R>): MapProvider<K, R>
  fun <R : Any> mapIterable(transformer: Transformer<Map.Entry<K, V>, R>): IterableProvider<R>
  fun <R : Any> flatMapIterable(transformer: Transformer<Map.Entry<K, V>, Iterable<R>>): IterableProvider<R>
}

typealias BooleanProvider = Provider<Boolean>
typealias NumberProvider = Provider<Number>
typealias EmptyProvider = Provider<Unit>
