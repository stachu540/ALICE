package ai.alice.api.store

import ai.alice.api.AliceObject
import ai.alice.api.provider.Provider
import java.io.Serializable
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.criteria.CriteriaQuery
import kotlin.reflect.KClass

interface DataStore : AliceObject {
    fun <T : IdObject<ID>, ID : Serializable> create(type: Class<T>): DaoObject<T, ID>
    fun <T : IdObject<ID>, ID : Serializable> create(type: KClass<T>): DaoObject<T, ID> = create(type.java)
}

interface DaoObject<T : IdObject<ID>, ID : Serializable> {
    suspend fun contains(id: ID): Boolean
    suspend fun get(id: ID): Provider<T>
    suspend fun purge(): Provider<Boolean>
    suspend fun delete(id: ID): Provider<T>
    suspend fun save(data: T)
    suspend fun find(condition: CriteriaQuery<T>.() -> Unit): Iterable<T>
}

@MappedSuperclass
interface IdObject<ID : Serializable> {
    @get:Id
    val id: ID
}