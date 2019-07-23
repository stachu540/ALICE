package ai.alice.api.service

import ai.alice.api.Alice

typealias ModuleRegistry = ServiceManager<IModule>

interface IModule {
    fun register(root: Alice)
    fun unregister(root: Alice)
}

class SimpleModule(
    private val register: (Alice) -> Unit,
    private val unregister: (Alice) -> Unit
) : IModule {
    override fun register(root: Alice) = this.register.invoke(root)

    override fun unregister(root: Alice) = this.unregister.invoke(root)
}