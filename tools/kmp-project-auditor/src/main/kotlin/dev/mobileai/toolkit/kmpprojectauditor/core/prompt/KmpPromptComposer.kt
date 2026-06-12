package dev.mobileai.toolkit.kmpprojectauditor.core.prompt

import dev.mobileai.toolkit.kmpprojectauditor.core.audit.KmpFinding
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanResult
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ScanSummaryRenderer
import java.nio.file.Files

class KmpPromptComposer(
    private val summaryRenderer: ScanSummaryRenderer = ScanSummaryRenderer(),
    private val maxFiles: Int = 8,
    private val maxCharsPerFile: Int = 1200,
    private val maxTotalChars: Int = 12000
) {
    fun compose(
        assets: KmpPromptAssets,
        scanResult: ProjectScanResult,
        deterministicFindings: List<KmpFinding>
    ): KmpPromptBundle {
        val deterministicSection = if (deterministicFindings.isEmpty()) {
            "No deterministic findings found."
        } else {
            deterministicFindings.joinToString("\n") {
                "- [${it.severity}] ${it.ruleId} | ${it.file} | ${it.title}" +
                    (it.evidence?.let { evidence -> " | evidence: $evidence" } ?: "")
            }
        }

        val snippetEntries = collectSnippetEntries(scanResult, deterministicFindings)
        val promptText = buildString {
            appendLine("# KMP Project Audit Request")
            appendLine()
            appendLine("## Base Instructions")
            appendLine(assets.basePrompt)
            appendLine()
            appendLine("## Output Schema")
            appendLine(assets.outputFormat)
            appendLine()
            appendLine("## Active Rule Guidance")
            assets.rules.sortedBy { it.id }.forEach { rule ->
                appendLine("### Rule: ${rule.id}")
                appendLine(rule.content)
                appendLine()
            }
            appendLine("## Scan Summary")
            appendLine(summaryRenderer.renderSummaryOnly(scanResult))
            appendLine()
            appendLine("## AI Guardrails")
            appendLine("- Heuristic layout notes are not findings and may reflect convention-plugin indirection.")
            appendLine("- Compose Multiplatform imports under `androidx.compose.*` are valid in `commonMain` and must not be treated as Android API leakage.")
            appendLine("- Prefer zero findings over low-confidence architecture advice.")
            appendLine()
            appendLine("## Deterministic Findings")
            appendLine(deterministicSection)
            appendLine()
            appendLine("## Relevant File Snippets")
            if (snippetEntries.isEmpty()) {
                appendLine("No snippets were selected.")
            } else {
                snippetEntries.forEach { snippet ->
                    appendLine("### File: ${snippet.path}")
                    appendLine("```text")
                    appendLine(snippet.content)
                    appendLine("```")
                    appendLine()
                }
            }
        }.trimEnd()

        return KmpPromptBundle(
            promptText = truncatePrompt(promptText),
            snippetPaths = snippetEntries.map { it.path }
        )
    }

    private fun collectSnippetEntries(
        scanResult: ProjectScanResult,
        deterministicFindings: List<KmpFinding>
    ): List<SnippetEntry> {
        val findingLinkedKotlinPaths = deterministicFindings
            .map { it.file }
            .filter { it.endsWith(".kt") && it.contains("/commonMain/") }
            .distinct()
            .sorted()

        val commonMainKotlinPaths = scanResult.kotlinSourceRoots
            .filter { it.endsWith("/commonMain/kotlin") }
            .flatMap { root ->
                val rootPath = scanResult.analyzedPath.resolve(root)
                if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
                    emptyList()
                } else {
                    Files.walk(rootPath).use { stream ->
                        stream
                            .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".kt") }
                            .map { scanResult.analyzedPath.relativize(it).toString().replace('\\', '/') }
                            .toList()
                    }
                }
            }
            .sorted()

        val gradleSnippetPaths = scanResult.gradleFiles.sorted()

        val candidatePaths = (findingLinkedKotlinPaths + commonMainKotlinPaths + gradleSnippetPaths)
            .distinct()
            .take(maxFiles)

        return candidatePaths.mapNotNull { relativePath ->
            val filePath = scanResult.analyzedPath.resolve(relativePath)
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                return@mapNotNull null
            }
            val raw = filePath.toFile().readText()
            val truncated = if (raw.length <= maxCharsPerFile) raw else raw.take(maxCharsPerFile) + "\n...<truncated>"
            SnippetEntry(relativePath, truncated.trimEnd())
        }
    }

    private fun truncatePrompt(prompt: String): String {
        return if (prompt.length <= maxTotalChars) prompt else prompt.take(maxTotalChars) + "\n...<prompt-truncated>"
    }

    private data class SnippetEntry(
        val path: String,
        val content: String
    )
}

data class KmpPromptBundle(
    val promptText: String,
    val snippetPaths: List<String>
)
