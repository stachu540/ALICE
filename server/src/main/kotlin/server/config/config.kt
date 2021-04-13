package io.aliceplatform.server.config

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.typesafe.config.ConfigFactory
import io.aliceplatform.api.config.ArrayNode
import io.aliceplatform.api.config.ConfigPath
import io.aliceplatform.api.config.ConfigurationProvider
import io.aliceplatform.api.config.Node
import io.aliceplatform.api.config.ObjectNode
import io.aliceplatform.api.config.PropertyPath
import io.aliceplatform.api.objects.Provider
import io.aliceplatform.server.DefaultAliceInstance
import io.aliceplatform.server.registerAliceModules
import java.io.File
import java.io.Reader
import java.nio.file.Path
import java.util.*

class ConfigurationProviderImpl(
  override val alice: DefaultAliceInstance
) : ConfigurationProvider {

  private val propertyMapper = JavaPropsMapper()
    .registerKotlinModule()
    .registerAliceModules(alice)

  private val global: ObjectNode = propertyMapper
    .convertValue(System.getProperties())
  private var local: ObjectNode = ObjectNodeImpl(emptyMap())

  init {

  }

  override fun getGlobalProperty(path: String): Provider<Node> =
    alice.objects.of(_getPropertyGlobal(path))

  override fun getEnvironment(env: String): Provider<String> =
    alice.objects.ofNullable(System.getenv(env))

  override fun getProperty(path: String): Provider<Node> =
    alice.objects.of(_getProperty(path))

  override fun <T : Any> get(path: String, type: Class<T>): Provider<T> {
    var value = local.getPath(path)
    if (value.isNull) {
      value = global.getPath(path)
    }

    return alice.objects.convert(value, type)
  }

  override fun apply(config: Properties) {
    local = propertyMapper.convertValue(config)
  }

  override fun apply(configPath: File) {
    val extension = getResolver(configPath.extension)
    apply(extension(configPath.reader()))
  }

  override fun apply(configPath: Path) {
    apply(configPath.toFile())
  }

  override fun apply(configPath: String) {
    apply(File(configPath))
  }

  private fun getResolver(extension: String): (Reader) -> Properties = {
    if (extension.toLowerCase() == "conf") {
      val cfg = ConfigFactory.parseReader(it)
      propertyMapper.convertValue(cfg.root().unwrapped())
    } else {
      val tree = when (extension.toLowerCase()) {
        "yml", "yaml" -> YAMLMapper()
        "toml" -> TomlMapper()
        "json" -> JsonMapper()
        "properties" -> propertyMapper
        else -> throw UnsupportedOperationException("This file extension is not supported: $extension. Supported file extensions: [ yml, yaml, toml, conf, json ]")
      }.readTree(it)
      propertyMapper.convertValue(tree)
    }
  }

  private fun _getProperty(path: String): Node = local.getPath(path)

  private fun _getPropertyGlobal(path: String): Node = global.getPath(path)

  private fun _null() = NullNodeImpl(alice.objects)

  private fun ObjectNode.getPath(path: String): Node {
    var value: Node = _null()

    for (p in path.split('.').withIndex()) {
      value = if (p.index == 0) {
        global[p.value] ?: _null()
      } else {
        when {
          value is ArrayNode && p.value.toIntOrNull() != null -> value[p.value.toInt()]
          value is ObjectNode -> value[p.value] ?: _null()
          else -> _null()
        }
      }
    }

    return value
  }

  internal fun verify() {

  }
}
