package io.aliceplatform.server

import io.aliceplatform.api.Alice
import io.aliceplatform.api.AliceState
import io.aliceplatform.api.Consumer
import io.aliceplatform.api.Version
import io.aliceplatform.api.config.ConfigurationProvider
import io.aliceplatform.api.datastore.DataStoreFactory
import io.aliceplatform.api.engine.Engine
import io.aliceplatform.api.engine.EngineProvider
import io.aliceplatform.api.event.EventManager
import io.aliceplatform.api.extensions.ExtensionsProvider
import io.aliceplatform.api.modules.Module
import io.aliceplatform.api.modules.ModuleProvider
import io.aliceplatform.api.objects.ObjectFactory
import io.aliceplatform.server.config.ConfigurationProviderImpl
import io.aliceplatform.server.datastore.DataStoreFactoryImpl
import io.aliceplatform.server.event.EventManagerImpl
import io.aliceplatform.server.extensions.ExtensionProviderImpl
import io.aliceplatform.server.objects.ObjectFactoryImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DefaultAliceInstance(
  classLoader: ClassLoader
) : Alice {
  override val logger: Logger = LoggerFactory.getLogger(Alice::class.java)
  override val version: Version = Version.current()
  override val objects: ObjectFactoryImpl = ObjectFactoryImpl(this)
  override val configuration: ConfigurationProviderImpl = ConfigurationProviderImpl(this)
  override val engines: EngineProviderImpl = EngineProviderImpl(this)
  override val modules: ModuleProviderImpl = ModuleProviderImpl(this)
  override val datastore: DataStoreFactoryImpl = DataStoreFactoryImpl(this, classLoader)
  override val events: EventManagerImpl = EventManagerImpl(this)
  override val extensions: ExtensionProviderImpl = ExtensionProviderImpl(this)

  override val alice: Alice
    get() = this

  private val stateManager = AliceStateManager(classLoader)

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

  override fun before(state: AliceState, alice: Consumer<Alice>) {
    val action = stateManager.beforeAction[state]
    stateManager.beforeAction[state] = action?.append(alice) ?: alice
  }

  override fun after(state: AliceState, alice: Consumer<Alice>) {
    val action = stateManager.afterAction[state]
    stateManager.afterAction[state] = action?.append(alice) ?: alice
  }

  override fun <TConf> install(module: Module<TConf>, configure: TConf.() -> Unit) {
    modules.install(module, configure)
  }

  override fun <C : Engine.Config, F : Engine.Factory<C>> install(factory: F, configure: C.() -> Unit) {
    engines.install(factory, configure)
  }

  override fun extensions(extensions: Consumer<ExtensionsProvider>) {
    extensions.consume(this.extensions)
  }

  override fun run() {
    stateManager.beforeAction[AliceState.START]?.consume(this)
    datastore.run()
    engines.run()
    modules.init()
    stateManager.afterAction[AliceState.START]?.consume(this)
  }

  override fun close() {
    stateManager.beforeAction[AliceState.STOP]?.consume(this)
    engines.close()
    datastore.close()
    stateManager.afterAction[AliceState.STOP]?.consume(this)
  }

  internal fun initialize() {
    stateManager.beforeAction[AliceState.INIT]?.consume(this)
    datastore.init()
    stateManager.afterAction[AliceState.INIT]?.consume(this)
  }
}

internal class AliceStateManager(
  val classLoader: ClassLoader
) {
  val beforeAction = mutableMapOf<AliceState, Consumer<Alice>>()
  val afterAction = mutableMapOf<AliceState, Consumer<Alice>>()
}
