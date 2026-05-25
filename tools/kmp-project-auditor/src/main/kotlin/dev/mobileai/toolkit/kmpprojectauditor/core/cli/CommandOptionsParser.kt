package dev.mobileai.toolkit.kmpprojectauditor.core.cli

import java.nio.file.Path

data class CommandOptions(
    val projectPath: Path,
    val outputPath: Path?
)

class CommandOptionsParser {
    fun parse(args: Array<String>): CommandOptions {
        if (args.size < 3) {
            throw IllegalArgumentException("Invalid arguments")
        }
        if (args[0] != "kmp" || args[1] != "audit") {
            throw IllegalArgumentException("Invalid command")
        }

        val projectPath = Path.of(args[2])
        var outputPath: Path? = null
        var index = 3
        while (index < args.size) {
            when (args[index]) {
                "--output" -> {
                    val value = args.getOrNull(index + 1)
                        ?: throw IllegalArgumentException("Missing value for --output")
                    if (value.startsWith("--")) {
                        throw IllegalArgumentException("Missing value for --output")
                    }
                    outputPath = Path.of(value)
                    index += 2
                }

                else -> throw IllegalArgumentException("Unsupported option: ${args[index]}")
            }
        }

        return CommandOptions(projectPath = projectPath, outputPath = outputPath)
    }
}
