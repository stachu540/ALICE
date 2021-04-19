package io.aliceplatform.api.modules

import io.aliceplatform.api.Alice
import io.aliceplatform.api.AliceObject
import io.aliceplatform.api.objects.NamedObjectCollection

interface ModuleProvider : NamedObjectCollection<Module<*>>, AliceObject {
  fun <TConfig> install(module: Module<TConfig>, configure: TConfig.() -> Unit)
}

interface Module<TConfig> {
  val name: String
  fun configure(configure: TConfig.() -> Unit): TConfig
  fun apply(alice: Alice, configure: TConfig)
}
