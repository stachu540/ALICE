package ai.alice.api.datastore

import ai.alice.api.AliceInstanceObject
import ai.alice.api.objects.provider.Provider

interface DataStore : AliceInstanceObject {
  fun <T : Any> create(type: Class<T>): Provider<T>
}
