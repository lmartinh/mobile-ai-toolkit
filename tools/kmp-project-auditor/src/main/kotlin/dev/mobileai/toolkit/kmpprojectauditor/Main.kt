package dev.mobileai.toolkit.kmpprojectauditor

import dev.mobileai.toolkit.aiclient.AiClientFactory
import dev.mobileai.toolkit.aiclient.AiConfigLoader
import dev.mobileai.toolkit.kmpprojectauditor.core.analysis.KmpAiAnalyzer
import dev.mobileai.toolkit.kmpprojectauditor.core.analysis.KmpAiAnalysisResult
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.DeterministicKmpAuditor
import dev.mobileai.toolkit.kmpprojectauditor.core.cli.CommandOptionsParser
import dev.mobileai.toolkit.kmpprojectauditor.core.cli.ReportOutputWriter
import dev.mobileai.toolkit.kmpprojectauditor.core.report.KmpMarkdownReportRenderer
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

    val deterministicFindings = DeterministicKmpAuditor().audit(scanResult)
    val aiAnalysis = try {
        val aiConfig = AiConfigLoader().load()
        val aiClient = AiClientFactory.create(aiConfig)
        KmpAiAnalyzer(aiClient).analyze(scanResult, deterministicFindings)
    } catch (_: Exception) {
        KmpAiAnalysisResult(
            findings = emptyList(),
            warnings = listOf("AI analysis failed."),
            model = "unavailable",
            provider = "unavailable"
        )
    }
    val summaryRenderer = ScanSummaryRenderer()
    val markdownReport = KmpMarkdownReportRenderer().render(
        scanResult = scanResult,
        deterministicFindings = deterministicFindings,
        aiResult = aiAnalysis
    )
    if (options.outputPath != null) {
        try {
            ReportOutputWriter().write(markdownReport, options.outputPath)
        } catch (error: IllegalArgumentException) {
            System.err.println("Error: ${error.message}")
            exitProcess(1)
        }
        println("KMP Project Auditor")
        println("Analyzed path: ${scanResult.analyzedPath}")
        println("Deterministic findings: ${deterministicFindings.size}")
        println("AI findings: ${aiAnalysis.findings.size}")
        println("Markdown report written to: ${options.outputPath.toAbsolutePath().normalize()}")
    } else {
        println(
            summaryRenderer.render(
                result = scanResult,
                deterministicFindings = deterministicFindings,
                aiFindings = aiAnalysis.findings,
                aiWarnings = aiAnalysis.warnings
            )
        )
    }
}

private fun printUsage() {
    println("Usage: kmp audit <path> [--output <path>]")
}
