package ai.alice

import ai.alice.di.ClassOperator
import ai.alice.di.get
import ai.alice.engine.DelegatedEngineProvider
import ai.alice.jackson.ConfigObjectFormat
import ai.alice.store.PersistenceDataStore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import java.io.File
import kotlin.reflect.cast
import kotlin.system.exitProcess

object LightPriest : CliktCommand("Alice Server Instance", "Foo more details please check the wiki page", "alice") {

    val cfgFile by option("--config", "-C").file(
        canBeFile = true,
        canBeDir = false,
        mustExist = false
    ).default(File("alice.conf"))
        .validate {
            if (it.extension != "conf") fail("File must be a .conf file extension")
            if (!it.exists()) {
                message("Instance has been launching first time.")
                message("Creating new configuration file at path ${it.absolutePath}")
                generateFileWithDefaults(it)
            } else {
                if (it.length() == 0L) fail("Configuration is empty")
            }
        }

    private val base
        get() = ConfigFactory.parseResourcesAnySyntax("alice.base")

    private val defaults: Config
        get() = ConfigFactory.parseResourcesAnySyntax("defaults")


    private fun generateFileWithDefaults(file: File) {
        file.createNewFile()
        file.writeText(
            defaults.root().render(ConfigRenderOptions.defaults().setOriginComments(false).setJson(false)),
            Charsets.UTF_8
        )
        System.err.run {
            println("Configuration file has been created!")
            println("Before relaunch instance please refer to the ${file.absolutePath}")
            println("If you have any questions about running instance go to the wiki page.")
            println("Otherwise go to the issue tab and create your issue.")
        }
        exitProcess(0)
    }

    @ExperimentalStdlibApi
    override fun run() {
        ClassOperator.register { createConfig() }
        ClassOperator.register { Thread.currentThread().contextClassLoader }
        ClassOperator.register { PersistenceDataStore(get()) }
        ClassOperator.register {
            ObjectMapper().findAndRegisterModules()
                .registerModule(
                    SimpleModule()
                        .addSerializer(ConfigValue::class.java, ConfigObjectFormat.Serializer())
                        .addDeserializer(ConfigValue::class.java, ConfigObjectFormat.Deserializer())
                )
        }
        ClassOperator.register {
            AliceHikari(get())
        }
        ClassOperator.register { DelegatedEngineProvider(get()) }
        ClassOperator.get<AliceHikari>().run {
            start()
        }
    }

    private fun useDefaultScope() = GlobalScope.also {
        GlobalLogger.debug("Using default CoroutineScope")
    }

    private fun createConfig(): Config =
        ConfigFactory.parseFile(cfgFile)
            .withFallback(defaults)
            .withFallback(base)

    @ExperimentalStdlibApi
    internal fun createScope(config: Config): CoroutineScope =
        if (config.hasPathOrNull("alice.coroutine.class")) {
            val clz = config.getString("alice.coroutine.class")
            if (!clz.isNotBlank()) {
                try {
                    CoroutineScope::class.cast(Class.forName(clz).newInstance())
                } catch (nf: ClassNotFoundException) {
                    GlobalLogger.warn("Cannot find a specific class", nf)
                    useDefaultScope()
                } catch (cast: ClassCastException) {
                    GlobalLogger.warn("Cannot cast a specific class", cast)
                    useDefaultScope()
                }
            } else {
                GlobalLogger.warn("Cannot use empty / blank strings to cast those class. Back to default CoroutineScope")
                useDefaultScope()
            }
        } else useDefaultScope()
}

fun main(args: Array<String>) {
    LightPriest.main(args)
}