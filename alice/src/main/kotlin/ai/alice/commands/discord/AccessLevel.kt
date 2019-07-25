package ai.alice.commands.discord

import ai.alice.api.CommandAccess
import discord4j.core.`object`.util.Permission
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking

enum class AccessLevel(private val handler: suspend (MessageCreateEvent) -> Boolean) :
    CommandAccess<MessageCreateEvent> {
    BOT_OWNER({
        it.client.applicationInfo.map { app -> it.message.author.get().id == app.ownerId }.awaitSingle()
    }),
    SERVER_OWNER({
        it.guild.map { g -> g.ownerId == it.message.author.get().id }.awaitSingle()
    }),
    USER_ADMIN({
        it.member.get().basePermissions.map { it.contains(Permission.ADMINISTRATOR) }.awaitSingle()
    }),
    USER_MOD({
        it.member.get().basePermissions.map {
            it.any {
                setOf(
                    Permission.KICK_MEMBERS, Permission.BAN_MEMBERS,
                    Permission.MANAGE_GUILD, Permission.MANAGE_ROLES
                ).contains(it)
            }
        }.awaitSingle()
    }),
    EVERYONE({ true });

    override operator fun invoke(event: MessageCreateEvent): Boolean = runBlocking { handler(event) }
}