package ai.alice.api.engine.module

import ai.alice.api.AliceObject
import ai.alice.api.engine.Engine
import ai.alice.api.exception.AliceModuleException
import ai.alice.api.provider.CollectionProvider
import ai.alice.api.provider.Provider
import ai.alice.api.provider.provide
import ai.alice.descriptor.ModuleDescriptor
import com.typesafe.config.Config
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

interface ModuleProvider<TEngine : Engine<*, *, *>> : AliceObject, CollectionProvider<Module<TEngine>> {
    val engine: TEngine

    val names: Set<String>

    fun getById(id: String): Provider<Module<TEngine>>

    fun <T : Module<TEngine>> getById(id: String, type: KClass<T>): Provider<T>

    fun apply(id: String)

    fun <T : Module<TEngine>> apply(type: KClass<T>)

    suspend fun register(index: ModuleIndex<*>)
}

interface Module<TEngine : Engine<*, *, *>> {
    suspend fun handle(engine: TEngine)
}

abstract class AbstractModuleProvider<TEngine : Engine<*, *, *>>(
    final override val engine: TEngine
) : ModuleProvider<TEngine>, AliceObject by engine {

    private val index: MutableSet<ModuleIndex<TEngine>> = mutableSetOf()

    private val appliedIndex
        get() = index.filter { it.isApplied }

    override val size: Int
        get() = appliedIndex.size

    override val names: Set<String>
        get() = appliedIndex.map { it.id }.toSet()

    override fun getById(id: String): Provider<Module<TEngine>> =
        provide(index.firstOrNull { it.id == id }?.instance) { "Couldn't find a $id" }

    @ExperimentalStdlibApi
    override fun <T : Module<TEngine>> getById(id: String, type: KClass<T>): Provider<T> =
        getById(id).flatMap {
            provide(type.safeCast(it)) { "Instance of $id is not a ${type.qualifiedName!!}" }
        }

    override fun isEmpty(): Boolean = index.isEmpty()

    override fun contains(type: KClass<Module<TEngine>>): Boolean = index.any { it.isInstance(type) }

    override fun iterator(): Iterator<Module<TEngine>> = index.map { it.instance }.iterator()

    @ExperimentalStdlibApi
    override fun <R : Module<TEngine>> get(type: KClass<R>): Provider<R> = provide(type.safeCast(index.firstOrNull {
        type.isInstance(it)
    })) { "Cannot find instance of ${type.qualifiedName!!}" }

    @Suppress("UNCHECKED_CAST")
    override suspend fun register(index: ModuleIndex<*>) {
        val required = index.requiredIds.filterNot { it in names }
        if (index.id in names) throw AliceModuleException("This module is already initialized: ${index.id}")
        if (required.isNotEmpty()) throw AliceModuleException("Module initialization failed. Requirements not be met: $required")
        else this.index += (index as ModuleIndex<TEngine>)
    }

    override fun apply(id: String) {
        index.firstOrNull {
            it.id == id
        }?.applyIndex() ?: throw AliceModuleException("[$id] Provided ID has been not found.")
    }

    override fun <T : Module<TEngine>> apply(type: KClass<T>) {
        index.firstOrNull { it.isInstance(type) }?.applyIndex() ?: throw AliceModuleException("[${type.qualifiedName}] Provided type has been not found.")
    }

    inline fun <reified T : Module<TEngine>> apply() = apply(T::class)

    private fun ModuleIndex<TEngine>.applyIndex() {
        alice.launch{
            val ids = requiredIds.filterNot { it in names }
            if (ids.isNotEmpty()) throw AliceModuleException("[$id] Applied module needs applying modules: $ids")
            if (isApplied) throw AliceModuleException("[$id] Module is already applied.")
            alice.logger.debug("Initialize module: $id")
            instance.handle(engine)
            alice.logger.info("Module: $id has been initialized")
            isApplied = true
        }.start()
    }

    @Suppress("UNCHECKED_CAST", "EXTENSION_SHADOWED_BY_MEMBER")
    val ModuleDescriptor.implementationClass: KClass<Module<TEngine>>
        get() = Class.forName(implementationClassName).kotlin as KClass<Module<TEngine>>

    abstract fun initialize()
}

data class ModuleIndex<TEngine : Engine<*, *, *>>(
    val id: String,
    val requiredIds: Set<String>,
    val instance: Module<TEngine>
) {
    var isApplied: Boolean = false
        internal set

    @Suppress("UNCHECKED_CAST")
    constructor(descriptor: ModuleDescriptor) : this(
        descriptor.id,
        descriptor.requiredModules.toSet(),
        Class.forName(descriptor.implementationClassName).newInstance() as Module<TEngine>
    )

    fun isInstance(type: KClass<*>): Boolean = type.isInstance(instance)
}

