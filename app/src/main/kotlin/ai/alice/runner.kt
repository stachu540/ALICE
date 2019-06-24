package ai.alice

import ai.alice.api.config.RootConfigurationProvider
import ai.alice.internal.AliceEngine
import ai.alice.internal.config.RootParameterConfigurationProvider
import ai.alice.utils.L
import com.fasterxml.jackson.databind.MappingJsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.jasonclawson.jackson.dataformat.hocon.HoconFactory
import java.io.File

object Alice : CliktCommand() {
    val config by option("--config", "-c", help = "Configuration File - Allowed extensions: yml, yaml, json, conf, xml")
        .file()
        .default(File("config.yml"))
//        .validate {
//            require(it.isFile && listOf("yml", "yaml", "json", "conf", "xml").contains(it.extension.toLowerCase()))
//            { "Unknown Extension ${it.extension}. Required Extension yml, yaml, json, conf, xml" }
//        }

    val port by option("--port", "-p", help = "", envvar = "PORT").int().default(8080)
    val jdbcUrl by option("--jdbc", help = "", hidden = true, envvar = "JDBC_URL")
        .validate {
            require(it.matches(Regex(""))) { "Required JDBC Patter URI" }
        }

    override fun run() {
        val mapper = ObjectMapper(
            when (config.extension) {
                "yml", "yaml" -> YAMLFactory()
                "conf" -> HoconFactory()
                "xml" -> XmlFactory()
                "json" -> MappingJsonFactory()
                else -> MappingJsonFactory()
            }
        )
        L.info("Checking file existence!")
        if (!config.exists()) {
            L.info("File doesn't not exist. Creating them!!!")
            config.createNewFile()
            val node = mapper.createObjectNode().apply {
                set("server", mapper.createObjectNode().apply {
                    set("port", IntNode(port))
                    set("bind", TextNode("0.0.0.0"))
                })

                set("database", mapper.createObjectNode().apply {
                    set("driver", TextNode("org.h2.Driver"))
                    set("url", TextNode(jdbcUrl ?: "jdbc:h2:file:alice.db"))
                })
            }
            mapper.writeValue(config, node)
        }
        composeProvider(mapper).let {
            AliceEngine(it).run {
                L.info("Starting ALICE!!!")
                start().also {
                    Runtime.getRuntime().addShutdownHook(Thread(this::stop))
                }.apply {
                    L.info("Alice has been started with ${modules.size} Modules and ${engines.size} Bot Engines!")
                }
            }
        }


    }

    private fun composeProvider(mapper: ObjectMapper): RootConfigurationProvider =
        RootParameterConfigurationProvider(mapper, config).apply {
            if (jdbcUrl != null) {
                set("database.url", TextNode(jdbcUrl!!))
            }
        }
}

fun main(args: Array<String>) = Alice.main(args)