package ai.alice.internal.registry

import ai.alice.api.engine.EngineRegistry
import ai.alice.api.engine.IEngine
import ai.alice.internal.AliceEngine

class EngineRegistrySpec(override val rootEngine: AliceEngine) : EngineRegistry {

    private val engines: MutableSet<IEngine> = mutableSetOf()

    override fun <TConfig, TFactory : IEngine.Factory<TConfig, TEngine>, TEngine : IEngine> install(
        factory: TFactory,
        config: TConfig.() -> Unit
    ): TEngine = factory.configure(config).apply { this@EngineRegistrySpec.engines += this }

    override val size: Int
        get() = engines.size

    override fun contains(element: IEngine): Boolean = engines.contains(element)

    override fun containsAll(elements: Collection<IEngine>): Boolean = engines.containsAll(elements)

    override fun isEmpty(): Boolean = engines.isEmpty()

    override fun iterator(): Iterator<IEngine> = engines.iterator()
}
