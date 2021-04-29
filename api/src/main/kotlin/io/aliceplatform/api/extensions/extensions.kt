package io.aliceplatform.api.extensions

import io.aliceplatform.api.AliceObject
import io.aliceplatform.api.objects.NamedObjectCollection
import io.aliceplatform.api.objects.Provider

interface ExtensionsProvider : NamedObjectCollection<Any>, AliceObject {
  fun <T : Any> create(name: String, type: Class<T>, vararg values: Any): Provider<T>
  fun <T : Any> register(name: String, value: T)
}
