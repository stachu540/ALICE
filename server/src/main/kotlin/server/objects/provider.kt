package io.aliceplatform.server.objects

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.deser.ValueInstantiator
import com.fasterxml.jackson.databind.deser.std.MapDeserializer
import com.fasterxml.jackson.databind.deser.std.ReferenceTypeDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.ReferenceType
import com.fasterxml.jackson.databind.type.SimpleType
import com.fasterxml.jackson.databind.type.TypeBindings
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.databind.type.TypeModifier
import com.fasterxml.jackson.databind.util.NameTransformer
import io.aliceplatform.api.AliceObject
import io.aliceplatform.api.Consumer
import io.aliceplatform.api.Supplier
import io.aliceplatform.api.Transformer
import io.aliceplatform.api.objects.IterableProvider
import io.aliceplatform.api.objects.MapProvider
import io.aliceplatform.api.objects.ObjectFactory
import io.aliceplatform.api.objects.Provider
import io.aliceplatform.server.DefaultAliceInstance
import io.aliceplatform.server.toJacksonVersion
import java.lang.reflect.Type

internal abstract class AbstractProvider<T> : Provider<T> {
  override fun <R : Any> map(transformer: Transformer<T, R>): Provider<R> =
    MapperProviderImpl(this, transformer)

  override fun <R : Any> flatMap(transformer: Transformer<T, Provider<R>>): Provider<R> =
    FlatMapperProviderImpl(this, transformer)
}

internal class ProviderImpl<T : Any>(
  private val value: Result<T>
) : AbstractProvider<T>() {
  override val isPresent: Boolean
    get() = value.isSuccess

  override fun get(): T = value.getOrThrow()

  override fun getOrElse(default: T): T = value.getOrDefault(default)

  override fun getOrNull(): T? = value.getOrNull()

  override fun <X : Throwable> getOrThrow(throwable: Supplier<X>): T = value.getOrNull() ?: throw throwable.get()

  override fun ifPresent(consumer: Consumer<T>) {
    value.getOrNull()?.let {
      consumer.consume(it)
    }
  }

  override fun toString() =
    "Provider${
      try {
        "(${value.getOrThrow()})"
      } catch (e: Throwable) {
        "[${e::class.java.canonicalName!!}]"
      }
    }"
}

internal class IterableProviderImpl<T>(
  private val value: List<T>
) : AbstractProvider<Iterable<T>>(), IterableProvider<T>, Iterable<T> by value {
  override val isPresent: Boolean = true

  override fun get(): Iterable<T> = value

  override fun getOrElse(default: Iterable<T>): Iterable<T> = value

  override fun getOrNull(): Iterable<T>? = value

  override fun <X : Throwable> getOrThrow(throwable: Supplier<X>): Iterable<T> = value

  override fun ifPresent(consumer: Consumer<Iterable<T>>) {
    consumer.consume(value)
  }

  override val size: Int
    get() = value.size
  override val isEmpty: Boolean
    get() = value.isEmpty()

  override fun get(index: Int): T = value.get(index)

  override fun getOrNull(index: Int): T? = value.getOrNull(index)

  override fun getOrElse(index: Int, default: T): T = value.getOrElse(index) { default }

  override fun <X : Throwable> getOrThrow(index: Int, throwable: Supplier<X>): T =
    getOrNull(index) ?: throw throwable.get()

  override fun <R : Any> mapIterable(transformer: Transformer<T, R>): IterableProvider<R> =
    IterableProviderImpl(value.map(transformer::transform))

  override fun <R : Any> flatMapIterable(transformer: Transformer<T, Iterable<R>>): IterableProvider<R> =
    IterableProviderImpl(value.flatMap(transformer::transform))
}

internal class MapProviderImpl<K, V>(
  private val value: Map<K, V>
) : AbstractProvider<Map<K, V>>(), MapProvider<K, V> {
  override val isPresent: Boolean = true

  override fun get(): Map<K, V> = value

  override fun getOrElse(default: Map<K, V>): Map<K, V> = value

  override fun getOrNull(): Map<K, V>? = value

  override fun <X : Throwable> getOrThrow(throwable: Supplier<X>): Map<K, V> = value

  override fun ifPresent(consumer: Consumer<Map<K, V>>) {
    consumer.consume(value)
  }

  override val keys: IterableProvider<K>
    get() = IterableProviderImpl(value.keys.toList())
  override val values: IterableProvider<V>
    get() = IterableProviderImpl(value.values.toList())
  override val entries: IterableProvider<Map.Entry<K, V>>
    get() = IterableProviderImpl(value.entries.toList())

  override fun get(key: K): V =
    value.getValue(key)

  override fun getOrNull(key: K): V? =
    value[key]

  override fun getOrElse(key: K, default: V): V =
    value.getOrDefault(key, default)

  override fun <X : Throwable> getOrThrow(key: K, throwable: Supplier<X>): V =
    getOrNull(key) ?: throw throwable.get()

  override fun <R : Any> mapKeys(transformer: Transformer<K, R>): MapProvider<R, V> =
    MapProviderImpl(value.mapKeys { transformer.transform(it.key) })

  override fun <R : Any> mapValues(transformer: Transformer<V, R>): MapProvider<K, R> =
    MapProviderImpl(value.mapValues { transformer.transform(it.value) })

  override fun <R : Any> mapIterable(transformer: Transformer<Map.Entry<K, V>, R>): IterableProvider<R> =
    entries.mapIterable(transformer)

  override fun <R : Any> flatMapIterable(transformer: Transformer<Map.Entry<K, V>, Iterable<R>>): IterableProvider<R> =
    entries.flatMapIterable(transformer)
}

internal class MapperProviderImpl<R, T>(
  private val provider: Provider<R>,
  private val mapper: Transformer<R, T>
) : AbstractProvider<T>() {
  override val isPresent: Boolean
    get() = provider.isPresent

  override fun get(): T = mapper.transform(provider.get())

  override fun getOrElse(default: T): T =
    getOrNull() ?: default

  override fun getOrNull(): T? =
    provider.getOrNull()?.let(mapper::transform)

  override fun <X : Throwable> getOrThrow(throwable: Supplier<X>): T =
    getOrNull() ?: throw throwable.get()

  override fun ifPresent(consumer: Consumer<T>) {
    provider.ifPresent {
      consumer.consume(mapper.transform(it))
    }
  }
}

internal class FlatMapperProviderImpl<R, T>(
  private val provider: Provider<R>,
  private val mapper: Transformer<R, Provider<T>>
) : AbstractProvider<T>() {
  override val isPresent: Boolean
    get() = provider.isPresent

  override fun get(): T = mapper.transform(provider.get()).get()

  override fun getOrElse(default: T): T =
    getOrNull() ?: default

  override fun getOrNull(): T? =
    provider.getOrNull()?.let(mapper::transform)?.getOrNull()

  override fun <X : Throwable> getOrThrow(throwable: Supplier<X>): T =
    getOrNull() ?: throw throwable.get()

  override fun ifPresent(consumer: Consumer<T>) {
    provider.ifPresent { mapper.transform(it).ifPresent(consumer) }
  }
}

class AliceProviderModule(
  override val alice: DefaultAliceInstance
) : Module(), AliceObject {
  override fun version(): com.fasterxml.jackson.core.Version = alice.version.toJacksonVersion()

  override fun getModuleName(): String = "AliceProviderModule"

  override fun setupModule(context: SetupContext) {
    context.addSerializers(ProviderSerializer())
    context.addDeserializers(ProviderDeserializer(alice.objects))

    context.addTypeModifier(ProviderTypeModifier())
  }
}

private class ProviderSerializer : Serializers.Base() {
  override fun findReferenceSerializer(
    config: SerializationConfig,
    type: ReferenceType,
    beanDesc: BeanDescription,
    contentTypeSerializer: TypeSerializer?,
    contentValueSerializer: JsonSerializer<Any>
  ): JsonSerializer<*>? {
    val raw = type.rawClass
    return if (Provider::class.java.isAssignableFrom(raw)) {
      val staticTyping = (contentTypeSerializer == null) &&
        config.isEnabled(MapperFeature.USE_STATIC_TYPING)
      Base(type, staticTyping, contentTypeSerializer, contentValueSerializer)
    } else null
  }

  private class Base : ReferenceTypeSerializer<Provider<*>> {

    constructor(
      type: ReferenceType, staticTyping: Boolean, contentTypeSerializer: TypeSerializer?,
      contentValueSerializer: JsonSerializer<Any>
    ) : super(
      type, staticTyping, contentTypeSerializer, contentValueSerializer
    )

    constructor(
      base: Base, property: BeanProperty,
      vts: TypeSerializer, valueSerializer: JsonSerializer<*>, unwrapper: NameTransformer,
      suppressableValue: Any?, suppressNulls: Boolean
    ) : super(base, property, vts, valueSerializer, unwrapper, suppressableValue, suppressNulls)

    override fun withResolved(
      prop: BeanProperty,
      vts: TypeSerializer,
      valueSer: JsonSerializer<*>,
      unwrapper: NameTransformer
    ): ReferenceTypeSerializer<Provider<*>> = Base(
      this, prop, vts, valueSer, unwrapper, _suppressableValue, _suppressNulls
    )

    override fun withContentInclusion(
      suppressableValue: Any?,
      suppressNulls: Boolean
    ): ReferenceTypeSerializer<Provider<*>> =
      Base(this, _property, _valueTypeSerializer, _valueSerializer, _unwrapper, suppressableValue, suppressNulls)

    override fun _isValuePresent(value: Provider<*>): Boolean =
      value.isPresent

    override fun _getReferenced(value: Provider<*>): Any =
      value.get()!!

    override fun _getReferencedIfPresent(value: Provider<*>): Any? =
      value.getOrNull()
  }
}

@Suppress("DEPRECATION", "UNCHECKED_CAST")
private class ProviderDeserializer(
  private val objects: ObjectFactory
) : Deserializers.Base() {
  override fun findReferenceDeserializer(
    refType: ReferenceType,
    config: DeserializationConfig,
    beanDesc: BeanDescription,
    contentTypeDeserializer: TypeDeserializer,
    contentDeserializer: JsonDeserializer<*>
  ): JsonDeserializer<*>? =
    when {
      refType.hasRawClass(Provider::class.java) -> Base(
        refType, null, contentTypeDeserializer,
        contentDeserializer, objects
      )
      refType.hasRawClass(IterableProvider::class.java) -> Iterate(
        CollectionType.construct(List::class.java, refType.containedTypeOrUnknown(0)),
        objects
      )
      refType.hasRawClass(MapProvider::class.java) -> Mapped(
        MapType.construct(Map::class.java, refType.containedTypeOrUnknown(0), refType.containedTypeOrUnknown(1)),
        objects
      )
      else -> null
    }

  private class Base(
    fullType: JavaType, inst: ValueInstantiator?,
    typeDeser: TypeDeserializer, deser: JsonDeserializer<*>,
    private val objects: ObjectFactory
  ) : ReferenceTypeDeserializer<Provider<*>>(
    fullType, inst, typeDeser, deser
  ) {
    override fun getNullValue(ctxt: DeserializationContext): Provider<*> =
      objects.empty<Any>()

    override fun withResolved(
      typeDeser: TypeDeserializer,
      valueDeser: JsonDeserializer<*>
    ): ReferenceTypeDeserializer<Provider<*>> =
      Base(_fullType, _valueInstantiator, typeDeser, valueDeser, objects)

    override fun referenceValue(contents: Any?): Provider<*> =
      objects.ofNullable(contents)

    override fun updateReference(reference: Provider<*>?, contents: Any?): Provider<*> =
      objects.ofNullable(contents)

    override fun getReferenced(reference: Provider<*>): Any =
      reference.get()!!
  }

  private class Iterate(
    type: JavaType, private val objects: ObjectFactory
  ) : StdDeserializer<IterableProvider<*>>(type) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IterableProvider<*> =
      objects.many(ctxt.findNonContextualValueDeserializer(valueType).deserialize(p, ctxt) as List<*>)
  }

  private class Mapped(
    type: JavaType, private val objects: ObjectFactory
  ) : StdDeserializer<MapProvider<*, *>>(type) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MapProvider<*, *> =
      objects.map(ctxt.findNonContextualValueDeserializer(valueType).deserialize(p, ctxt) as Map<Any, Any>)
  }
}

private class ProviderTypeModifier : TypeModifier() {
  override fun modifyType(
    type: JavaType,
    jdkType: Type?,
    context: TypeBindings?,
    typeFactory: TypeFactory?
  ): JavaType {
    if (type.isReferenceType || type.isContainerType) {
      return type
    }
    val raw = type.rawClass

    if (Provider::class.java.isAssignableFrom(raw)) {
      return ReferenceType.upgradeFrom(type, type.containedTypeOrUnknown(0))
    }

    return type
  }
}
