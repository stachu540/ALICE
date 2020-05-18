package ai.alice.api.engine

import ai.alice.api.AliceObject
import ai.alice.api.engine.command.*
import ai.alice.api.engine.module.ModuleProvider
import ai.alice.api.provider.CollectionProvider
import ai.alice.api.provider.Provider
import kotlin.reflect.KClass

interface EngineProvider : AliceObject, CollectionProvider<Engine<*, *, *>> {
    val names: Set<String>
    fun getById(id: String): Provider<Engine<*, *, *>>
    fun <T : Engine<*, *, *>> getById(id: String, type: KClass<T>): Provider<T>
}

interface Engine<TMessage, TEvent : CommandEvent<TMessage, *>, TEngine : Engine<TMessage, TEvent, TEngine>> :
    AliceObject {
    val provider: CommandProvider<TMessage, TEvent>
    val modules: ModuleProvider<TEngine>
    val isActive: Boolean

    fun <E : Any> on(type: KClass<E>, handler: suspend (E) -> Unit)

    fun command(
        vararg names: String,
        description: String? = null,
        accessor: CommandAccess<TEvent> = { true },
        group: CommandGroup<TEvent> = group("unknown"),
        exec: suspend Command<TEvent>.(TEvent) -> Unit
    )

    suspend fun start()
    fun stop()

    interface ValidatorLauncher {
        fun handle(engine: Engine<*, *, *>): Boolean
    }
}

annotation class Conditional(
    val value: KClass<out Engine.ValidatorLauncher>
)

inline fun <reified E : Any, TMessage, TEvent : CommandEvent<TMessage, *>, TEngine : Engine<TMessage, TEvent, TEngine>>
        TEngine.on(noinline handler: suspend (E) -> Unit) = on(E::class, handler)

inline fun <reified T : Engine<*, *, *>> EngineProvider.gettingById(id: String) = getById(id, T::class)