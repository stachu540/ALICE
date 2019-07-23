package ai.alice

import ai.alice.api.Alice
import ai.alice.api.config.AliceConfigSpec
import ai.alice.api.http.HttpClient
import ai.alice.api.http.HttpServer
import ai.alice.api.service.EngineRegistry
import ai.alice.api.service.IEngine
import ai.alice.api.service.IFactory
import ai.alice.api.service.IModule
import ai.alice.api.service.ModuleRegistry
import ai.alice.internal.EngineRegistryImpl
import ai.alice.internal.ModuleRegistryImpl
import ai.alice.internal.http.HttpClientImpl
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.DefaultLoaders
import com.uchuhimo.konf.source.hocon.toHocon
import com.uchuhimo.konf.source.json.toJson
import com.uchuhimo.konf.source.properties.toProperties
import com.uchuhimo.konf.source.toml.toToml
import com.uchuhimo.konf.source.xml.toXml
import com.uchuhimo.konf.source.yaml.toYaml
import io.jooby.Kooby
import io.jooby.ServerOptions
import io.jooby.handlebars.HandlebarsModule
import io.jooby.json.JacksonModule
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.reflect.jvm.jvmName
import kotlin.system.exitProcess


class Root(override val config: Config, override val logger: Logger) : Alice {
    override val datastore: CoroutineDatabase
        get() = dbClient?.getDatabase(config[AliceConfigSpec.StorageSpec.database])
            ?: throw IllegalAccessException("Database is not yet connected")
    override val engines: EngineRegistry = EngineRegistryImpl(this)
    override val modules: ModuleRegistry = ModuleRegistryImpl(this)
    override val web: HttpServer = Kooby(this::prepareServer)

    private var dbClient: CoroutineClient? = null

    override val client: HttpClient = HttpClientImpl(this)

    private val beforeStart: MutableSet<Alice.() -> Unit> = mutableSetOf()
    private val afterStart: MutableSet<Alice.() -> Unit> = mutableSetOf()
    private val beforeStop: MutableSet<Alice.() -> Unit> = mutableSetOf()
    private val afterStop: MutableSet<Alice.() -> Unit> = mutableSetOf()

    override fun beforeStart(action: Alice.() -> Unit) {
        beforeStart += action
    }

    override fun afterStart(action: Alice.() -> Unit) {
        afterStart += action
    }

    override fun beforeStop(action: Alice.() -> Unit) {
        beforeStop += action
    }

    override fun afterStop(action: Alice.() -> Unit) {
        afterStop += action
    }

    override suspend fun start() {
        this.beforeStart.forEach { this.apply(it) }
        initServices()
        val hostPort = config[AliceConfigSpec.StorageSpec.host] + ":" + config[AliceConfigSpec.StorageSpec.port]
        val port = config[AliceConfigSpec.ServerSpec.port]
        dbClient = KMongo.createClient("mongodb://$hostPort").coroutine
        dbClient!!.startSession().startTransaction()
        engines.startAll()
        web.start()
        this.afterStart.forEach { this.apply(it) }
        logger.info("A.L.I.C.E is initialized with server port: $port")
    }

    @Suppress("UNCHECKED_CAST")
    private fun initServices() {
        logger.info("Initialize Services")
        ServiceLoader.load(IFactory::class.java).forEach {
            val type = it.javaClass.genericSuperclass as Class<*>
            when {
                IModule::class.java.isAssignableFrom(type) -> {
                    logger.info("Register module: {}", it.id)
                    modules.register(it as IFactory<out IModule>)
                }
                IEngine::class.java.isAssignableFrom(type) -> {
                    logger.info("Register engine: {}", it.id)
                    engines.register(it as IFactory<out IEngine<*, *>>)
                }
                else -> logger.warn(
                    "Factor '{}' is not assignable for specific Service '{}'",
                    it::class.jvmName,
                    type.name
                )
            }
        }
    }

    override suspend fun stop(force: Boolean) {
        logger.info("Initialize{} shutdown", if (force) " forced" else "")
        this.beforeStop.forEach { this.apply(it) }
        dbClient?.close()
        dbClient = null
        web.stop()
        engines.stopAll(force)
        this.afterStop.forEach { this.apply(it) }
        logger.info("A.L.I.C.E is going down NOW!")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            AliceRunner().main(args)
        }
    }

    private fun prepareServer(jooby: Kooby) {
        jooby.apply {
            install(JacksonModule(ObjectMapper().findAndRegisterModules()))
            install(HandlebarsModule())
            setServerOptions(ServerOptions().apply {
                server = "Netty"
                port = this@Root.config[AliceConfigSpec.ServerSpec.port]
            })
        }
    }
}

class AliceRunner : CliktCommand() {
    val configGenerate by option("--generate", "-G", help = "Generate Config File and Exit.").flag()
    val cfgFile: File by option("--config", "-c", help = "Configuration File").file().default(File("config.yml"))
    val port by option("--port", "-p", help = "Server Port").int()
    val dbUrl by option("--database", "-d", help = "Mongo Database URL")
    val discordToken by option("--discord-token", "-D", help = "Disord Bot Token")

    val logger = LoggerFactory.getLogger(Root::class.java)

    override fun run() {
        logger.info("Starting A.L.I.C.E")

        val config = Config {
            addSpec(AliceConfigSpec)
            ServiceLoader.load(ConfigSpec::class.java).forEach {
                addSpec(it)
            }
            from.loadFile()
            from.systemProperties()
            from.env()
        }

        runBlocking {
            Root(config, logger).apply {
                Runtime.getRuntime().addShutdownHook(Thread {
                    runBlocking {
                        this@apply.stop()
                    }
                })
            }.start()
        }
    }

    private fun DefaultLoaders.loadFile() {
        if (!cfgFile.exists() || configGenerate) {
            if (!cfgFile.exists()) {
                logger.error("Configuration File is not exist!")
            }
            logger.error("Starting provide new configuration using defaults!")
            config.saveFile()
            logger.error("Please update configuration file before launching again.")
            logger.error("Your configuration file is in: ${cfgFile.absolutePath}")
            logger.error("Now I am going down now.")
            exitProcess(0)
        } else {
            when (cfgFile.extension) {
                "conf" -> hocon.file(cfgFile)
                "json" -> json.file(cfgFile)
                "properties" -> properties.file(cfgFile)
                "toml" -> toml.file(cfgFile)
                "xml" -> xml.file(cfgFile)
                "yml", "yaml" -> yaml.file(cfgFile)
            }
        }
    }

    private fun Config.saveFile() {
        when (cfgFile.extension) {
            "conf" -> toHocon.toFile(cfgFile)
            "json" -> toJson.toFile(cfgFile)
            "properties" -> toProperties.toFile(cfgFile)
            "toml" -> toToml.toFile(cfgFile)
            "xml" -> toXml.toFile(cfgFile)
            "yml", "yaml" -> toYaml.toFile(cfgFile)
        }
    }
}
