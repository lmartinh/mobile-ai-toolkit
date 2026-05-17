package dev.mobileai.toolkit.composeguardrails.core.prompt

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.streams.toList

class PromptAssetLoader(private val promptsRoot: Path) {
    fun load(): PromptAssets {
        val reviewPrompt = readRequiredFile(promptsRoot.resolve("compose-review.md"))
        val outputFormatPrompt = readRequiredFile(promptsRoot.resolve("output-format.md"))
        val rulePrompts = loadRulePrompts(promptsRoot.resolve("rules"))

        return PromptAssets(
            composeReview = reviewPrompt,
            outputFormat = outputFormatPrompt,
            rules = rulePrompts
        )
    }

    private fun loadRulePrompts(rulesDirectory: Path): List<RulePrompt> {
        require(rulesDirectory.exists()) { "Rules directory does not exist: $rulesDirectory" }

        Files.list(rulesDirectory).use { stream ->
            val files = stream
                .filter { it.isRegularFile() }
                .filter { it.name.endsWith(".md") }
                .sorted()
                .toList()

            require(files.isNotEmpty()) { "No rule prompt files found in: $rulesDirectory" }

            return files.map { file ->
                RulePrompt(
                    id = file.name.removeSuffix(".md"),
                    content = readRequiredFile(file)
                )
            }
        }
    }

    private fun readRequiredFile(path: Path): String {
        require(path.exists()) { "Required prompt file does not exist: $path" }
        require(path.isRegularFile()) { "Prompt path is not a file: $path" }

        val content = path.readText().trim()
        require(content.isNotBlank()) { "Prompt file is empty: $path" }

        return content
    }
}

data class PromptAssets(
    val composeReview: String,
    val outputFormat: String,
    val rules: List<RulePrompt>
)

data class RulePrompt(
    val id: String,
    val content: String
)
