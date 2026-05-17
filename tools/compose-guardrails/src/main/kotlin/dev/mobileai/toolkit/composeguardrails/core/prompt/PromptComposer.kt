package dev.mobileai.toolkit.composeguardrails.core.prompt

import dev.mobileai.toolkit.composeguardrails.core.ComposeFileAnalysis
import java.nio.file.Files
import kotlin.io.path.pathString

class PromptComposer {
    fun compose(
        assets: PromptAssets,
        analyses: List<ComposeFileAnalysis>
    ): PromptBundle {
        val composeCandidates = analyses.filter { it.isComposeCandidate }
        val sortedRules = assets.rules.sortedBy { it.id }

        val promptText = buildString {
            appendLine("# Compose Guardrails Review Request")
            appendLine()
            appendLine("## Base Review Instructions")
            appendLine(assets.composeReview)
            appendLine()
            appendLine("## Output Requirements")
            appendLine(assets.outputFormat)
            appendLine()
            appendLine("## Active Rules")
            sortedRules.forEach { rule ->
                appendLine("### Rule: ${rule.id}")
                appendLine(rule.content)
                appendLine()
            }
            appendLine("## Analysis Context")
            appendLine("Compose candidate files: ${composeCandidates.size}")
            appendLine("Total analyzed files: ${analyses.size}")
            appendLine()

            if (composeCandidates.isEmpty()) {
                appendLine("No Compose candidate files were detected.")
            } else {
                composeCandidates.forEach { analysis ->
                    appendLine("### File: ${analysis.filePath.pathString}")
                    if (analysis.composableFunctions.isEmpty()) {
                        appendLine("Detected composable functions: none")
                    } else {
                        appendLine("Detected composable functions:")
                        analysis.composableFunctions.forEach { function ->
                            appendLine("- ${function.functionName} (line ${function.line})")
                        }
                    }
                    appendLine("Code:")
                    appendLine("```kotlin")
                    appendLine(Files.readString(analysis.filePath).trimEnd())
                    appendLine("```")
                    appendLine()
                }
            }
        }.trimEnd()

        return PromptBundle(
            promptText = promptText,
            activeRuleIds = sortedRules.map { it.id },
            composeCandidateCount = composeCandidates.size
        )
    }
}

data class PromptBundle(
    val promptText: String,
    val activeRuleIds: List<String>,
    val composeCandidateCount: Int
)
