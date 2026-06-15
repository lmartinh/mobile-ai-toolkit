package dev.mobileai.toolkit.kmpprojectauditor.core.audit

enum class KmpFindingSeverity {
    ERROR,
    WARNING,
    INFO
}

data class KmpFinding(
    val ruleId: String,
    val severity: KmpFindingSeverity,
    val title: String,
    val file: String,
    val explanation: String,
    val suggestion: String,
    val evidence: String? = null,
    val lineNumber: Int? = null
)
