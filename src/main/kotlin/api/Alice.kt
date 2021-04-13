package io.aliceplatform.api

import io.aliceplatform.api.config.ConfigurationProvider
import io.aliceplatform.api.datastore.DataStoreFactory
import io.aliceplatform.api.engine.EngineProvider
import io.aliceplatform.api.event.EventManager
import io.aliceplatform.api.modules.ModuleProvider
import io.aliceplatform.api.objects.ObjectFactory
import org.slf4j.Logger

interface Alice : AliceObjectOperator {
  val logger: Logger
  val version: Version
  val configuration: ConfigurationProvider
  val engines: EngineProvider
  val modules: ModuleProvider
  val datastore: DataStoreFactory
  val objects: ObjectFactory
  val events: EventManager

  fun configuration(configuration: Consumer<ConfigurationProvider>)
  fun engines(engines: Consumer<EngineProvider>)
  fun modules(modules: Consumer<ModuleProvider>)
  fun datastore(datastore: Consumer<DataStoreFactory>)
  fun objects(objects: Consumer<ObjectFactory>)
  fun events(events: Consumer<EventManager>)

  fun preInit(alice: Consumer<Alice>)
  fun postInit(alice: Consumer<Alice>)

  fun preStart(alice: Consumer<Alice>)
  fun postStart(alice: Consumer<Alice>)

  fun preStop(alice: Consumer<Alice>)
  fun postStop(alice: Consumer<Alice>)
}
