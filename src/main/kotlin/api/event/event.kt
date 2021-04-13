package io.aliceplatform.api.event

import io.aliceplatform.api.AliceObject
import io.aliceplatform.api.Transformer

interface EventManager {
  fun registerAnnotatedEvent(event: Any?)
  fun <E> registerEvent(dispatcher: EventDispatcher<E>)
  fun <E> onEvent(type: Class<E>): EventFlow<E>
}

interface Event : AliceObject

fun interface EventDispatcher<E> {
  fun handle(event: E)
}

interface EventFlow<E> {
  fun <R> flatMap(transformer: Transformer<E, Iterable<R>>): EventFlow<R>
  fun <R> map(transformer: Transformer<E, R>): EventFlow<R>
  fun <R : E> ofInstance(type: Class<R>): EventFlow<R>

  fun subscribe(subscription: EventSubscription<E>): Disposable
  fun subscribe(
    dispatcher: EventDispatcher<E> = EventDispatcher { }, error: EventDispatcher<Throwable> = EventDispatcher { }
  ): Disposable
}

interface EventSubscription<E> {
  fun onError(error: Throwable)
  fun onNext(event: E)
  fun onSubscribe()
  fun onComplete()
  fun onDisposable()
}

interface Disposable {
  val isDisposed: Boolean
  fun dispose()
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler
