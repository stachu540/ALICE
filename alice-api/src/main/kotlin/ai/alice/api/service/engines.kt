package ai.alice.api.service

import ai.alice.api.RootComponent
import ai.alice.api.command.CommandRegistry
import com.uchuhimo.konf.Config
import java.util.function.Consumer
import kotlin.reflect.KClass

interface EngineRegistry : ServiceManager<IEngine<*, *>> {
    suspend fun startAll()
    suspend fun <E : IEngine<*, *>> start(type: Class<E>)
    suspend fun <E : IEngine<*, *>> start(type: KClass<E>) = start(type.java)

    suspend fun stopAll(force: Boolean = false)
    suspend fun <E : IEngine<*, *>> stop(force: Boolean = false, type: Class<E>)
    suspend fun <E : IEngine<*, *>> stop(force: Boolean = false, type: KClass<E>) = stop(force, type.java)
}

interface IEngine<T, E : Any> : RootComponent {
    val client: T
    val config: Config
    val isRunning: Boolean
    val commands: CommandRegistry<IEngine<T, E>, *>

    fun <R : E> doOn(event: Class<R>, consumer: R.() -> Unit)
    fun <R : E> doOn(event: KClass<R>, consumer: R.() -> Unit) = doOn(event.java, consumer)

    fun <R : E> doOn(event: Class<R>, consumer: Consumer<R>) = doOn(event) { consumer.accept(this) }
    fun <R : E> doOn(event: KClass<R>, consumer: Consumer<R>) = doOn(event.java) { consumer.accept(this) }

    suspend fun start()
    suspend fun stop(force: Boolean = false)
}