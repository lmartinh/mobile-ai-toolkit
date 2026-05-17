package dev.mobileai.toolkit.composeguardrails.core.parsing

import dev.mobileai.toolkit.report.FindingSeverity
import dev.mobileai.toolkit.report.GuardrailFinding

class FindingParser {
    fun parse(rawContent: String): ParsedFindings {
        val findingsArray = extractFindingsArray(rawContent)
            ?: return ParsedFindings(emptyList(), listOf("Missing findings array in AI response"))

        val objectChunks = extractJsonObjects(findingsArray)
        val findings = mutableListOf<GuardrailFinding>()
        val warnings = mutableListOf<String>()

        objectChunks.forEachIndexed { index, chunk ->
            val ruleId = extractStringField(chunk, "rule_id")
            val title = extractStringField(chunk, "title")
            val filePath = extractStringField(chunk, "file_path")
            val explanation = extractStringField(chunk, "explanation")
            val suggestion = extractStringField(chunk, "suggestion")
            val severityRaw = extractStringField(chunk, "severity")

            if (ruleId == null || title == null || filePath == null || explanation == null || suggestion == null) {
                warnings += "Skipped invalid finding at index $index: missing required fields"
                return@forEachIndexed
            }

            val severity = if (severityRaw == null) {
                warnings += "Finding at index $index missing severity, defaulted to info"
                FindingSeverity.INFO
            } else {
                val normalized = FindingSeverity.from(severityRaw)
                if (normalized == FindingSeverity.INFO && severityRaw.lowercase() !in setOf("info", "warning", "error")) {
                    warnings += "Finding at index $index has invalid severity '$severityRaw', defaulted to info"
                }
                normalized
            }

            findings += GuardrailFinding(
                severity = severity,
                ruleId = ruleId,
                title = title,
                filePath = filePath,
                explanation = explanation,
                suggestion = suggestion,
                codeExample = extractStringField(chunk, "code_example")
            )
        }

        return ParsedFindings(findings, warnings)
    }

    private fun extractFindingsArray(rawContent: String): String? {
        val keyIndex = rawContent.indexOf("\"findings\"")
        if (keyIndex < 0) return null

        val startBracket = rawContent.indexOf('[', keyIndex)
        if (startBracket < 0) return null

        var depth = 0
        for (i in startBracket until rawContent.length) {
            when (rawContent[i]) {
                '[' -> depth++
                ']' -> {
                    depth--
                    if (depth == 0) {
                        return rawContent.substring(startBracket + 1, i)
                    }
                }
            }
        }

        return null
    }

    private fun extractJsonObjects(arrayContent: String): List<String> {
        val results = mutableListOf<String>()
        var depth = 0
        var start = -1

        arrayContent.forEachIndexed { index, char ->
            if (char == '{') {
                if (depth == 0) start = index
                depth++
            } else if (char == '}') {
                depth--
                if (depth == 0 && start >= 0) {
                    results += arrayContent.substring(start, index + 1)
                    start = -1
                }
            }
        }

        return results
    }

    private fun extractStringField(chunk: String, field: String): String? {
        val regex = Regex("\"$field\"\\s*:\\s*\"([^\"]*)\"")
        return regex.find(chunk)?.groupValues?.get(1)
    }
}

data class ParsedFindings(
    val findings: List<GuardrailFinding>,
    val warnings: List<String>
)
