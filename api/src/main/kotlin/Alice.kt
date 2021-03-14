package ai.alice.api

import ai.alice.api.config.Configuration
import ai.alice.api.datastore.DataStore
import ai.alice.api.engine.EngineContainer
import ai.alice.api.objects.ObjectFactory

interface Alice {
  val configurations: Configuration
  val engines: EngineContainer
  val objects: ObjectFactory
  val datastore: DataStore

  fun configurations(configurations: Consumer<Configuration>)
  fun engines(engines: Consumer<EngineContainer>)
  fun objects(objects: Consumer<ObjectFactory>)
  fun datastore(datastore: Consumer<DataStore>)
}
