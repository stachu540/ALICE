package io.aliceplatform.api.datastore

import io.aliceplatform.api.AliceObjectOperator
import io.aliceplatform.api.Predicate
import io.aliceplatform.api.objects.BooleanProvider
import io.aliceplatform.api.objects.ListProvider
import io.aliceplatform.api.objects.NumberProvider
import io.aliceplatform.api.objects.Provider

interface DataStoreFactory : AliceObjectOperator {
  fun <DAO : IDao<T, ID>, T : IdObject<ID>, ID> create(dao: Class<DAO>): Provider<DAO>
}

interface IDao<T : IdObject<ID>, ID> : Iterable<T> {
  fun get(id: ID): Provider<T>
  fun any(predicate: Predicate<T>): BooleanProvider
  fun filter(predicate: Predicate<T>): ListProvider<T>
  fun findFirst(predicate: Predicate<T>): Provider<T>
  fun put(target: T): BooleanProvider
  fun remove(id: ID): BooleanProvider
  fun removeIf(predicate: Predicate<T>): NumberProvider
  fun listAll(): ListProvider<T>
}

interface IdObject<ID> {
  val id: ID
}

annotation class Query(val value: String)

annotation class TableName(val value: String)

annotation class Parameter(val value: String = "")
