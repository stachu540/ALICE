package ai.alice.store

import ai.alice.AliceHikari
import ai.alice.api.LateInit
import ai.alice.api.provider.Provider
import ai.alice.api.provider.provide
import ai.alice.api.store.DaoObject
import ai.alice.api.store.DataStore
import ai.alice.api.store.IdObject
import ai.alice.di.ClassOperator
import ai.alice.di.inject
import ai.alice.utils.format
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hibernate.dialect.Dialect
import java.io.IOException
import java.io.Serializable
import java.io.UncheckedIOException
import java.net.URL
import java.sql.Driver
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.SharedCacheMode
import javax.persistence.ValidationMode
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.spi.ClassTransformer
import javax.persistence.spi.PersistenceUnitInfo
import javax.persistence.spi.PersistenceUnitTransactionType
import javax.sql.DataSource
import kotlin.reflect.KClass


class PersistenceDataStore(override val alice: AliceHikari) : DataStore {
    private val factory: EntityManagerFactory by ClassOperator.inject()

    private var manager: EntityManager by LateInit { "EntityManager is not loaded yet" }

    override fun <T : IdObject<ID>, ID : Serializable> create(type: Class<T>): DaoObject<T, ID> =
        DaoObjectImpl(type, manager, alice)

    suspend fun start() = alice.launch {
        this@PersistenceDataStore.manager = factory.createEntityManager()
    }.join()

    fun stop() {
        manager.transaction.commit()
        manager.close()
    }
}

class DaoObjectImpl<T : IdObject<ID>, ID : Serializable>(
    private val type: Class<T>,
    private val manager: EntityManager,
    private val alice: AliceHikari
) : DaoObject<T, ID> {
    override suspend fun contains(id: ID): Boolean =
        withContext(alice.coroutineContext) {
            manager.transaction.begin()
            (manager.find(type, id) != null).also {
                manager.transaction.commit()
            }
        }

    override suspend fun get(id: ID): Provider<T> =
        withContext(alice.coroutineContext) {
            try {
                manager.transaction.begin()
                provide<T>(manager.getReference(type, id)).also {
                    manager.transaction.commit()
                }
            } catch (e: Exception) {
                provide<T>(null, e)
            }
        }


    override suspend fun purge(): Provider<Boolean> =
        withContext(alice.coroutineContext) {
            try {
                manager.transaction.begin()
                provide<Boolean>(
                    manager.createQuery(manager.criteriaBuilder.createCriteriaDelete(type))
                        .executeUpdate() > 0
                ).also {
                    manager.transaction.commit()
                }
            } catch (e: Exception) {
                provide<Boolean>(null, e)
            }
        }

    override suspend fun delete(id: ID): Provider<T> = withContext(alice.coroutineContext) {
        try {
            manager.transaction.begin()
            provide<T>(manager.find(type, id)).also {
                it.ifPresent { manager.remove(it) }
                manager.transaction.commit()
            }
        } catch (e: Exception) {
            provide<T>(null, e)
        }
    }

    override suspend fun save(data: T) {
        withContext(alice.coroutineContext) {
            manager.transaction.begin()
            manager.persist(data)
            manager.transaction.commit()
        }
    }

    override suspend fun find(condition: CriteriaQuery<T>.() -> Unit): Iterable<T> =
        withContext(alice.coroutineContext) {
            manager.transaction.begin()
            val query = manager.criteriaBuilder.createQuery(type).apply(condition)
            manager.createQuery(query).resultList.also {
                manager.transaction.commit()
            }
        }

}

data class JpaConfig(
    val type: DatabaseType,
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String
) {
    fun getRaw(): String = type.format(host, port, database)

    override fun toString(): String = getRaw()

    enum class DatabaseType(
        internal val format: String,
        internal val driverClass: KClass<out Driver>,
        internal val dialect: KClass<out Dialect>,
        internal val defaultPort: Int
    ) {
        MYSQL(
            "jdbc:mysql://{host_port}/{database}",
            com.mysql.cj.jdbc.Driver::class,
            org.hibernate.dialect.MySQLDialect::class,
            3306
        ),

        @Suppress("DEPRECATION")
        POSTGRES(
            "jdbc:postgresql://{host_port}/{database}",
            org.postgresql.Driver::class,
            org.hibernate.dialect.PostgreSQLDialect::class,
            5432
        ),
        SQLITE(
            "jdbc:sqlite:{host}",
            org.sqlite.JDBC::class,
            SqliteDialect::class,
            -1
        ),
        SQLSERVER(
            "jdbc:postgresql://{host_port};databaseName={database}",
            com.microsoft.sqlserver.jdbc.SQLServerDriver::class,
            org.hibernate.dialect.SQLServerDialect::class,
            1433
        ),
        H2(
            "jdbc:h2:{host}:{database}",
            org.h2.Driver::class,
            org.hibernate.dialect.H2Dialect::class,
            -1
        );

        fun format(host: String, port: Int, database: String): String {
            val m = mutableMapOf<String, String>()
            m["host"] = host

            if (port != defaultPort) {
                m["port"] = "$port"
            }

            m["host_port"] = buildString {
                append(host)
                if (port != defaultPort) {
                    append(":$port")
                }
            }

            m["database"] = database

            return format.format(m)
        }
    }
}

object AlicePersistenceUnitInfo : PersistenceUnitInfo {
    override fun getPersistenceUnitName(): String {
        return "AlicePersistenceUnit"
    }

    override fun getPersistenceProviderClassName(): String {
        return "org.hibernate.jpa.HibernatePersistenceProvider"
    }

    override fun getTransactionType(): PersistenceUnitTransactionType {
        return PersistenceUnitTransactionType.RESOURCE_LOCAL
    }

    override fun getJtaDataSource(): DataSource? {
        return null
    }

    override fun getNonJtaDataSource(): DataSource? {
        return null
    }

    override fun getMappingFileNames(): List<String> {
        return emptyList()
    }

    override fun getJarFileUrls(): List<URL> {
        return try {
            Collections.list(
                this.javaClass
                    .classLoader
                    .getResources("")
            )
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    override fun getPersistenceUnitRootUrl(): URL? {
        return null
    }

    override fun getManagedClassNames(): List<String> {
        return emptyList()
    }

    override fun excludeUnlistedClasses(): Boolean {
        return false
    }

    override fun getSharedCacheMode(): SharedCacheMode? {
        return null
    }

    override fun getValidationMode(): ValidationMode? {
        return null
    }

    override fun getProperties(): Properties {
        return Properties()
    }

    override fun getPersistenceXMLSchemaVersion(): String? {
        return null
    }

    override fun getClassLoader(): ClassLoader? {
        return null
    }

    override fun addTransformer(transformer: ClassTransformer?) {}

    override fun getNewTempClassLoader(): ClassLoader? {
        return null
    }
}