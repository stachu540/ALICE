package ai.alice.api

import ai.alice.api.provider.NamedObjectProvider
import java.util.*
import kotlin.reflect.KClass

interface ObjectCollection<T : Any> : Collection<T> {
    fun <S : T> withType(type: Class<S>): ObjectCollection<S> =
        filter { type.isAssignableFrom(it.javaClass) } as ObjectCollection<S>

    fun <S : T> withType(type: KClass<S>): ObjectCollection<S> = withType(type.java)
    fun <S : T> withType(type: Class<S>, action: Action<S>) = withType(type).map { action.execute(it) }
    fun <S : T> withType(type: KClass<S>, action: Action<S>) = withType(type.java, action)
    fun matching(spec: Spec<T>): Boolean = any { spec.isSatisfiedBy(it) }
    fun all(action: Action<T>) = forEach { action.execute(it) }
}

interface NamedObjectCollection<T : Any> : ObjectCollection<T> {
    val asMap: SortedMap<String, T>
    val names: SortedSet<String>
        get() = asMap.keys.toSortedSet()
    override val size: Int
        get() = asMap.size

    fun findByName(name: String): T? = asMap[name]
    @Throws(UnknownDomainObjectException::class)
    fun getByName(name: String): T =
        findByName(name) ?: throw UnknownDomainObjectException("Cannot find specific object on this name")

    @Throws(UnknownDomainObjectException::class)
    fun getByName(name: String, action: Action<T>): T = getByName(name).apply { action.execute(this) }

    override fun <S : T> withType(type: Class<S>): NamedObjectCollection<S> =
        filter { type.isAssignableFrom(type.javaClass) } as NamedObjectCollection<S>

    override fun <S : T> withType(type: KClass<S>): NamedObjectCollection<S> = withType(type.java)

    @Throws(UnknownDomainObjectException::class)
    fun named(name: String): NamedObjectProvider<T>

    @Throws(UnknownDomainObjectException::class)
    fun <S : T> named(name: String, type: Class<S>): NamedObjectProvider<S>

    @Throws(UnknownDomainObjectException::class)
    fun <S : T> named(name: String, type: KClass<S>): NamedObjectProvider<S> = named(name, type.java)

    override fun contains(element: T): Boolean = asMap.values.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = asMap.values.containsAll(elements)

    override fun isEmpty(): Boolean = asMap.isEmpty()

    override fun iterator(): Iterator<T> = asMap.values.iterator()
}

