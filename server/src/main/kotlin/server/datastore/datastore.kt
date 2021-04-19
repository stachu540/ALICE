package io.aliceplatform.server.datastore

import com.fasterxml.jackson.databind.ObjectMapper
import io.aliceplatform.api.LateInit
import io.aliceplatform.api.datastore.DataStoreFactory
import io.aliceplatform.api.datastore.IDao
import io.aliceplatform.api.datastore.IdObject
import io.aliceplatform.api.objects.Provider
import io.aliceplatform.server.DefaultAliceInstance
import io.aliceplatform.server.registerAliceModules
import java.nio.channels.NotYetConnectedException
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.jackson2.Jackson2Config
import org.jdbi.v3.jackson2.Jackson2Plugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin

class DataStoreFactoryImpl(
  override val alice: DefaultAliceInstance,
  private val classLoader: ClassLoader
) : DataStoreFactory {
  private var jdbi: Jdbi by LateInit { IllegalStateException("Instance is not ready to resolve") }
  private var handle: Handle by LateInit { NotYetConnectedException() }

  override fun <DAO : IDao<T, ID>, T : IdObject<ID>, ID> create(dao: Class<DAO>): Provider<DAO> =
    alice.objects.of { handle.attach(dao) }

  override fun run() {
    handle = jdbi.open()

    // check tables - fetching table classes
    // update if it is necessary
    // when {
    //   instance updated -> fetch table classes and update tables - migration solutions
    // }
  }

  override fun close() {
    handle.close()
  }

  internal fun init() {
    jdbi = Jdbi.create("")
      .installPlugin(Jackson2Plugin())
      .configure(Jackson2Config::class.java) {
        it.mapper = ObjectMapper()
          .findAndRegisterModules()
          .registerAliceModules(alice)
      }
      .installPlugin(KotlinSqlObjectPlugin())
  }

  private fun fetchTables() {

  }
}
