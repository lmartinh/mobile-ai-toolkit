package dev.mobileai.toolkit.composeguardrails

import dev.mobileai.toolkit.aiclient.AiConfigLoader
import dev.mobileai.toolkit.aiclient.AiClientFactory
import dev.mobileai.toolkit.composeguardrails.core.cli.CommandOptionsParser
import dev.mobileai.toolkit.composeguardrails.core.ComposeCandidateDetector
import dev.mobileai.toolkit.composeguardrails.core.KotlinFileScanner
import dev.mobileai.toolkit.composeguardrails.core.analysis.GuardrailsAiAnalyzer
import dev.mobileai.toolkit.composeguardrails.core.parsing.FindingParser
import dev.mobileai.toolkit.composeguardrails.core.prompt.PromptAssetLoader
import dev.mobileai.toolkit.composeguardrails.core.prompt.PromptComposer
import dev.mobileai.toolkit.report.FindingSeverity
import dev.mobileai.toolkit.report.MarkdownReportRenderer
import dev.mobileai.toolkit.report.ReportInput
import kotlin.io.path.pathString
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val commandOptions = try {
        CommandOptionsParser().parse(args)
    } catch (error: IllegalArgumentException) {
        System.err.println("Error: ${error.message}")
        printUsage()
        exitProcess(1)
    }

    val inputPath = commandOptions.inputPath
    val scanner = KotlinFileScanner()
    val detector = ComposeCandidateDetector()
    val promptLoader = PromptAssetLoader()
    val promptComposer = PromptComposer()
    val findingParser = FindingParser()
    val reportRenderer = MarkdownReportRenderer()
    val aiConfig = try {
        AiConfigLoader().load()
    } catch (error: IllegalArgumentException) {
        System.err.println("Error: ${error.message}")
        exitProcess(1)
    }
    val aiAnalyzer = try {
        GuardrailsAiAnalyzer(AiClientFactory.create(aiConfig))
    } catch (error: IllegalArgumentException) {
        System.err.println("Error: ${error.message}")
        exitProcess(1)
    }

    val kotlinFiles = try {
        scanner.scan(inputPath)
    } catch (error: IllegalArgumentException) {
        System.err.println("Error: ${error.message}")
        exitProcess(1)
    }
    val composeAnalyses = detector.detect(kotlinFiles)
    val composeCandidates = composeAnalyses.filter { it.isComposeCandidate }
    val totalComposableFunctions = composeCandidates.sumOf { it.composableFunctions.size }
    val promptAssets = try {
        promptLoader.load(commandOptions.ruleSet)
    } catch (error: IllegalArgumentException) {
        System.err.println("Error: ${error.message}")
        exitProcess(1)
    }
    val promptBundle = promptComposer.compose(promptAssets, composeAnalyses)
    val aiResult = try {
        aiAnalyzer.analyze(promptBundle)
    } catch (error: IllegalStateException) {
        System.err.println("Error: ${error.message}")
        exitProcess(1)
    }
    val parsedFindings = findingParser.parse(aiResult.content)
    val findingsBySeverity = parsedFindings.findings.groupingBy { it.severity }.eachCount()

    println("Compose Guardrails - File Discovery")
    println("Analyzed path: ${inputPath.toAbsolutePath().normalize().pathString}")
    println("Kotlin files found: ${kotlinFiles.size}")
    println("Compose candidate files: ${composeCandidates.size}")
    println("Composable functions detected: $totalComposableFunctions")
    println("Rule set: ${commandOptions.ruleSet.cliValue}")
    println("Active guardrail rules: ${promptBundle.activeRuleIds.size}")
    println("Composed prompt size: ${promptBundle.promptText.length} chars")
    println("AI client: ${aiResult.metadata["provider"]} (${aiResult.model})")
    println("Findings parsed: ${parsedFindings.findings.size}")
    println(
        "Findings by severity: error=${findingsBySeverity[FindingSeverity.ERROR] ?: 0}, " +
            "warning=${findingsBySeverity[FindingSeverity.WARNING] ?: 0}, " +
            "info=${findingsBySeverity[FindingSeverity.INFO] ?: 0}"
    )
    println("Files:")

    kotlinFiles.forEach { filePath ->
        println("- ${filePath.toAbsolutePath().normalize().pathString}")
    }

    if (composeCandidates.isNotEmpty()) {
        println("Compose details:")
        composeCandidates.forEach { analysis ->
            println("- ${analysis.filePath.toAbsolutePath().normalize().pathString}")
            analysis.composableFunctions.forEach { function ->
                println("  - @Composable ${function.functionName} (line ${function.line})")
            }
        }
    }

    if (parsedFindings.warnings.isNotEmpty()) {
        println("Parser warnings:")
        parsedFindings.warnings.forEach { warning ->
            println("- $warning")
        }
    }

    if (parsedFindings.findings.isNotEmpty()) {
        println("Structured findings:")
        parsedFindings.findings.forEach { finding ->
            println("- [${finding.severity}] ${finding.ruleId}: ${finding.title} (${finding.filePath})")
        }
    }

    val markdownReport = reportRenderer.render(
        ReportInput(
            analyzedPath = inputPath.toAbsolutePath().normalize().pathString,
            kotlinFilesScanned = kotlinFiles.size,
            findings = parsedFindings.findings,
            parserWarnings = parsedFindings.warnings
        )
    )

    println()
    println(markdownReport)
}

private fun printUsage() {
    println("Usage: mobile-ai guardrails check <path> [--rule-set default|advanced|all]")
}
