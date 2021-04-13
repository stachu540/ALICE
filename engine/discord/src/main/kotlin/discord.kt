package io.aliceplatform.engine.discord

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.rest.request.RouterOptions
import io.aliceplatform.api.Alice
import io.aliceplatform.api.Consumer
import io.aliceplatform.api.LateInit
import io.aliceplatform.api.engine.Engine
import io.aliceplatform.api.engine.command.Command
import io.aliceplatform.api.engine.command.CommandProvider
import java.nio.channels.NotYetConnectedException

//object DiscordClient : Engine.Factory<DiscordConfig> {
//  override val name = "discord"
//
//  override fun init(alice: Alice, config: DiscordConfig.() -> Unit): Engine<*, *, *> {
//    DiscordClientBuilder.create()
//  }
//}
//
//class DiscordEngine(
//  override val alice: Alice,
//  val builder: DiscordClientBuilder<DiscordClient, RouterOptions>
//) : Engine<GatewayDiscordClient, MessageCreateEvent, DiscordCommandEvent> {
//  val client: DiscordClient = builder.build()
//  override var root: GatewayDiscordClient by LateInit { NotYetConnectedException() }
//    private set
//  override val provider: CommandProvider<MessageCreateEvent, DiscordCommandEvent, Engine<GatewayDiscordClient, MessageCreateEvent, DiscordCommandEvent>>
//    get() = TODO("Not yet implemented")
//
//  override fun <T : MessageCreateEvent> onEvent(type: Class<T>, consumer: Consumer<T>) {
//    alice.events.onEvent(, )
//  }
//
//  override fun registerCommand(command: Command<MessageCreateEvent, DiscordCommandEvent>) {
//    TODO("Not yet implemented")
//  }
//
//  override fun unregisterCommand(name: String, aliased: Boolean) {
//    TODO("Not yet implemented")
//  }
//
//  override fun run() {
//    root = client.login().block()!!
//  }
//
//  override fun close() {
//    root.logout().block()
//  }
//}
//
//class DiscordConfig internal constructor() : Engine.Config {
//  override var token: String by LateInit { NullPointerException("Requires Bot Token!") }
//}
