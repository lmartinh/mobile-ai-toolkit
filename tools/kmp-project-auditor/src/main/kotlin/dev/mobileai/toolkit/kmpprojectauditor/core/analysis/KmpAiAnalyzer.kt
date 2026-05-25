package dev.mobileai.toolkit.kmpprojectauditor.core.analysis

import dev.mobileai.toolkit.aiclient.AiClient
import dev.mobileai.toolkit.aiclient.AiRequest
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.KmpFinding
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
            KmpAiAnalysisResult(
                findings = parsed.findings,
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
}

data class KmpAiAnalysisResult(
    val findings: List<KmpFinding>,
    val warnings: List<String>,
    val model: String,
    val provider: String
)
