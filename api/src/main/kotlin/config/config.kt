package io.aliceplatform.api.config

import io.aliceplatform.api.AliceObject
import io.aliceplatform.api.objects.Provider

interface ConfigurationProvider : AliceObject {
  fun getEnvironment(env: String): Provider<String>
  fun getProperty(path: String): Provider<Node>
}

annotation class ConfigPath(val value: String)
annotation class ConfigOption(
  val options: Array<String>,
  val separatorRegex: String = "\\s+",
  val description: String = "",
  val required: Boolean = false
)

annotation class ConfigArgument(
  val index: Int,
  val count: Int = 1,
  val description: String = "",
  val required: Boolean = false,
)

annotation class ConfigEnvironment(val value: String = "")
