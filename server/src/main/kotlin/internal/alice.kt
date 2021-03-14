package ai.alice.internal

import ai.alice.api.Alice
import ai.alice.api.Consumer
import ai.alice.api.config.Configuration
import ai.alice.api.datastore.DataStore
import ai.alice.api.engine.EngineContainer
import ai.alice.api.objects.ObjectFactory
import ai.alice.internal.containers.DataStoreImpl
import ai.alice.internal.containers.EngineContainerImpl
import ai.alice.internal.objects.ObjectFactoryImpl
import java.io.File
import java.util.*

class AliceImpl(
  builder: AliceInstanceBuilder
) : Alice {
  override val objects: ObjectFactory = ObjectFactoryImpl()
  override val configurations: Configuration = builder.configuration.build(this)
  override val engines: EngineContainer = EngineContainerImpl(this)
  override val datastore: DataStore = DataStoreImpl(this)

  override fun configurations(configurations: Consumer<Configuration>) {
    this.configurations.let(configurations::consume)
  }

  override fun engines(engines: Consumer<EngineContainer>) {
    this.engines.let(engines::consume)
  }

  override fun objects(objects: Consumer<ObjectFactory>) {
    this.objects.let(objects::consume)
  }

  override fun datastore(datastore: Consumer<DataStore>) {
    this.datastore.let(datastore::consume)
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

  fun add(path: String, `object`: Any) {
    properties[path] = `object`
  }

  fun build(alice: AliceImpl): Configuration = TODO()

  companion object {
    fun init(options: AliceOptions) =
      AliceConfigurationBuilder(options)
  }
}

class AliceOptions(argv: Array<String>)
