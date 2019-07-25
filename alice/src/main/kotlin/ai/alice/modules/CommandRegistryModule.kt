package ai.alice.modules

import ai.alice.commands.CommandRegistry
import com.typesafe.config.Config
import io.jooby.*

class CommandRegistryModule : Extension {
    override fun install(application: Jooby) {
        application.services.put(
            CommandRegistry::class,
            CommandRegistry(application as Kooby)
        )
    }
}