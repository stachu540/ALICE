package ai.alice.api.objects

import ai.alice.api.AliceObject
import ai.alice.api.Consumer
import ai.alice.api.Transformer
import ai.alice.api.objects.provider.Provider

interface ContainerFactory {
  fun <T : Any> create(vararg container: T): ObjectContainer<T>

  fun <T : Any> create(container: Collection<T>): ObjectContainer<T>

  fun <T : Any> named(container: Map<String, T>): NamedObjectContainer<T>

  fun <T : Any> named(vararg container: Pair<String, T>): NamedObjectContainer<T>
}

interface ObjectContainer<T : Any> : AliceObject, Collection<T> {

  fun <R : T> ofType(type: Class<R>): Provider<R>

  fun <R : Any> map(transformer: Transformer<T, R>): ObjectContainer<R>

  fun <R : Any> flatMap(transformer: Transformer<T, Iterable<R>>): ObjectContainer<R>

  fun <R : T> configure(type: Class<R>, action: Consumer<R>)

  fun configureEach(action: Consumer<T>)
}

interface NamedObjectContainer<T : Any> : ObjectContainer<T> {

  fun ofNamed(name: String): Provider<T>

  fun <R : T> ofNamed(name: String, type: Class<R>): Provider<R>

  fun configure(name: String, action: Consumer<T>)

  fun <R : T> configure(name: String, type: Class<R>, action: Consumer<R>)
}
