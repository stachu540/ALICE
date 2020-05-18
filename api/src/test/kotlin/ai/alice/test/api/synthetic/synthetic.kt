package ai.alice.test.api.synthetic

import ai.alice.api.Alice
import ai.alice.api.engine.Engine
import ai.alice.api.engine.command.*
import ai.alice.api.engine.module.AbstractModuleProvider
import ai.alice.api.engine.module.Module
import ai.alice.api.engine.module.ModuleProvider
import ai.alice.api.exception.AliceEngineException
import ai.alice.api.store.IdObject
import ai.alice.test.api.TestUtil
import java.nio.channels.AlreadyConnectedException
import java.nio.channels.NotYetConnectedException
import javax.persistence.Entity
import javax.persistence.Table
import kotlin.reflect.KClass

class SyntheticEngine(override val alice: Alice) : Engine<SyntheticMessage, SyntheticCommandEvent, SyntheticEngine> {
    override val provider: CommandProvider<SyntheticMessage, SyntheticCommandEvent>
        get() = SyntheticProvider("!")
    override val modules: ModuleProvider<SyntheticEngine>
        get() = SyntheticModuleProvider(this)

    override var isActive: Boolean = false
        private set

    override fun <E : Any> on(type: KClass<E>, handler: suspend (E) -> Unit) {
        TODO("not implemented")
    }

    override fun command(
        vararg names: String,
        description: String?,
        accessor: CommandAccess<SyntheticCommandEvent>,
        group: CommandGroup<SyntheticCommandEvent>,
        exec: suspend Command<SyntheticCommandEvent>.(SyntheticCommandEvent) -> Unit
    ) {
        provider += TestUtil.createCommand(
            names.first(),
            names.toList().subList(1, names.size - 1).toTypedArray(),
            description,
            accessor,
            group,
            exec
        )
    }

    override suspend fun start() {
        if (isActive) throw AliceEngineException("Engine is already started", AlreadyConnectedException())
        isActive = true
    }

    override fun stop() {
        if (!isActive) throw AliceEngineException("Engine is already stopped", NotYetConnectedException())
        isActive = false
    }
}

class SyntheticMessage(
    val message: String
) {

}

class SyntheticCommandEvent(override val raw: SyntheticMessage, override val prefix: String) :
    CommandEvent<SyntheticMessage, Unit> {
    override val message: Unit
        get() = Unit
    override val rawMessage: String
        get() = raw.message
    override val command: String
        get() = rawMessage.split(" ").first().substring(prefix.length)
    override val args: Array<String>
        get() = rawMessage.split(" ").drop(1).toTypedArray()

    override suspend fun respond(message: String) {
        TODO("not implemented")
    }

    override suspend fun dm(message: String) {
        TODO("not implemented")
    }
}

class SyntheticModule : Module<SyntheticEngine> {
    override suspend fun handle(engine: SyntheticEngine) {
        engine.command("test") {
            it.respond("test")
        }
    }
}

class SyntheticCommand<CE : CommandEvent<*, *>>(
    name: String,
    alias: Array<String>,
    description: String?,
    access: CommandAccess<CE>,
    group: CommandGroup<CE>,
    private val exec: suspend Command<CE>.(CE) -> Unit
) : Command<CE>(name, alias, description, access, group) {

    override suspend fun execute(event: CE) {
        exec(this, event)
    }

    override suspend fun CE.message(message: String?, isError: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun CE.error(exception: Exception) {
        TODO("Not yet implemented")
    }

    override suspend fun CE.usage() {
        TODO("Not yet implemented")
    }
}

class SyntheticProvider(defaultPrefix: String) :
    AbstractCommandProvider<SyntheticMessage, SyntheticCommandEvent>(SyntheticPrefix(defaultPrefix)) {

    override suspend fun convert(event: SyntheticMessage): SyntheticCommandEvent? {
        val prefix = this.prefix[event]
        return if (event.message.startsWith(prefix)) {
            SyntheticCommandEvent(event, prefix)
        } else {
            null
        }
    }
}

class SyntheticPrefix(override val default: String) : Prefix<SyntheticMessage> {
    override fun get(event: SyntheticMessage): String = default
}

class SyntheticModuleProvider(engine: SyntheticEngine) : AbstractModuleProvider<SyntheticEngine>(engine) {
    override fun initialize() {

    }
}

@Entity
@Table(name = "test")
data class SyntheticSubject(override val id: Long, val result: String) : IdObject<Long>
