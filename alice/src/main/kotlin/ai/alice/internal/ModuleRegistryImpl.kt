package ai.alice.internal

import ai.alice.api.Alice
import ai.alice.api.service.IModule
import ai.alice.api.service.ServiceManager

class ModuleRegistryImpl(root: Alice) : ServiceManagerImpl<IModule>(root)
