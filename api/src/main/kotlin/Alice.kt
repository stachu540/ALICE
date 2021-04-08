package io.aliceplatform.api

import io.aliceplatform.api.config.ConfigurationProvider
import io.aliceplatform.api.datastore.DataStoreFactory
import io.aliceplatform.api.engine.EngineProvider
import io.aliceplatform.api.modules.ModuleProvider
import io.aliceplatform.api.objects.ObjectFactory
import io.aliceplatform.api.web.WebComponentFactory

interface Alice : AliceObjectOperator {
  val version: Version
  val configuration: ConfigurationProvider
  val engines: EngineProvider
  val web: WebComponentFactory
  val modules: ModuleProvider
  val datastore: DataStoreFactory
  val objects: ObjectFactory

  fun configuration(configuration: Consumer<ConfigurationProvider>)
  fun engines(engines: Consumer<EngineProvider>)
  fun web(web: Consumer<WebComponentFactory>)
  fun modules(modules: Consumer<ModuleProvider>)
  fun datastore(datastore: Consumer<DataStoreFactory>)
  fun objects(objects: Consumer<ObjectFactory>)
}
