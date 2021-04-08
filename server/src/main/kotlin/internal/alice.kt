package io.aliceplatform.internal

import io.aliceplatform.api.Alice
import io.aliceplatform.api.Consumer
import io.aliceplatform.api.Version
import io.aliceplatform.api.config.ConfigurationProvider
import io.aliceplatform.api.datastore.DataStoreFactory
import io.aliceplatform.api.engine.EngineProvider
import io.aliceplatform.api.modules.ModuleProvider
import io.aliceplatform.api.objects.ObjectFactory
import io.aliceplatform.api.web.WebComponentFactory
import io.aliceplatform.internal.objects.ObjectFactoryImpl
import java.io.File
import java.util.*

class AliceImpl(
  configFile: File
) : Alice {
  override val alice: Alice
    get() = this

  override val version: Version = Version.current()
  override val objects: ObjectFactory
  override val configuration: ConfigurationProvider
  override val engines: EngineProvider
  override val web: WebComponentFactory
  override val modules: ModuleProvider
  override val datastore: DataStoreFactory

  init {
    this.configuration = ConfigurationProviderImpl(this, configFile)
    this.objects = ObjectFactoryImpl(this)
    this.engines = EngineProviderImpl(this)
    this.web = WebComponentFactoryImpl(this)
    this.modules = ModuleProviderImpl(this)
    this.datastore = DataStoreFactoryImpl(this)

    postInit()
  }

  override fun configuration(configuration: Consumer<ConfigurationProvider>) {
    configuration.consume(this.configuration)
  }

  override fun engines(engines: Consumer<EngineProvider>) {
    engines.consume(this.engines)
  }

  override fun web(web: Consumer<WebComponentFactory>) {
    web.consume(this.web)
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

  override fun run() {
    TODO("Not yet implemented")
  }

  override fun close() {
    TODO("Not yet implemented")
  }
}

class AliceInstanceBuilder private constructor(val options: AliceOptions) {

  val configuration: AliceConfigurationBuilder = AliceConfigurationBuilder.init(options)

  companion object {
    @JvmStatic
    fun init(argv: Array<String>): AliceInstanceBuilder =
      AliceInstanceBuilder(AliceOptions(argv))
  }

  fun build(): Alice = AliceImpl(this)
}

class AliceConfigurationBuilder private constructor(options: AliceOptions) {
  //  private val file: File
  private val properties = Properties()

  fun add(path: String, value: Any) {
    properties[path] = value
  }

  fun build(alice: AliceImpl): Configuration = TODO()

  companion object {
    fun init(options: AliceOptions) =
      AliceConfigurationBuilder(options)
  }
}

class AliceOptions(argv: Array<String>)
