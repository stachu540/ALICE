package io.aliceplatform.server.datastore

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import io.aliceplatform.api.LateInit
import io.aliceplatform.api.datastore.DataStoreExtension
import io.aliceplatform.api.datastore.DataStoreFactory
import io.aliceplatform.api.datastore.IDao
import io.aliceplatform.api.datastore.IdObject
import io.aliceplatform.api.dsl.named
import io.aliceplatform.api.objects.BooleanProvider
import io.aliceplatform.api.objects.IterableProvider
import io.aliceplatform.api.objects.NumberProvider
import io.aliceplatform.api.objects.Provider
import io.aliceplatform.server.DefaultAliceInstance
import java.util.*
import org.bson.conversions.Bson
import org.litote.kmongo.Id
import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

class DataStoreFactoryImpl(
  override val alice: DefaultAliceInstance
) : DataStoreFactory {
  private val config: DataStoreExtension by alice.extensions.named<DataStoreExtension>("datastore")
  private var client: MongoClient by LateInit { IllegalStateException("Instance is not ready to resolve") }
  private val database: MongoDatabase
    get() = client.getDatabase(config.database)

  override fun <T : IdObject<T>> create(dao: Class<T>): Provider<IDao<T>> =
    alice.objects.of { DaoCollection(alice, database.getCollection(getName(dao), dao)) }

  override fun run() {
    client = KMongo.createClient(config.connectionString)
  }

  override fun close() {
    client.close()
  }

  private fun getName(type: Class<*>): String =
    (PropertyNamingStrategies.SNAKE_CASE as PropertyNamingStrategies.SnakeCaseStrategy).translate(type.simpleName)

}

internal class DaoCollection<T : IdObject<T>>(
  override val alice: DefaultAliceInstance,
  private val collection: MongoCollection<T>
) : IDao<T> {
  override fun get(id: Id<T>): Provider<T> =
    findFirst(IdObject<T>::id eq id)

  override fun any(predicate: Bson): BooleanProvider =
    alice.objects.of { collection.find(predicate).any() }

  override fun filter(predicate: Bson): IterableProvider<T> =
    alice.objects.many(collection.find(predicate))

  override fun findFirst(predicate: Bson): Provider<T> =
    alice.objects.ofNullable(collection.findOne(predicate))

  override fun put(target: T): BooleanProvider =
    alice.objects.of { collection.insertOne(target).let { it.insertedId != null && it.wasAcknowledged() } }

  override fun remove(id: Id<T>): BooleanProvider =
    alice.objects.of { collection.deleteOne(IdObject<T>::id eq id).let { it.deletedCount > 0 && it.wasAcknowledged() } }

  override fun removeIf(predicate: Bson): NumberProvider =
    alice.objects.of { collection.deleteMany(predicate).deletedCount }

  override fun listAll(): IterableProvider<T> =
    alice.objects.many(collection.find())
}
