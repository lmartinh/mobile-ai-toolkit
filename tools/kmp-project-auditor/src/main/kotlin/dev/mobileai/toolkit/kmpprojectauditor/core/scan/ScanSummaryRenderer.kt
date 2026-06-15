package dev.mobileai.toolkit.kmpprojectauditor.core.scan

import dev.mobileai.toolkit.kmpprojectauditor.core.audit.KmpFinding

class ScanSummaryRenderer {
    fun render(
        result: ProjectScanResult,
        deterministicFindings: List<KmpFinding>,
        aiFindings: List<KmpFinding>,
        aiWarnings: List<String>
    ): String {
        val lines = mutableListOf<String>()
        lines += renderSummaryOnly(result)
        lines += "Deterministic findings:"
        lines += formatFindings(deterministicFindings, emptyFallback = "No deterministic findings found.")
        lines += "AI findings:"
        lines += formatFindings(aiFindings, emptyFallback = "No AI findings found.")
        if (aiWarnings.isNotEmpty()) {
            lines += "AI warnings:"
            lines += formatList(aiWarnings)
        }
        lines += "AI findings are heuristic and should be reviewed by a developer."
        lines += "Use --output <path> to write a Markdown report."
        return lines.joinToString(separator = "\n")
    }

    fun renderSummaryOnly(result: ProjectScanResult): String {
        val lines = mutableListOf<String>()
        lines += "KMP Project Auditor"
        lines += "Analyzed path: ${result.analyzedPath}"
        lines += "Gradle files:"
        lines += formatList(result.gradleFiles)
        lines += "Detected Gradle configuration:"
        lines += "- KMP project shape: ${result.kmpContextStatusLabel()}"
        lines += "- Kotlin Multiplatform plugin: ${result.kmpPluginStatusLabel()}"
        lines += "- Android target: ${result.androidTargetStatusLabel()}"
        lines += "- iOS target: ${result.iosTargetStatusLabel()}"
        lines += "- KMP plugin detected in: ${formatInlineList(result.gradleHeuristics.kmpPluginFiles)}"
        lines += "- Android target detected in: ${formatInlineList(result.gradleHeuristics.androidTargetFiles)}"
        lines += "- iOS target detected in: ${formatInlineList(result.gradleHeuristics.iosTargetFiles)}"
        lines += "Source sets:"
        lines += formatSourceSets(result.sourceSetSummaries)
        lines += "Kotlin source roots:"
        lines += formatList(result.kotlinSourceRoots)
        lines += "Detected capabilities:"
        lines += "- has commonMain: ${result.hasCommonMain}"
        lines += "- has commonTest: ${result.hasCommonTest}"
        lines += "- has Android source set: ${result.hasAndroidSourceSet}"
        lines += "- has iOS source set: ${result.hasIosSourceSet}"
        lines += "- Android target signal: ${result.androidTargetStatusLabel()}"
        lines += "- iOS target signal: ${result.iosTargetStatusLabel()}"
        lines += "- has intermediate source sets: ${result.hasIntermediateSourceSets}"
        lines += "- has custom source sets: ${result.hasCustomSourceSets}"
        lines += "Layout notes:"
        lines += formatList(result.layoutNotes)
        return lines.joinToString(separator = "\n")
    }

    private fun formatSourceSets(items: List<SourceSetSummary>): String {
        return if (items.isEmpty()) {
            "- (none)"
        } else {
            items.joinToString("\n") { "- ${it.name} (${it.kind.name.lowercase()})" }
        }
    }

    private fun formatList(items: List<String>): String {
        return if (items.isEmpty()) "- (none)" else items.joinToString("\n") { "- $it" }
    }

    private fun formatInlineList(items: List<String>): String {
        return if (items.isEmpty()) "(none)" else items.joinToString(", ")
    }

    private fun formatFindings(findings: List<KmpFinding>, emptyFallback: String): String {
        if (findings.isEmpty()) {
            return emptyFallback
        }

        val lines = mutableListOf<String>()
        findings.forEach { finding ->
            lines += "- [${finding.severity}] ${finding.ruleId} - ${finding.title}"
            lines += "  File: ${finding.file}"
            if (finding.lineNumber != null) {
                lines += "  Line: ${finding.lineNumber}"
            }
            if (finding.evidence != null) {
                lines += "  Evidence: ${finding.evidence}"
            }
            lines += "  Explanation: ${finding.explanation}"
            lines += "  Suggestion: ${finding.suggestion}"
        }
        return lines.joinToString("\n")
    }
}
