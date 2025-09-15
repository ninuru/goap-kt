package io.github.ninuru.goap.reports

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status

class Report(val level: ReportLevel, val message: String)

class Reporter {
    private val reports = mutableListOf<Report>()

    fun beginSection(name: String): ReportSection {
        return ReportSectionImpl(name, this)
    }

    fun pushDebug(message: String, vararg args: Any) {
        push(ReportLevel.DEBUG, message, args)
    }

    fun pushError(message: String, vararg args: Any) {
        push(ReportLevel.ERROR, message, args)
    }

    fun push(level: ReportLevel, message: String, args: Array<out Any>) {
        val formatted = if (args.isNotEmpty()) message.format(*args) else message
        reports.add(Report(level, formatted))
    }

    fun getReports() = reports.toList()
    fun clear() = reports.clear()
}

class ReportSectionImpl(
    private val name: String,
    private val reporter: Reporter
) : ReportSection {
    init {
        reporter.pushDebug(">>> $name")
    }

    override fun pushDebug(message: String, vararg args: Any) {
        reporter.pushDebug(message, *args)
    }

    override fun pushError(message: String, vararg args: Any) {
        reporter.pushError(message, *args)
    }

    override fun pushInfo(message: String, vararg args: Any) {
        reporter.push(ReportLevel.INFO, message, args)
    }

    override fun close() {
        reporter.pushDebug("<<< $name")
    }
}
