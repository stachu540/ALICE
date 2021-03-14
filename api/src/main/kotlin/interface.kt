package ai.alice.api

fun interface Consumer<T> {
  fun consume(target: T)
}

fun interface Transformer<IN, OUT> {
  fun transform(input: IN): OUT
}

fun interface Predicate<T> {
  fun predict(spec: T): Boolean
}

fun interface Supplier<T> {
  @Throws(Exception::class)
  fun get(): T
}

fun interface Runnable {
  @Throws(Exception::class)
  fun run()
}

interface Closeable {
  val isClosed: Boolean

  @Throws(Exception::class)
  fun close()
}

interface Application : Runnable, Closeable {
  val isActive: Boolean
}

interface AliceObject {
  val root: Alice
}

interface AliceInstanceObject : AliceObject, Application
