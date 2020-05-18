package ai.alice

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object GlobalLogger : Logger by LoggerFactory.getLogger("ai.alice.root") {
    @JvmStatic
    inline fun <reified T> of() = LoggerFactory.getLogger(T::class.java)
}