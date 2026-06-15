package dev.mobileai.toolkit.kmpprojectauditor.core.report

import dev.mobileai.toolkit.kmpprojectauditor.core.analysis.KmpAiAnalysisResult
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.KmpFinding
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanResult

class KmpMarkdownReportRenderer {
    fun render(
        scanResult: ProjectScanResult,
        deterministicFindings: List<KmpFinding>,
        aiResult: KmpAiAnalysisResult
    ): String {
        val additionalAiFindings = additionalAiFindings(deterministicFindings, aiResult.findings)
        val totalFindings = deterministicFindings.size + additionalAiFindings.size
        val lines = mutableListOf<String>()
        lines += "# KMP Project Audit Report"
        lines += "## Summary"
        lines += "- Analyzed path: `${escapeInline(displayPath(scanResult.analyzedPath))}`"
        lines += "- Gradle files: ${scanResult.gradleFiles.size}"
        lines += "- Source sets: ${scanResult.sourceSets.size}"
        lines += "- Kotlin source roots: ${scanResult.kotlinSourceRoots.size}"
        lines += "- Total findings: $totalFindings"
        lines += "- Deterministic findings: ${deterministicFindings.size}"
        lines += "- Additional AI findings: ${additionalAiFindings.size}"
        lines += "- AI warnings: ${aiResult.warnings.size}"
        lines += "- Provider: ${escapeInline(aiResult.provider)}"
        lines += ""
        lines += "## Detected Gradle Configuration"
        lines += "- KMP project shape: ${scanResult.kmpContextStatusLabel()}"
        lines += "- Kotlin Multiplatform plugin: ${scanResult.kmpPluginStatusLabel()}"
        lines += "- Android target: ${scanResult.androidTargetStatusLabel()}"
        lines += "- iOS target: ${scanResult.iosTargetStatusLabel()}"
        lines += ""
        lines += "## Source Sets"
        lines += "| Source set | Category |"
        lines += "| --- | --- |"
        scanResult.sourceSetSummaries.forEach {
            lines += "| ${escapeTable(it.name)} | ${escapeTable(it.kind.name.lowercase())} |"
        }
        lines += ""
        lines += "## Kotlin Source Roots"
        if (scanResult.kotlinSourceRoots.isEmpty()) {
            lines += "No Kotlin source roots found."
        } else {
            scanResult.kotlinSourceRoots.forEach { lines += "- `${escapeInline(it)}`" }
        }
        lines += ""
        lines += "## Deterministic Findings"
        lines += formatFindingsSection(deterministicFindings, "No deterministic findings found.")
        lines += ""
        lines += "## Additional AI Findings"
        lines += formatFindingsSection(additionalAiFindings, "No additional AI findings found.")
        lines += ""
        lines += "## AI Warnings"
        if (aiResult.warnings.isEmpty()) {
            lines += "No AI warnings."
        } else {
            aiResult.warnings.forEach { lines += "- ${escapeText(it)}" }
        }
        lines += ""
        lines += "## Limitations"
        lines += "- Detection is heuristic and text-based."
        lines += "- AI findings should be manually reviewed."
        lines += "- No Gradle AST or dependency graph resolution is performed."
        return lines.joinToString("\n")
    }

    private fun formatFindingsSection(findings: List<KmpFinding>, emptyText: String): String {
        if (findings.isEmpty()) {
            return emptyText
        }

        return findings.joinToString("\n\n") { finding ->
            buildString {
                appendLine("### ${finding.severity} - ${escapeText(finding.ruleId)}")
                appendLine("**Title:** ${escapeText(finding.title)}  ")
                appendLine("**File:** `${escapeInline(finding.file)}`  ")
                if (finding.lineNumber != null) {
                    appendLine("**Line:** ${finding.lineNumber}  ")
                }
                if (!finding.evidence.isNullOrBlank()) {
                    appendLine("**Evidence:** `${escapeInline(finding.evidence)}`")
                }
                appendLine(escapeText(finding.explanation))
                appendLine()
                append("**Suggestion:** ${escapeText(finding.suggestion)}")
            }.trimEnd()
        }
    }

    private fun additionalAiFindings(
        deterministicFindings: List<KmpFinding>,
        aiFindings: List<KmpFinding>
    ): List<KmpFinding> {
        val deterministicKeys = deterministicFindings.mapTo(linkedSetOf(), ::findingKey)
        val seenAiKeys = linkedSetOf<String>()
        return aiFindings.filter { finding ->
            val key = findingKey(finding)
            key !in deterministicKeys && seenAiKeys.add(key)
        }
    }

    private fun findingKey(finding: KmpFinding): String = "${finding.ruleId}|${finding.file}"

    private fun displayPath(path: java.nio.file.Path): String {
        val normalized = path.toAbsolutePath().normalize()
        val workingDirectory = java.nio.file.Path.of("").toAbsolutePath().normalize()
        return if (normalized.startsWith(workingDirectory)) {
            workingDirectory.relativize(normalized).toString().replace("\\", "/")
        } else {
            path.normalize().toString().replace("\\", "/")
        }
    }

    private fun escapeInline(value: String): String = value.replace("`", "\\`")
    private fun escapeText(value: String): String = value.replace("|", "\\|")
    private fun escapeTable(value: String): String = escapeText(value)
}
