package ai.alice.internal.data

import me.liuwj.ktorm.logging.Logger

class DbLogger(
    private val log: org.slf4j.Logger
) : Logger {
    override fun debug(msg: String, e: Throwable?) {
        if (e != null) log.debug(msg, e) else log.debug(msg)
    }

    override fun error(msg: String, e: Throwable?) {
        if (e != null) log.error(msg, e) else log.error(msg)
    }

    override fun info(msg: String, e: Throwable?) {
        if (e != null) log.info(msg, e) else log.info(msg)
    }

    override fun isDebugEnabled(): Boolean = log.isDebugEnabled

    override fun isErrorEnabled(): Boolean = log.isErrorEnabled

    override fun isInfoEnabled(): Boolean = log.isInfoEnabled

    override fun isTraceEnabled(): Boolean = log.isTraceEnabled

    override fun isWarnEnabled(): Boolean = log.isWarnEnabled

    override fun trace(msg: String, e: Throwable?) {
        if (e != null) log.trace(msg, e) else log.trace(msg)
    }

    override fun warn(msg: String, e: Throwable?) {
        if (e != null) log.warn(msg, e) else log.warn(msg)
    }
}