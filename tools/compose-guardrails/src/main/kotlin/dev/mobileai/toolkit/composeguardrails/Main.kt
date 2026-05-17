package dev.mobileai.toolkit.composeguardrails

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

    val kotlinFiles = try {
        scanner.scan(inputPath)
    } catch (error: IllegalArgumentException) {
        System.err.println("Error: ${error.message}")
        exitProcess(1)
    }

    println("Compose Guardrails - File Discovery")
    println("Analyzed path: ${inputPath.toAbsolutePath().normalize().pathString}")
    println("Kotlin files found: ${kotlinFiles.size}")
    println("Files:")

    kotlinFiles.forEach { filePath ->
        println("- ${filePath.toAbsolutePath().normalize().pathString}")
    }
}

private fun printUsage() {
    println("Usage: mobile-ai guardrails check <path>")
}
