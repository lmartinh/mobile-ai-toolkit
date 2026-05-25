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
        val lines = mutableListOf<String>()
        lines += "# KMP Project Audit Report"
        lines += "## Summary"
        lines += "- Analyzed path: `${escapeInline(scanResult.analyzedPath.toString())}`"
        lines += "- Gradle files: ${scanResult.gradleFiles.size}"
        lines += "- Source sets: ${scanResult.sourceSets.size}"
        lines += "- Kotlin source roots: ${scanResult.kotlinSourceRoots.size}"
        lines += "- Deterministic findings: ${deterministicFindings.size}"
        lines += "- AI findings: ${aiResult.findings.size}"
        lines += "- Provider: ${escapeInline(aiResult.provider)}"
        lines += ""
        lines += "## Detected Gradle Configuration"
        lines += "- Kotlin Multiplatform plugin: ${scanResult.gradleHeuristics.hasKmpPlugin}"
        lines += "- Android target: ${scanResult.gradleHeuristics.hasAndroidTarget}"
        lines += "- iOS target: ${scanResult.gradleHeuristics.hasIosTarget}"
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
        lines += "## AI Findings"
        lines += formatFindingsSection(aiResult.findings, "No AI findings found.")
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
                if (!finding.evidence.isNullOrBlank()) {
                    appendLine("**Evidence:** `${escapeInline(finding.evidence)}`")
                }
                appendLine(escapeText(finding.explanation))
                appendLine()
                append("**Suggestion:** ${escapeText(finding.suggestion)}")
            }.trimEnd()
        }
    }

    private fun escapeInline(value: String): String = value.replace("`", "\\`")
    private fun escapeText(value: String): String = value.replace("|", "\\|")
    private fun escapeTable(value: String): String = escapeText(value)
}
