package ai.alice.api

import ai.alice.api.http.HttpClient
import ai.alice.api.http.HttpServer
import ai.alice.api.service.EngineRegistry
import ai.alice.api.service.ModuleRegistry
import com.uchuhimo.konf.Config
import org.slf4j.Logger
import java.util.function.Consumer
import org.litote.kmongo.coroutine.CoroutineDatabase

interface Alice {
    val logger: Logger
    val config: Config
    val datastore: CoroutineDatabase
    val engines: EngineRegistry
    val modules: ModuleRegistry
    val web: HttpServer
    val client: HttpClient

    fun config(config: Config.() -> Unit) = config.invoke(this.config)
    fun datastore(datastore: CoroutineDatabase.() -> Unit) = datastore.invoke(this.datastore)
    fun engines(engines: EngineRegistry.() -> Unit) = engines.invoke(this.engines)
    fun modules(modules: ModuleRegistry.() -> Unit) = modules.invoke(this.modules)
    fun web(web: HttpServer.() -> Unit) = web.invoke(this.web)
    fun client(client: HttpClient.() -> Unit) = client.invoke(this.client)

    fun config(config: Consumer<Config>) = config { config.accept(this) }
    fun datastore(datastore: Consumer<CoroutineDatabase>) = datastore { datastore.accept(this) }
    fun engines(engines: Consumer<EngineRegistry>) = engines { engines.accept(this) }
    fun modules(modules: Consumer<ModuleRegistry>) = modules { modules.accept(this) }
    fun web(web: Consumer<HttpServer>) = web { web.accept(this) }
    fun client(client: Consumer<HttpClient>) = client { client.accept(this) }

    fun beforeStart(action: Alice.() -> Unit)
    fun afterStart(action: Alice.() -> Unit)
    fun beforeStop(action: Alice.() -> Unit)
    fun afterStop(action: Alice.() -> Unit)

    fun beforeStart(action: Consumer<Alice>) = beforeStart { action.accept(this) }
    fun afterStart(action: Consumer<Alice>) = afterStart { action.accept(this) }
    fun beforeStop(action: Consumer<Alice>) = beforeStop { action.accept(this) }
    fun afterStop(action: Consumer<Alice>) = afterStop { action.accept(this) }

    suspend fun start()
    suspend fun stop(force: Boolean = false)
}

interface RootComponent {
    val root: Alice
}