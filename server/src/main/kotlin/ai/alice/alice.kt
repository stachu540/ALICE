package ai.alice

import ai.alice.api.Alice
import ai.alice.api.config
import ai.alice.api.exception.AliceEngineException
import ai.alice.api.provider.Provider
import ai.alice.api.provider.provide
import ai.alice.di.ClassOperator
import ai.alice.di.get
import ai.alice.engine.DelegatedEngineProvider
import ai.alice.store.AlicePersistenceUnitInfo
import ai.alice.store.JpaConfig
import ai.alice.store.PersistenceDataStore
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.hibernate.jpa.HibernatePersistenceProvider
import org.slf4j.Logger
import kotlin.reflect.KClass
import kotlin.system.exitProcess

class AliceHikari(scope: CoroutineScope = GlobalScope, override val configuration: Config) : Alice,
    CoroutineScope by scope {

    @ExperimentalStdlibApi
    constructor(configuration: Config) : this(LightPriest.createScope(configuration), configuration)

    override val logger: Logger = GlobalLogger.of<Alice>()

    override val engines: DelegatedEngineProvider = DelegatedEngineProvider(this)

    override val dataStore: PersistenceDataStore
        get() = ClassOperator.get()

    internal val mapper: ObjectMapper
        get() = ClassOperator.get()

    internal val classLoader: ClassLoader
        get() = ClassOperator.get()

    @ExperimentalStdlibApi
    fun start() {
        runBlocking(coroutineContext) {
            logger.info("Starting Alice")
            startCheck()
            preStart()
            dataStore.start()
            engines.init()
        }
    }

    fun stop() {
        logger.info("Stopping Alice")
        engines.dispose()
        dataStore.stop()
        logger.info("Goodbye!")
    }

    fun stop(exitCode: Int) {
        stop()
        exitProcess(exitCode)
    }

    @ExperimentalStdlibApi
    private fun startCheck() {
        storageCheck()
        enginesCheck()
    }

    private fun storageCheck() {
        logger.info("Checking storage configuration")
        require(configuration.hasPath("alice.store")) { "[alice.store] Requires store configuration" }
        require(configuration.hasPath("alice.store.type")) { "[alice.store.type] Requires store type" }
        require(configuration.hasPath("alice.store.host")) { "[alice.store.host] Requires store host / file path" }
        require(configuration.getString("alice.store.type") in JpaConfig.DatabaseType.values().map { it.name }) {
            "[alice.store.type] Only this type has been supported ${JpaConfig.DatabaseType.values().map { it.name }}"
        }
        logger.info("Storage configuration: OK")
    }

    @ExperimentalStdlibApi
    private fun enginesCheck() {
        logger.info("Checking engines configuration")
        require(engines.index.isNotEmpty()) { "No engines has been exist." }
        logger.warn("Those engines cannot be starting:")
        engines.index.filterNot { it.hasRequirementsMet }.forEach {
            logger.warn(" - ${it.id}")
        }
        if (engines.index.none { it.hasRequirementsMet }) {
            throw AliceEngineException("No engines has been meeting requirements.").also {
                logger.error("Engines configuration: ERROR", it)
            }
        } else if (!engines.index.all { it.hasRequirementsMet }) {
            logger.warn("Engines configuration: FINE")
        } else {
            logger.info("Engines configuration: OK")
        }
    }

    private fun preStart() {
        ClassOperator.register {
            val db by config<JpaConfig>("alice.store")
            HibernatePersistenceProvider().createContainerEntityManagerFactory(
                AlicePersistenceUnitInfo,
                mapOf<Any, Any>(
                    "hibernate.dialect" to db.type.dialect.qualifiedName!!,
                    "javax.persistence.jdbc.driver" to db.type.driverClass.qualifiedName!!,
                    "javax.persistence.jdbc.url" to db.getRaw(),
                    "javax.persistence.jdbc.user" to db.username,
                    "javax.persistence.jdbc.password" to db.password
                )
            )
        }
    }

    override fun <T : Any> config(type: KClass<T>, path: String): Provider<T> =
        try {
            provide(mapper.convertValue(configuration.getObject(path), type.java))
        } catch (e: Exception) {
            provide(null, e)
        }
}