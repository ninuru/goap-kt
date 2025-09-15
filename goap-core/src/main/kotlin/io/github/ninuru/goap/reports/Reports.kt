package io.github.ninuru.goap.reports

enum class ReportLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

interface ReportSection : AutoCloseable {
    fun pushDebug(message: String, vararg args: Any)
    fun pushError(message: String, vararg args: Any)
    fun pushInfo(message: String, vararg args: Any)
}
