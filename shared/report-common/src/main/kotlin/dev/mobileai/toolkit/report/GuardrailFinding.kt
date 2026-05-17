package dev.mobileai.toolkit.report

enum class FindingSeverity {
    ERROR,
    WARNING,
    INFO;

    companion object {
        fun from(value: String): FindingSeverity {
            return when (value.trim().lowercase()) {
                "error" -> ERROR
                "warning" -> WARNING
                "info" -> INFO
                else -> INFO
            }
        }
    }
}

data class GuardrailFinding(
    val severity: FindingSeverity,
    val ruleId: String,
    val title: String,
    val filePath: String,
    val explanation: String,
    val suggestion: String,
    val codeExample: String?
)
