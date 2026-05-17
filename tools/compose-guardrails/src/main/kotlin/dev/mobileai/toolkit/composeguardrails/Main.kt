package dev.mobileai.toolkit.composeguardrails

import dev.mobileai.toolkit.composeguardrails.core.ComposeCandidateDetector
import dev.mobileai.toolkit.composeguardrails.core.KotlinFileScanner
import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 3 || args[0] != "guardrails" || args[1] != "check") {
        printUsage()
        exitProcess(1)
    }

    val inputPath = Path.of(args[2])
    val scanner = KotlinFileScanner()
    val detector = ComposeCandidateDetector()

    val kotlinFiles = try {
        scanner.scan(inputPath)
    } catch (error: IllegalArgumentException) {
        System.err.println("Error: ${error.message}")
        exitProcess(1)
    }
    val composeAnalyses = detector.detect(kotlinFiles)
    val composeCandidates = composeAnalyses.filter { it.isComposeCandidate }
    val totalComposableFunctions = composeCandidates.sumOf { it.composableFunctions.size }

    println("Compose Guardrails - File Discovery")
    println("Analyzed path: ${inputPath.toAbsolutePath().normalize().pathString}")
    println("Kotlin files found: ${kotlinFiles.size}")
    println("Compose candidate files: ${composeCandidates.size}")
    println("Composable functions detected: $totalComposableFunctions")
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
}

private fun printUsage() {
    println("Usage: mobile-ai guardrails check <path>")
}
