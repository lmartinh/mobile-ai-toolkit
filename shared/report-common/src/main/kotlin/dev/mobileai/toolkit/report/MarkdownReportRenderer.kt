package dev.mobileai.toolkit.report

class MarkdownReportRenderer {
    fun render(input: ReportInput): String {
        val findings = input.findings.sortedWith(
            compareBy<GuardrailFinding> { it.filePath }
                .thenBy { severityRank(it.severity) }
                .thenBy { it.ruleId }
                .thenBy { it.title }
        )

        val severityCounts = findings.groupingBy { it.severity }.eachCount()
        val affectedFiles = findings.map { it.filePath }.toSet().size

        return buildString {
            appendLine("# Compose Guardrails Report")
            appendLine()
            appendLine("## Summary")
            appendLine("- Analyzed path: `${input.analyzedPath}`")
            appendLine("- Kotlin files scanned: ${input.kotlinFilesScanned}")
            appendLine("- Total findings: ${findings.size}")
            appendLine("- Affected files: $affectedFiles")
            appendLine(
                "- Findings by severity: " +
                    "error=${severityCounts[FindingSeverity.ERROR] ?: 0}, " +
                    "warning=${severityCounts[FindingSeverity.WARNING] ?: 0}, " +
                    "info=${severityCounts[FindingSeverity.INFO] ?: 0}"
            )

            if (input.parserWarnings.isNotEmpty()) {
                appendLine()
                appendLine("## Parser Warnings")
                input.parserWarnings.forEach { warning ->
                    appendLine("- $warning")
                }
            }

            appendLine()
            appendLine("## Findings")

            if (findings.isEmpty()) {
                appendLine("No guardrail violations detected.")
                return@buildString
            }

            findings.groupBy { it.filePath }.forEach { (filePath, fileFindings) ->
                appendLine()
                appendLine("### File: $filePath")
                fileFindings.forEach { finding ->
                    appendLine("#### ${finding.severity.name.lowercase().replaceFirstChar(Char::uppercase)}: ${finding.title}")
                    appendLine("- Rule: `${finding.ruleId}`")
                    appendLine("- Explanation: ${finding.explanation}")
                    appendLine("- Suggestion: ${finding.suggestion}")

                    if (!finding.codeExample.isNullOrBlank()) {
                        appendLine("- Code example:")
                        appendLine("```kotlin")
                        appendLine(finding.codeExample)
                        appendLine("```")
                    }
                    appendLine()
                }
            }
        }.trimEnd()
    }

    private fun severityRank(severity: FindingSeverity): Int {
        return when (severity) {
            FindingSeverity.ERROR -> 0
            FindingSeverity.WARNING -> 1
            FindingSeverity.INFO -> 2
        }
    }
}

data class ReportInput(
    val analyzedPath: String,
    val kotlinFilesScanned: Int,
    val findings: List<GuardrailFinding>,
    val parserWarnings: List<String> = emptyList()
)
