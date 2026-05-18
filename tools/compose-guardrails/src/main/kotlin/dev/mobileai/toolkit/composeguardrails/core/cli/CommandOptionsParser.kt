package dev.mobileai.toolkit.composeguardrails.core.cli

import dev.mobileai.toolkit.composeguardrails.core.prompt.RuleSet
import java.nio.file.Path

data class CommandOptions(
    val inputPath: Path,
    val ruleSet: RuleSet,
    val outputPath: Path?
)

class CommandOptionsParser {
    fun parse(args: Array<String>): CommandOptions {
        if (args.size < 3) {
            throw IllegalArgumentException("Invalid arguments")
        }
        if (args[0] != "guardrails" || args[1] != "check") {
            throw IllegalArgumentException("Invalid command")
        }

        val inputPath = Path.of(args[2])
        var ruleSet = RuleSet.DEFAULT
        var outputPath: Path? = null

        var index = 3
        while (index < args.size) {
            when (args[index]) {
                "--rule-set" -> {
                    val value = args.getOrNull(index + 1)
                        ?: throw IllegalArgumentException("Missing value for --rule-set")
                    ruleSet = RuleSet.fromCli(value)
                    index += 2
                }

                "--output" -> {
                    val value = args.getOrNull(index + 1)
                        ?: throw IllegalArgumentException("Missing value for --output")
                    if (value.startsWith("--")) {
                        throw IllegalArgumentException("Missing value for --output")
                    }
                    outputPath = Path.of(value)
                    index += 2
                }

                else -> {
                    throw IllegalArgumentException(
                        "Unsupported option: ${args[index]}. " +
                            "Supported options: --rule-set <default|advanced|all>, --output <path>"
                    )
                }
            }
        }

        return CommandOptions(inputPath, ruleSet, outputPath)
    }
}
