package io.aliceplatform.server.event

import com.fasterxml.jackson.databind.type.SimpleType
import io.aliceplatform.api.event.Event
import io.aliceplatform.api.event.EventDispatcher
import io.aliceplatform.api.event.EventHandler
import io.aliceplatform.api.event.EventManager
import io.aliceplatform.server.DefaultAliceInstance

@Suppress("UNCHECKED_CAST")
class EventManagerImpl(override val alice: DefaultAliceInstance) : EventManager {
  private val events = mutableMapOf<Class<out Event>, List<EventDispatcher<in Event>>>()

  override fun registerAnnotatedEvent(event: Any) {
    if (event is EventDispatcher<*>) {
      registerEvent(event as EventDispatcher<in Event>)
    }

    event.javaClass.methods.filter {
      it.isAnnotationPresent(EventHandler::class.java) && it.parameterCount == 1
      it.parameterTypes.first().isAssignableFrom(Event::class.java)
    }.forEach { method ->
      val type = method.parameterTypes.first() as Class<out Event>
      registerEvent(type) {
        method.invoke(event, it)
      }
    }
  }

  override fun <E : Event> registerEvent(dispatcher: EventDispatcher<in E>) {
    val eventType = SimpleType.constructUnsafe(dispatcher.javaClass).containedTypeOrUnknown(0).rawClass as Class<E>
    registerEvent(eventType, dispatcher as EventDispatcher<in Event>)
  }

  private fun registerEvent(type: Class<out Event>, dispatcher: EventDispatcher<in Event>) {
    val dispatchers = events[type]?.toMutableList() ?: mutableListOf()
    dispatchers.add(dispatcher)
    events[type] = dispatchers
  }

  override fun <E : Event> onEvent(type: Class<E>, dispatcher: EventDispatcher<E>) {
    registerEvent(type, dispatcher as EventDispatcher<in Event>)
  }

  fun handle(event: Event) {
    events[event.javaClass]?.forEach {
      it.handle(event)
    }
  }
}
