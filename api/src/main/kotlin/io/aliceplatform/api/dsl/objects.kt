package io.aliceplatform.api.dsl

import io.aliceplatform.api.objects.NamedObjectCollection
import io.aliceplatform.api.objects.ObjectCollection
import io.aliceplatform.api.objects.ObjectFactory
import io.aliceplatform.api.objects.Provider
import kotlin.reflect.KClass

fun <T : Any> ObjectFactory.of(`object`: Result<T>): Provider<T> =
  of { `object`.getOrThrow() }

fun <T : Any, R : T> ObjectCollection<T>.ofType(type: KClass<R>): Provider<R> =
  ofType(type.java)

inline fun <T : Any, reified R : T> ObjectCollection<T>.ofType(): Provider<R> =
  ofType(R::class)

fun <T : Any, R : T> NamedObjectCollection<T>.named(name: String, type: KClass<R>): Provider<R> =
  named(name, type.java)

inline fun <T : Any, reified R : T> NamedObjectCollection<T>.named(name: String): Provider<R> =
  named(name, R::class)
