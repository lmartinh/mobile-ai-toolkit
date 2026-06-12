package dev.mobileai.toolkit.composeguardrails.core.parsing

import dev.mobileai.toolkit.report.FindingSeverity
import dev.mobileai.toolkit.report.GuardrailFinding
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class FindingParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
) {
    fun parse(rawContent: String): ParsedFindings {
        val payload = try {
            json.decodeFromString<AiFindingsPayload>(rawContent)
        } catch (_: SerializationException) {
            return ParsedFindings(emptyList(), listOf("Invalid JSON AI response: unable to decode findings payload"))
        } catch (_: IllegalArgumentException) {
            return ParsedFindings(emptyList(), listOf("Invalid JSON AI response: unable to decode findings payload"))
        }

        val warnings = mutableListOf<String>()
        val findings = payload.findings.mapIndexedNotNull { index, finding ->
            val severity = normalizeSeverity(finding.severity, index, warnings)
            val ruleId = finding.ruleId?.trim().orEmpty()
            val title = finding.title?.trim().orEmpty()
            val filePath = finding.filePath?.trim().orEmpty()
            val explanation = finding.explanation?.trim().orEmpty()
            val suggestion = finding.suggestion?.trim().orEmpty()

            if (ruleId.isEmpty() || title.isEmpty() || filePath.isEmpty() || explanation.isEmpty() || suggestion.isEmpty()) {
                warnings += "Skipped invalid finding at index $index: missing required fields"
                return@mapIndexedNotNull null
            }

            GuardrailFinding(
                severity = severity,
                ruleId = ruleId,
                title = title,
                filePath = filePath,
                explanation = explanation,
                suggestion = suggestion,
                codeExample = finding.codeExample
            )
        }

        return ParsedFindings(findings, warnings)
    }

    private fun normalizeSeverity(
        rawSeverity: String?,
        index: Int,
        warnings: MutableList<String>
    ): FindingSeverity {
        if (rawSeverity.isNullOrBlank()) {
            warnings += "Finding at index $index missing severity, defaulted to info"
            return FindingSeverity.INFO
        }

        val normalized = FindingSeverity.from(rawSeverity)
        if (normalized == FindingSeverity.INFO && rawSeverity.lowercase() !in setOf("error", "warning", "info")) {
            warnings += "Finding at index $index has invalid severity '$rawSeverity', defaulted to info"
        }

        return normalized
    }
}

@Serializable
private data class AiFindingsPayload(
    val findings: List<AiFindingDto>
)

@Serializable
private data class AiFindingDto(
    val severity: String? = null,
    @SerialName("rule_id") val ruleId: String? = null,
    val title: String? = null,
    @SerialName("file_path") val filePath: String? = null,
    val explanation: String? = null,
    val suggestion: String? = null,
    @SerialName("code_example") val codeExample: String? = null
)

data class ParsedFindings(
    val findings: List<GuardrailFinding>,
    val warnings: List<String>
)
