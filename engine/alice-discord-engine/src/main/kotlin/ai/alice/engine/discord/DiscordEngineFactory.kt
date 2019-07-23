package ai.alice.engine.discord

import ai.alice.api.Alice
import ai.alice.api.config.AliceConfigSpec
import ai.alice.api.service.IFactory
import com.google.auto.service.AutoService
import discord4j.core.DiscordClientBuilder

@AutoService(IFactory::class)
class DiscordEngineFactory : IFactory<DiscordEngine> {
    override val id: String
        get() = "ai.alice.discord"

    override fun apply(root: Alice): DiscordEngine =
        DiscordEngine(root, DiscordClientBuilder(root.config[AliceConfigSpec.EngineSpec.DiscordConfigSpec.bot]).apply {

        }.build())
}