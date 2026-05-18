package dev.mobileai.toolkit.composeguardrails.core.cli

import dev.mobileai.toolkit.composeguardrails.core.prompt.RuleSet
import java.nio.file.Path

data class CommandOptions(
    val inputPath: Path,
    val ruleSet: RuleSet
)

class CommandOptionsParser {
    fun parse(args: Array<String>): CommandOptions {
        if (args.size != 3 && args.size != 5) {
            throw IllegalArgumentException("Invalid arguments")
        }
        if (args[0] != "guardrails" || args[1] != "check") {
            throw IllegalArgumentException("Invalid command")
        }

        val inputPath = Path.of(args[2])
        val ruleSet = if (args.size == 5) {
            if (args[3] != "--rule-set") {
                throw IllegalArgumentException(
                    "Unsupported option: ${args[3]}. Supported option: --rule-set <default|advanced|all>"
                )
            }
            RuleSet.fromCli(args[4])
        } else {
            RuleSet.DEFAULT
        }

        return CommandOptions(inputPath, ruleSet)
    }
}
