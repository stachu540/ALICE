package ai.alice.api.engine

import ai.alice.api.Action
import ai.alice.api.ObjectCollection
import ai.alice.api.config.RootConfigurationProvider
import ai.alice.api.http.Handler
import ai.alice.api.http.HttpClient
import ai.alice.api.http.HttpServer
import ai.alice.api.module.ModuleRegistry
import me.liuwj.ktorm.database.Database

interface EngineRegistry : ObjectCollection<IEngine> {
    val rootEngine: RootEngine

    fun <TConfig, TFactory : IEngine.Factory<TConfig, TEngine>, TEngine : IEngine>
            install(factory: TFactory, config: TConfig.() -> Unit): TEngine
}

interface IEngine {
    val rootEngine: RootEngine

    val httpClient: HttpClient
    val datastore: Database

    val engine: IEngine
        get() = this

    val modules: ModuleRegistry
    val engines: EngineRegistry
    val configuration: RootConfigurationProvider

    fun modules(action: Action<ModuleRegistry>) = action.execute(modules)
    fun engines(action: Action<EngineRegistry>) = action.execute(engines)
    fun configuration(action: Action<RootConfigurationProvider>) = action.execute(configuration)

    fun modules(action: (ModuleRegistry) -> Unit) = action(modules)
    fun engines(action: (EngineRegistry) -> Unit) = action(engines)
    fun configuration(action: (RootConfigurationProvider) -> Unit) = action(configuration)

    interface Factory<TConfig, TEngine : IEngine> {
        val name: String
        fun configure(config: TConfig.() -> Unit = {}): TEngine
    }
}

interface RootEngine : IEngine {
    val httpServer: HttpServer
    override val engine: RootEngine
        get() = this

    fun <TConfig, TFactory : IEngine.Factory<TConfig, TEngine>, TEngine : IEngine>
            install(factory: TFactory, config: TConfig.() -> Unit) = engines.install(factory, config)

    fun registerEndpoint(endpoint: String, handler: Handler.() -> Unit) = httpServer.register(endpoint, handler)
}