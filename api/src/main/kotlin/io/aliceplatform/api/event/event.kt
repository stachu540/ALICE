package io.aliceplatform.api.event

import io.aliceplatform.api.AliceObject

interface EventManager : AliceObject {
  fun registerAnnotatedEvent(event: Any)
  fun <E : Event> registerEvent(dispatcher: EventDispatcher<in E>)
  fun <E : Event> onEvent(type: Class<E>, dispatcher: EventDispatcher<E>)
}

interface Event : AliceObject

fun interface EventDispatcher<E : Event> {
  fun handle(event: E)
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler
