package io.aliceplatform.server

import io.aliceplatform.api.Alice
import io.aliceplatform.api.Consumer
import io.aliceplatform.api.Version
import io.aliceplatform.api.config.ConfigurationProvider
import io.aliceplatform.api.datastore.DataStoreFactory
import io.aliceplatform.api.engine.EngineProvider
import io.aliceplatform.api.event.EventManager
import io.aliceplatform.api.modules.ModuleProvider
import io.aliceplatform.api.objects.ObjectFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DefaultAliceInstance(classLoader: ClassLoader) : Alice {
  override val logger: Logger = LoggerFactory.getLogger(Alice::class.java)
  override val version: Version = Version.current()
  override val configuration: ConfigurationProvider
    get() = TODO("Not yet implemented")
  override val engines: EngineProvider
    get() = TODO("Not yet implemented")
  override val modules: ModuleProvider
    get() = TODO("Not yet implemented")
  override val datastore: DataStoreFactory
    get() = TODO("Not yet implemented")
  override val objects: ObjectFactory
    get() = TODO("Not yet implemented")
  override val events: EventManager
    get() = TODO("Not yet implemented")
  override val alice: Alice
    get() = this

  override fun configuration(configuration: Consumer<ConfigurationProvider>) {
    configuration.consume(this.configuration)
  }

  override fun engines(engines: Consumer<EngineProvider>) {
    engines.consume(this.engines)
  }

  override fun modules(modules: Consumer<ModuleProvider>) {
    modules.consume(this.modules)
  }

  override fun datastore(datastore: Consumer<DataStoreFactory>) {
    datastore.consume(this.datastore)
  }

  override fun objects(objects: Consumer<ObjectFactory>) {
    objects.consume(this.objects)
  }

  override fun events(events: Consumer<EventManager>) {
    events.consume(this.events)
  }

  override fun preInit(alice: Consumer<Alice>) {
    TODO("Not yet implemented")
  }

  override fun postInit(alice: Consumer<Alice>) {
    TODO("Not yet implemented")
  }

  override fun preStart(alice: Consumer<Alice>) {
    TODO("Not yet implemented")
  }

  override fun postStart(alice: Consumer<Alice>) {
    TODO("Not yet implemented")
  }

  override fun preStop(alice: Consumer<Alice>) {
    TODO("Not yet implemented")
  }

  override fun postStop(alice: Consumer<Alice>) {
    TODO("Not yet implemented")
  }

  override fun run() {
    TODO("Not yet implemented")
  }

  override fun close() {
    TODO("Not yet implemented")
  }

  internal fun initialize() {

  }
}
