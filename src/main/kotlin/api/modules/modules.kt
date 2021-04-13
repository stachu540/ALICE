package io.aliceplatform.api.modules

import io.aliceplatform.api.AliceObjectOperator
import io.aliceplatform.api.objects.NamedObjectCollection

interface ModuleProvider : NamedObjectCollection<Module<*>>, AliceObjectOperator {
  infix fun <T> apply(module: Module<T>)
  infix fun apply(id: String): ModuleSpec
}

interface ModuleSpec {
  infix fun version(version: String)
}

interface Module<T> {
  fun apply(target: T)
}
