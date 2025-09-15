package io.github.ninuru.goap.logs

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

interface Logger {
    fun log(level: LogLevel, message: String)

    fun debug(message: String) = log(LogLevel.DEBUG, message)
    fun info(message: String) = log(LogLevel.INFO, message)
    fun warn(message: String) = log(LogLevel.WARN, message)
    fun error(message: String) = log(LogLevel.ERROR, message)
}

class StdoutLogger(private val minLevel: LogLevel = LogLevel.DEBUG) : Logger {
    override fun log(level: LogLevel, message: String) {
        if (level.ordinal >= minLevel.ordinal) {
            println("[GOAP] [$level] $message")
        }
    }
}
