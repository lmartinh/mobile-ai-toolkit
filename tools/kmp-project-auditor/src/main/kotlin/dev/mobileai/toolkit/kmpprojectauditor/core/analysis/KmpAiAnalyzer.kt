package dev.mobileai.toolkit.kmpprojectauditor.core.analysis

import dev.mobileai.toolkit.aiclient.AiClient
import dev.mobileai.toolkit.aiclient.AiRequest
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.KmpFinding
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.KmpFindingSeverity
import dev.mobileai.toolkit.kmpprojectauditor.core.parsing.KmpAiFindingParser
import dev.mobileai.toolkit.kmpprojectauditor.core.prompt.KmpPromptAssetLoader
import dev.mobileai.toolkit.kmpprojectauditor.core.prompt.KmpPromptComposer
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanResult

class KmpAiAnalyzer(
    private val aiClient: AiClient,
    private val promptAssetLoader: KmpPromptAssetLoader = KmpPromptAssetLoader(),
    private val promptComposer: KmpPromptComposer = KmpPromptComposer(),
    private val findingParser: KmpAiFindingParser = KmpAiFindingParser()
) {
    fun analyze(scanResult: ProjectScanResult, deterministicFindings: List<KmpFinding>): KmpAiAnalysisResult {
        return try {
            val assets = promptAssetLoader.load()
            val promptBundle = promptComposer.compose(assets, scanResult, deterministicFindings)
            val response = aiClient.generate(
                AiRequest(
                    prompt = promptBundle.promptText,
                    metadata = mapOf(
                        "mode" to "kmp-audit",
                        "snippet_count" to promptBundle.snippetPaths.size.toString(),
                        "deterministic_finding_count" to deterministicFindings.size.toString()
                    )
                )
            )
            val parsed = findingParser.parse(response.content)
            val normalizedFindings = normalizeFindings(parsed.findings)
            KmpAiAnalysisResult(
                findings = normalizedFindings,
                warnings = parsed.warnings,
                model = response.model,
                provider = response.metadata["provider"] ?: "unknown"
            )
        } catch (_: Exception) {
            KmpAiAnalysisResult(
                findings = emptyList(),
                warnings = listOf("AI analysis failed."),
                model = "unavailable",
                provider = "unavailable"
            )
        }
    }

    private fun normalizeFindings(findings: List<KmpFinding>): List<KmpFinding> {
        return findings.mapNotNull { finding ->
            val normalizedSeverity = when (finding.ruleId) {
                RULE_AI_SOURCE_SET_CLARITY,
                RULE_PROJECT_STRUCTURE,
                RULE_INTERMEDIATE_SOURCE_SET_CLARITY,
                RULE_PLATFORM_DEPENDENCY_PLACEMENT,
                RULE_RESOURCES_COMMON_USAGE,
                RULE_PUBLISHING_METADATA,
                RULE_PUBLIC_SURFACE_CLEANLINESS,
                RULE_CONSUMER_SETUP -> KmpFindingSeverity.INFO

                else -> finding.severity.cappedForAi()
            }

            val normalizedFinding = finding.copy(severity = normalizedSeverity)
            if (normalizedFinding.evidence.isNullOrBlank()) {
                return@mapNotNull null
            }

            if (normalizedFinding.ruleId == RULE_COMMON_NO_ANDROID_API &&
                !isClearlyAndroidOnlyImport(normalizedFinding.evidence)
            ) {
                return@mapNotNull null
            }

            if (normalizedFinding.ruleId == RULE_COMMON_NO_IOS_API &&
                !isClearlyIosOnlyImport(normalizedFinding.evidence)
            ) {
                return@mapNotNull null
            }

            normalizedFinding
        }
    }

    private fun KmpFindingSeverity.cappedForAi(): KmpFindingSeverity {
        return when (this) {
            KmpFindingSeverity.ERROR -> KmpFindingSeverity.WARNING
            KmpFindingSeverity.WARNING,
            KmpFindingSeverity.INFO -> this
        }
    }

    private fun isClearlyAndroidOnlyImport(evidence: String): Boolean {
        val trimmed = evidence.trim()
        if (!trimmed.startsWith("import ")) {
            return false
        }

        val importedName = trimmed.removePrefix("import ").trim()
        if (ANDROID_COMMONMAIN_ALLOWED_PREFIXES.any { importedName.startsWith(it) }) {
            return false
        }

        return ANDROID_ONLY_IMPORT_PREFIXES.any { importedName.startsWith(it) }
    }

    private fun isClearlyIosOnlyImport(evidence: String): Boolean {
        val trimmed = evidence.trim()
        return trimmed.startsWith("import platform.") || trimmed.startsWith("import kotlinx.cinterop.")
    }

    private companion object {
        const val RULE_COMMON_NO_ANDROID_API = "kmp.common.no-android-api"
        const val RULE_COMMON_NO_IOS_API = "kmp.common.no-ios-api"
        const val RULE_AI_SOURCE_SET_CLARITY = "kmp.ai.source-set-clarity"
        const val RULE_PROJECT_STRUCTURE = "kmp.project.structure"
        const val RULE_INTERMEDIATE_SOURCE_SET_CLARITY = "kmp.source-sets.intermediate-clarity"
        const val RULE_PLATFORM_DEPENDENCY_PLACEMENT = "kmp.dependencies.platform-placement"
        const val RULE_RESOURCES_COMMON_USAGE = "kmp.resources.common-usage"
        const val RULE_PUBLISHING_METADATA = "kmp.publishing.metadata"
        const val RULE_PUBLIC_SURFACE_CLEANLINESS = "kmp.api.public-surface-cleanliness"
        const val RULE_CONSUMER_SETUP = "kmp.docs.consumer-setup"

        val ANDROID_COMMONMAIN_ALLOWED_PREFIXES = listOf(
            "androidx.compose.runtime.",
            "androidx.compose.foundation.",
            "androidx.compose.material3.",
            "androidx.compose.ui."
        )

        val ANDROID_ONLY_IMPORT_PREFIXES = listOf(
            "android.",
            "androidx.activity.",
            "androidx.appcompat.",
            "androidx.core.",
            "androidx.lifecycle."
        )
    }
}

data class KmpAiAnalysisResult(
    val findings: List<KmpFinding>,
    val warnings: List<String>,
    val model: String,
    val provider: String
)
