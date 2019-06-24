package ai.alice.internal

import ai.alice.api.AliceException
import ai.alice.api.config.RootConfigurationProvider
import ai.alice.api.engine.EngineRegistry
import ai.alice.api.engine.RootEngine
import ai.alice.api.http.HttpClient
import ai.alice.api.module.ModuleRegistry
import ai.alice.internal.config.RootParameterConfigurationProvider
import ai.alice.internal.data.DbLogger
import ai.alice.internal.http.ImplementedHttpServer
import ai.alice.internal.http.OkHttpClient
import ai.alice.internal.registry.EngineRegistrySpec
import ai.alice.internal.registry.ModuleRegistrySpec
import ai.alice.utils.L
import me.liuwj.ktorm.database.Database
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager

class AliceEngine(override val configuration: RootConfigurationProvider) : RootEngine {
    override val httpServer: ImplementedHttpServer
        get() = ImplementedHttpServer(this)
    override val rootEngine: RootEngine
        get() = this
    override val httpClient: HttpClient
        get() = OkHttpClient(this)
    override val datastore: Database
        get() = if (this::ds.isInitialized) ds else throw AliceException("Run main engine before call this endpoint")
    override val modules: ModuleRegistry
        get() = ModuleRegistrySpec(this)
    override val engines: EngineRegistry
        get() = EngineRegistrySpec(this)
    override val engine: RootEngine
        get() = this

    private lateinit var ds: Database
    private lateinit var conn: Connection

    fun start() {
        L.info("Initialize Database!")
        Class.forName(configuration.get("database.driver").asString.get())
        conn = DriverManager.getConnection(configuration.get("database.url").asString.get()).apply {
            L.info("Connection Established! - ${this.catalog}")
        }
        ds = Database.connect(logger = DbLogger(LoggerFactory.getLogger("ai.alice.data"))) {
            conn
        }.apply {
            L.info("Database Initialized!")
        }
        L.info("Starting HTTP Server!")
        httpServer.run().apply {
            L.info("Server has been started on ${httpServer.bindAddress}:${httpServer.port}")
        }
    }

    fun stop() {
        conn.close()
        httpServer.close()
        (configuration as RootParameterConfigurationProvider).store()
    }
}