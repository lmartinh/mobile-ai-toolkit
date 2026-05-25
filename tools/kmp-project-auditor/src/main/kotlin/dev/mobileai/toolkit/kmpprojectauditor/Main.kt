package dev.mobileai.toolkit.kmpprojectauditor

import dev.mobileai.toolkit.kmpprojectauditor.core.cli.CommandOptionsParser
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.DeterministicKmpAuditor
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanner
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ScanSummaryRenderer
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val options = try {
        CommandOptionsParser().parse(args)
    } catch (error: IllegalArgumentException) {
        System.err.println("Error: ${error.message}")
        printUsage()
        exitProcess(1)
    }

    val scanResult = try {
        ProjectScanner().scan(options.projectPath)
    } catch (error: IllegalArgumentException) {
        System.err.println("Error: ${error.message}")
        exitProcess(1)
    }

    val findings = DeterministicKmpAuditor().audit(scanResult)
    println(ScanSummaryRenderer().render(scanResult, findings))
}

private fun printUsage() {
    println("Usage: kmp audit <path>")
}
