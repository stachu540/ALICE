package io.aliceplatform.api.datastore

import io.aliceplatform.api.AliceObjectOperator
import io.aliceplatform.api.LateInit
import io.aliceplatform.api.Predicate
import io.aliceplatform.api.objects.BooleanProvider
import io.aliceplatform.api.objects.IterableProvider
import io.aliceplatform.api.objects.NumberProvider
import io.aliceplatform.api.objects.Provider
import java.sql.Driver
import java.sql.SQLException
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

/**
 * Data Storage Factory
 *
 * Helps utilize some data when you are need to store him into database.
 * To initialize database just use [create] to create table and use this object to start utilize your query
 */
interface DataStoreFactory : AliceObjectOperator {
  fun <DAO : IDao<T, ID>, T : IdObject<ID>, ID> create(dao: Class<DAO>): Provider<DAO>
}

/**
 * Data Access Object Interface
 */
@UseClasspathSqlLocator
interface IDao<T : IdObject<ID>, ID> {
  /**
   * Getting your requested object
   */
  @SqlUpdate
  fun get(id: ID): Provider<T>

  /**
   * Checking if your [query][predicate] is exist
   */
  @SqlQuery
  fun any(predicate: Predicate<T>): BooleanProvider

  /**
   * Getting all of them and [filtering][predicate] it
   */
  @SqlQuery
  fun filter(predicate: Predicate<T>): IterableProvider<T>

  /**
   * Get first from the [filter][predicate]
   */
  @SqlQuery
  fun findFirst(predicate: Predicate<T>): Provider<T>

  /**
   * Add / Update object
   */
  @SqlUpdate
  fun put(@BindBean target: T): BooleanProvider

  /**
   * Remove object by [ID]
   */
  @SqlUpdate
  fun remove(id: ID): BooleanProvider

  /**
   * Remove object by [filtering][predicate]
   */
  @SqlUpdate
  fun removeIf(predicate: Predicate<T>): NumberProvider

  /**
   * List all results
   */
  @SqlQuery
  fun listAll(): IterableProvider<T>
}

/**
 * ID Object interface
 */
interface IdObject<ID> {
  val id: ID
}

/**
 * Extension helps to configure your database
 */
class DataStoreExtension {
  var driver: Class<out Driver> by LateInit { SQLException("No driver has been defined") }
  var address: String by LateInit { IllegalArgumentException("No address has been defined") }
  var port: Int = -1
  var username: String by LateInit { SQLException("No username has been defined") }
  var password: String by LateInit { SQLException("No password has been defined") }
  var database: String by LateInit { SQLException("No database has been defined") }
}
