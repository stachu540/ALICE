package ai.alice.api

import ai.alice.api.engine.EngineProvider
import ai.alice.api.provider.Provider
import ai.alice.api.store.DataStore
import com.typesafe.config.Config
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

interface Alice : CoroutineScope {
    val logger: Logger
    val configuration: Config
    val engines: EngineProvider
    val dataStore: DataStore

    fun configuration(configuration: Config.() -> Unit) {
        this.configuration.apply(configuration)
    }

    fun engines(engines: EngineProvider.() -> Unit) {
        this.engines.apply(engines)
    }

    fun dataStore(dataStore: DataStore.() -> Unit) {
        this.dataStore.apply(dataStore)
    }

    fun <T : Any> config(type: KClass<T>, path: String): Provider<T>
}

interface AliceObject : CoroutineScope {
    val alice: Alice
    override val coroutineContext: CoroutineContext
        get() = alice.coroutineContext
}

inline fun <reified T : Any> Alice.config(path: String) =
    config(T::class, path)