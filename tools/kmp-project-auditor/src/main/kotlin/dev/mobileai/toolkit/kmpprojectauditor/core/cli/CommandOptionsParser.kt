package dev.mobileai.toolkit.kmpprojectauditor.core.cli

import java.nio.file.Path

data class CommandOptions(
    val projectPath: Path
)

class CommandOptionsParser {
    fun parse(args: Array<String>): CommandOptions {
        if (args.size != 3) {
            throw IllegalArgumentException("Invalid arguments")
        }
        if (args[0] != "kmp" || args[1] != "audit") {
            throw IllegalArgumentException("Invalid command")
        }

        return CommandOptions(projectPath = Path.of(args[2]))
    }
}
