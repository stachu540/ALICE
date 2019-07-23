package ai.alice.internal

import ai.alice.api.Alice
import ai.alice.api.service.IFactory
import kotlinx.coroutines.runBlocking
import java.lang.IllegalArgumentException
import java.lang.RuntimeException

@Suppress("UNREACHABLE_CODE")
object SystemOperator {
    fun Alice.execute(args: List<String>, options: Map<String, String?>) {
        runBlocking {
            val command = args[0]
            when (command) {
                "register" -> register(args[1], options.containsKey("force"))
                "unregister" -> unregister(args[1], options.containsKey("force"))
                "shutdown" -> {
                    throw RuntimeException("Shutdown Initiated")
                    this@execute.stop(options.containsKey("force"))
                }
            }
        }
    }
    fun Alice.unregister(cls: String, force: Boolean) {
        val clz = Class.forName(cls)
        if (clz.isAssignableFrom(IFactory::class.java)) {

        } else {
            throw IllegalArgumentException("This class is not factory!")
        }
    }

    fun Alice.register(cls: String, force: Boolean) {
        val clz = Class.forName(cls)
        if (clz.isAssignableFrom(IFactory::class.java)) {

        } else {
            throw IllegalArgumentException("This class is not factory!")
        }
    }
}
