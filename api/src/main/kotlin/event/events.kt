package io.aliceplatform.api.event

import io.aliceplatform.api.Alice

data class ServerStartEvent(
  override val alice: Alice
) : Event

data class ServerShutdownEvent(
  override val alice: Alice
) : Event

data class EngineStartEvent(
  override val alice: Alice
) : Event

data class EngineStoppedEvent(
  override val alice: Alice
) : Event

data class EngineActionEvent<E>(
  override val alice: Alice
) : Event

data class ModuleLoadedEvent(
  override val alice: Alice
) : Event

data class ModuleUnloadEvent(
  override val alice: Alice
) : Event
