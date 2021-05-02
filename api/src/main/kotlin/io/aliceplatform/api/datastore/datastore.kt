package io.aliceplatform.api.datastore

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonKey
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.aliceplatform.api.AliceObject
import io.aliceplatform.api.AliceObjectOperator
import io.aliceplatform.api.LateInit
import io.aliceplatform.api.objects.BooleanProvider
import io.aliceplatform.api.objects.IterableProvider
import io.aliceplatform.api.objects.NumberProvider
import io.aliceplatform.api.objects.Provider
import java.util.*
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.Id

/**
 * Data Storage Factory
 *
 * Helps utilize some data when you are need to store him into database.
 * To initialize database just use [create] to create table and use this object to start utilize your query
 */
interface DataStoreFactory : AliceObjectOperator {
  fun <T : IdObject<T>> create(dao: Class<T>): Provider<IDao<T>>
}

/**
 * Data Access Object Interface
 */
interface IDao<T : IdObject<T>> : AliceObject {
  /**
   * Getting your requested object
   */
  fun get(id: Id<T>): Provider<T>

  /**
   * Checking if your [query][predicate] is exist
   */
  fun any(predicate: Bson): BooleanProvider

  /**
   * Getting all of them and [filtering][predicate] it
   */
  fun filter(predicate: Bson): IterableProvider<T>

  /**
   * Get first from the [filter][predicate]
   */
  fun findFirst(predicate: Bson): Provider<T>

  /**
   * Add / Update object
   */
  fun put(target: T): BooleanProvider

  /**
   * Remove object by [ID]
   */
  fun remove(id: Id<T>): BooleanProvider

  /**
   * Remove object by [filtering][predicate]
   */
  fun removeIf(predicate: Bson): NumberProvider

  /**
   * List all results
   */
  fun listAll(): IterableProvider<T>
}

interface IdObject<T : IdObject<T>> {
  @get:JsonAlias("_id") val id: Id<T>
}

/**
 * Extension helps to configure your database
 */
class DataStoreExtension {
  var address: String by LateInit { IllegalArgumentException("No address has been defined") }
  var port: Int = 27017
  var username: String? = null
  var password: String? = null
  var database: String by LateInit { IllegalArgumentException("No database has been defined") }

  val connectionString
    get() = buildString {
      append("mongodb://")
      if (username != null && password != null) {
        append("${username}:${password}@")
      }
      append(address)
      if (port != 27017) {
        append(":${port}")
      }
    }
}
