package dev.mobileai.toolkit.kmpprojectauditor.core.parsing

import dev.mobileai.toolkit.kmpprojectauditor.core.audit.KmpFinding
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.KmpFindingSeverity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class KmpAiFindingParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
) {
    fun parse(rawContent: String): ParsedKmpAiFindings {
        val root = try {
            json.parseToJsonElement(rawContent)
        } catch (_: Exception) {
            return ParsedKmpAiFindings(emptyList(), listOf("Unable to parse AI response."))
        }

        val findingsArray = (root as? JsonObject)?.get("findings") as? JsonArray
            ?: return ParsedKmpAiFindings(emptyList(), listOf("AI response does not contain a valid findings array."))

        val warnings = mutableListOf<String>()
        val findings = findingsArray.mapIndexedNotNull { index, element ->
            parseFinding(index, element as? JsonObject, warnings)
        }

        return ParsedKmpAiFindings(findings, warnings)
    }

    private fun parseFinding(index: Int, findingObject: JsonObject?, warnings: MutableList<String>): KmpFinding? {
        if (findingObject == null) {
            warnings += "Skipped invalid AI finding at index $index: not an object"
            return null
        }

        val ruleId = findingObject.stringValue("ruleId")
        val title = findingObject.stringValue("title")
        val file = findingObject.stringValue("file")
        val explanation = findingObject.stringValue("explanation")
        val suggestion = findingObject.stringValue("suggestion")
        val evidence = findingObject.optionalStringValue("evidence")

        if (ruleId.isNullOrBlank() || title.isNullOrBlank() || file.isNullOrBlank() || explanation.isNullOrBlank() || suggestion.isNullOrBlank()) {
            warnings += "Skipped invalid AI finding at index $index: missing required fields"
            return null
        }

        val severity = normalizeSeverity(findingObject.optionalStringValue("severity"), index, warnings)

        return KmpFinding(
            ruleId = ruleId,
            severity = severity,
            title = title,
            file = file,
            explanation = explanation,
            suggestion = suggestion,
            evidence = evidence
        )
    }

    private fun normalizeSeverity(raw: String?, index: Int, warnings: MutableList<String>): KmpFindingSeverity {
        return when (raw?.trim()?.uppercase()) {
            "ERROR" -> KmpFindingSeverity.ERROR
            "WARNING" -> KmpFindingSeverity.WARNING
            "INFO", null, "" -> KmpFindingSeverity.INFO
            else -> {
                warnings += "AI finding at index $index has invalid severity '$raw', defaulted to INFO"
                KmpFindingSeverity.INFO
            }
        }
    }

    private fun JsonObject.stringValue(key: String): String? {
        return (this[key] as? JsonPrimitive)?.contentOrNull?.trim()
    }

    private fun JsonObject.optionalStringValue(key: String): String? {
        return stringValue(key)?.ifBlank { null }
    }
}

data class ParsedKmpAiFindings(
    val findings: List<KmpFinding>,
    val warnings: List<String>
)
