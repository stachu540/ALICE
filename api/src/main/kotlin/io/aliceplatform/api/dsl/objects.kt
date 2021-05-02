package io.aliceplatform.api.dsl

import io.aliceplatform.api.objects.NamedObjectCollection
import io.aliceplatform.api.objects.ObjectCollection
import io.aliceplatform.api.objects.ObjectFactory
import io.aliceplatform.api.objects.Provider
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@JvmName("ofResult")
fun <T : Any> ObjectFactory.of(`object`: Result<T>): Provider<T> =
  of { `object`.getOrThrow() }

fun <T : Any> ObjectCollection<Any>.ofType(type: KClass<T>): Provider<T> =
  ofType(type.java)

inline fun <reified T : Any> ObjectCollection<Any>.ofType(): Provider<T> =
  ofType(T::class)

fun <T : Any> NamedObjectCollection<Any>.named(name: String, type: KClass<T>): Provider<T> =
  named(name, type.java)

@JvmName("inlineNamed")
inline fun <reified T : Any> NamedObjectCollection<Any>.named(name: String): Provider<T> =
  named(name, T::class)

