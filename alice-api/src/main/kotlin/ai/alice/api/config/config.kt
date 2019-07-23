package ai.alice.api.config

import com.uchuhimo.konf.ConfigSpec

object AliceConfigSpec : ConfigSpec("alice") {
    val sysCommandName by optional("alice", description = "System Command Name")
    val sysCommandAlias by optional(listOf("sys", "system"), description = "System Command Name")

    object EngineSpec : ConfigSpec("engines") {
        object DiscordConfigSpec : ConfigSpec("discord") {
            val enabled by optional(true, description = "Active module")
            val clientId by required<String>(description = "Client ID")
            val clientSecret by required<String>(description = "Client Secret")
            val bot by required<String>(description = "Bot Token")
            val botPrefix by optional("a!", "prefix", "Default Bot Prefix")
            val canMention by optional(true, description = "Allow to use mention to listen command")
        }

        object TwitchConfigSpec : ConfigSpec("twitch") {
            val enabled by optional(false, description = "Active module")
            val clientId by required<String>(description = "Client ID")
            val clientSecret by required<String>(description = "Client Secret")
            val channels by required<Collection<String>>(description = "Listen Channel")
            val bot by required<Credential>(description = "Bot Token")
            val botPrefix by optional("!", "prefix", "Default Bot Prefix")
        }
    }

    object ServerSpec : ConfigSpec("server") {
        val port by optional(8080, description = "Default Server Port")
        val bindAddress by optional("0.0.0.0", description = "IP Address to Binding")
    }

    object StorageSpec : ConfigSpec("store") {
        val host by optional("localhost", description = "DataStore Address")
        val port by optional(27015, description = "DataStore Address")
        val database by optional("alice", description = "DataStore Address")
    }
}

data class Credential(val accessToken: String, val refreshToken: String)