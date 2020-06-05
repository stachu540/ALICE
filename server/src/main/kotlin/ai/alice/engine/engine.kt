package ai.alice.engine

import ai.alice.AliceHikari
import ai.alice.GlobalLogger
import ai.alice.api.Alice
import ai.alice.api.engine.Conditional
import ai.alice.api.engine.Engine
import ai.alice.api.engine.EngineProvider
import ai.alice.api.engine.module.ModuleIndex
import ai.alice.api.exception.AliceEngineException
import ai.alice.api.provider.Provider
import ai.alice.api.provider.provide
import ai.alice.descriptor.EngineDescriptor
import ai.alice.descriptor.ModuleDescriptor
import ai.alice.utils.forClassName
import ai.alice.utils.getProperties
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.safeCast
import kotlin.system.exitProcess

class DelegatedEngineProvider(
    override val alice: AliceHikari
) : EngineProvider {

    internal val index: Set<EngineIndex>

    init {
        val index = mutableSetOf<EngineIndex>()

        val engines = alice.getProperties("META-INF/alice/engines")
            .map { EngineDescriptor(it) }.let {
                val aliases = it.filter { it.alias != null }
                    .groupBy({ it.alias!! }, { it.id })
                it.filter { it.alias == null }
                    .map { EngineSpec(it.id, aliases[it.id] ?: emptyList(), forClassName(it.implementationClassName)) }
            }

        engines.forEach { spec ->
            val ids = (spec.aliases + spec.id).filter { id -> index.any { it.id == id || id in it.alias } }
            if (ids.isNotEmpty()) GlobalLogger.error(
                "Cannot register this engine: ${spec.implementation.canonicalName}",
                AliceEngineException("Those namespace has been taken: $ids")
            )
            else index += EngineIndex(spec.id, spec.aliases, spec.create(alice)).also { engineIndex ->
                alice.getProperties("META-INF/alice/modules")
                    .map { ModuleDescriptor(it) }
                    .filter {
                        it.requiredEngine in engineIndex.alias + engineIndex.id
                    }.map {
                        ModuleIndex<Engine<*, *, *>>(it)
                    }.forEach {
                        runBlocking {
                            engineIndex.instance.modules.register(it)
                        }
                    }
            }
        }

        this.index = index
    }


    override fun getById(id: String): Provider<Engine<*, *, *>> =
        provide(index.firstOrNull { it.id == id || id in it.alias }?.instance) { "Instance of $id is not exist" }

    @ExperimentalStdlibApi
    override fun <T : Engine<*, *, *>> getById(id: String, type: KClass<T>): Provider<T> =
        getById(id).flatMap {
            provide(type.safeCast(it)) { "Instance of $id is not a ${type.qualifiedName!!}" }
        }

    override val names: Set<String>
        get() = index.flatMap { it.alias + it.id }.toSet()

    @ExperimentalStdlibApi
    override fun <R : Engine<*, *, *>> get(type: KClass<R>): Provider<R> =
        provide(type.safeCast(index.firstOrNull { type.isInstance(it.instance) }))

    override val size: Int
        get() = index.size

    override fun contains(type: KClass<Engine<*, *, *>>): Boolean =
        index.any { type.isInstance(it.instance) }

    override fun isEmpty(): Boolean = index.isEmpty()

    override fun iterator(): Iterator<Engine<*, *, *>> =
        index.map { it.instance }.iterator()

    private fun namespaceCheck(id: String, exec: () -> Unit) =
        index.firstOrNull {
            it.id == id || id in it.alias
        }.let {
            if (it != null) throw AliceEngineException("This namespace has been taken by: ${it.classInstance.qualifiedName!!}")
            else exec()
        }

    @ExperimentalStdlibApi
    internal suspend fun init() {
        if (index.isEmpty()) {
            alice.logger.error("No engines has been exist. Shutting down now!")
            Thread.currentThread().interrupt()
            exitProcess(255)
        }

        index.forEach {
            if (it.hasRequirementsMet) {
                alice.logger.debug("Starting engine: ${it.id}")
                it.instance.start()
                alice.logger.info("Engine ${it.id} has been started")
            } else alice.logger.warn("Cannot launching engine: ${it.id}. Requirements has not been met")
        }
    }

    internal fun dispose() {
        index.forEach {
            with(it.instance) {
                if (isActive) {
                    alice.logger.debug("Stopping engine: ${it.id}")
                    stop()
                    alice.logger.info("Engine ${it.id} has been stopped")
                }
            }
        }
    }
}

data class EngineSpec(
    val id: String,
    val aliases: List<String>,
    val implementation: Class<Engine<*, *, *>>
) {
    fun create(alice: AliceHikari): Engine<*, *, *> =
        implementation.constructors.firstOrNull {
            it.parameterCount == 1 && it.parameterTypes.first().isAssignableFrom(Alice::class.java)
        }?.let {
            println(it.parameterCount)
            implementation.cast(it.newInstance(alice))
        }
            ?: throw IllegalAccessException("Cannot find any constructor with root instance parameter requirements! \"${Alice::class.java.canonicalName}\"")

    override fun toString(): String =
        "${id}[${implementation.canonicalName}]"

}

data class EngineIndex(
    val id: String,
    val alias: List<String>,
    val instance: Engine<*, *, *>
) {
    @ExperimentalStdlibApi
    val hasRequirementsMet: Boolean
        get() = classInstance.let {
            it.findAnnotation<Conditional>()!!.value.primaryConstructor?.let {
                if (it.parameters.isEmpty()) it.call().handle(instance)
                else throw AliceEngineException("[$id] Implementation of ValidatorLauncher requires empty args of constructor")
            }
                ?: throw AliceEngineException("[$id] This engine needs requirement checks to running. Please apply @Conditional annotation to Engine implementation")
        }
    val classInstance: KClass<Engine<*, *, *>>
        get() = instance.javaClass.kotlin

    internal fun matchedOf(id: String) = (alias + this.id).contains(id)

    override fun toString(): String =
        "${id}[${instance}]"
}