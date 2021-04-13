package io.aliceplatform.api

import java.util.*

fun interface Predicate<T> {
  fun test(target: T): Boolean

  infix fun and(predicate: Predicate<T>): Predicate<T> = Predicate {
    this.test(it) and predicate.test(it)
  }

  infix fun or(predicate: Predicate<T>): Predicate<T> = Predicate {
    this.test(it) or predicate.test(it)
  }

  infix fun xor(predicate: Predicate<T>): Predicate<T> = Predicate {
    this.test(it) xor predicate.test(it)
  }

  operator fun not(): Predicate<T> = Predicate {
    this.test(it).not()
  }
}

fun interface Consumer<T> {
  fun consume(target: T)

  fun append(consumer: Consumer<T>): Consumer<T> = Consumer {
    this.consume(it)
    consumer.consume(it)
  }
}

fun interface Supplier<T> {
  @Throws(Exception::class)
  fun get(): T
}

fun interface Transformer<IN, OUT> {
  fun transform(input: IN): OUT
}

fun interface Runnable {
  @Throws(RuntimeException::class)
  fun run()
}

fun interface Closeable {
  @Throws(RuntimeException::class)
  fun close()
}

interface AliceObject {
  val alice: Alice
}

interface AliceObjectOperator : AliceObject, Runnable, Closeable

interface Version : Comparable<Version> {
  val major: Int
  val minor: Int
  val patch: Int
  val release: Release?

  enum class Release {
    SNAPSHOT, ALPHA, BETA
  }

  companion object {
    private val MATCHER = Regex("([0-9]+\\.){1,2}[0-9]+(?:-.+)?")

    @JvmStatic
    fun current() = of(System.getProperty("alice.platform.version"))

    @JvmStatic
    @JvmOverloads
    fun of(major: Int, minor: Int, patch: Int, release: Release? = null): Version =
        VersionImpl(major, minor, patch, release)

    @JvmStatic
    fun of(version: String): Version {
      if (version.matches(MATCHER)) {
        var (major, minor, patch) = version.split('.')
        var release: String? = null

        if (patch.contains('-')) {
          val pSplit = patch.split('-', limit = 2)
          patch = pSplit[0]
          release = pSplit[1]
        }

        return of(major.toInt(), minor.toInt(), patch.toInt(), release?.let { Release.valueOf(it.toUpperCase()) })
      } else {
        throw IllegalArgumentException("Version string doesn't even matched to [<major>.<minor>.<patch>] - actual: $version")
      }
    }
  }
}

private class VersionImpl(
  override val major: Int,
  override val minor: Int,
  override val patch: Int,
  override val release: Version.Release?
) : Version {

  override fun compareTo(other: Version): Int =
    when {
      this === other -> 0
      major != other.major -> major.compareTo(other.major)
      minor != other.minor -> minor.compareTo(other.minor)
      patch != other.patch -> patch.compareTo(other.patch)
      release != null && other.release != null && release != other.release -> release.compareTo(other.release!!)
      else -> 0
    }

  override fun equals(other: Any?): Boolean = when {
    other != null && other is Version -> other.compareTo(this) == 0
    else -> false
  }

  override fun hashCode(): Int {
    return Objects.hash(major, minor, patch, release)
  }

  override fun toString(): String {
    return "$major.$minor.$patch${if (release != null) "-$release" else ""}"
  }
}

@DslMarker
annotation class AliceDsl
