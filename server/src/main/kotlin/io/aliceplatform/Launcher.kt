package io.aliceplatform

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import io.aliceplatform.api.Alice
import io.aliceplatform.api.AliceDsl
import io.aliceplatform.server.DefaultAliceInstance
import java.io.File
import java.util.*
import org.slf4j.LoggerFactory
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.system.exitProcess

object Launcher : CliktCommand(
  """
  Launching the server instance
""".trimIndent(), """
  If some configuration for specific engine is not defined, server will be shutting down immediately with exit code 1
""".trimIndent(), "alice", true, autoCompleteEnvvar = null, treatUnknownOptionsAsArgs = true
) {

  private val DEFAULT_FILE = File("alice.kts")

  private val logger = LoggerFactory.getLogger(Launcher::class.java)

  val createFile by option("-F", "--create-file").flag()
    .help("Generate configuration file if it is not exist")

  val configFile: File by option("-c", "--config")
    .help("Configuration file location")
    .file(mustBeReadable = true, mustExist = true)
    .default(DEFAULT_FILE)
    .validate {
      if (it.extension.toLowerCase() != "kts") fail("Your configuration must be a Kotlin Script file (*.kts) - default: alice.kts")
    }

  override fun run() {
    logger.info("Starting server")
    initBeforeCompose()
    logger.info("Load configuration")
    validateFile()
    logger.info("Prepare Alice instance")
    val alice = prepare()
    if (alice.engines.isEmpty) {
      exit(126, "No engines has been specified")
    }
    logger.info("Starting Alice Instance")
    alice.run()
    logger.info("Alice Has been started")
    Runtime.getRuntime().addShutdownHook(Thread {
      alice.close()
    })
  }

  private fun exit(code: Int = 0, reason: String? = null) {
    if (reason != null && code > 0) {
      logger.warn(reason)
      logger.warn("Server is going shutdown!")
    }
    exitProcess(code)
  }

  private fun prepare(): DefaultAliceInstance {
    val classLoader = javaClass.classLoader
    val alice = DefaultAliceInstance(classLoader)

    val result = classLoader.evalFile(alice)
    if (result is ResultWithDiagnostics.Failure) {
      result.reports.forEach {
        logger.error(it.message, it.exception)
      }
      exit(255)
    }

    return alice.also {
      it.initialize()
    }
  }

  private fun validateFile() {
    logger.debug("Checking file existence")
    if (!configFile.exists()) {
      if (createFile) {
        logger.warn("Creating configuration file!")
        if (!configFile.parentFile.exists()) {
          configFile.parentFile.mkdirs()
        }
        configFile.createNewFile()
        exit(1, "Configuration file has been created at: ${configFile.absolutePath}")
      } else {
        exit(126, "No configuration file has been provided: ${configFile.absolutePath}")
      }
    }
    logger.debug("Checking file access")
    if (!configFile.canRead()) {
      exit(126, "Cannot read this file: ${configFile.absolutePath}")
    }
    logger.debug("File exist and can be read.")
    logger.debug("Continue to initialization.")
  }

  private fun initBeforeCompose() {
    logger.debug("initialize system properties for this instance")
    val systemProp = javaClass.classLoader.getResourceAsStream("META-INF/alice/system.properties")
    if (systemProp != null) {
      val properties = Properties().also {
        it.load(systemProp) // load defaults
        it.putAll(System.getProperties()) // append properties from globals
      }
      System.getProperties().putAll(properties) // inject and append global properties
    }
  }

  private fun ClassLoader.evalFile(alice: Alice): ResultWithDiagnostics<EvaluationResult> = run {
    BasicJvmScriptingHost().eval(
      configFile.toScriptSource(),
      createJvmCompilationConfigurationFromTemplate<SimpleScript> {
        getResources("META-INF/alice/imports.txt").toList()
          .flatMap {
            it.openStream().use {
              it.reader().readLines()
            }
          }
        defaultImports.putIfNotNull(getResources("META-INF/alice/imports.txt").toList()
          .flatMap {
            it.openStream().use {
              it.reader().readLines()
            }
          })
        jvm {
          dependenciesFromClassloader(classLoader = this@evalFile, wholeClasspath = true)
        }
      },
      ScriptEvaluationConfiguration {
        jvm {
          constructorArgs.put(listOf(alice))
        }
      })
  }
}

@AliceDsl
@KotlinScript(filePathPattern = "(?:.+\\.)?alice\\.kts")
abstract class SimpleScript(alice: Alice) : Alice by alice

fun main(args: Array<String>) {
  Launcher.main(args)
}
