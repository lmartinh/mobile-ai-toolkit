package dev.mobileai.toolkit.composeguardrails.core.analysis

import dev.mobileai.toolkit.aiclient.AiClient
import dev.mobileai.toolkit.aiclient.AiRequest
import dev.mobileai.toolkit.aiclient.AiResponse
import dev.mobileai.toolkit.composeguardrails.core.prompt.PromptBundle

class GuardrailsAiAnalyzer(private val aiClient: AiClient) {
    fun analyze(promptBundle: PromptBundle): GuardrailsAiResult {
        val response = aiClient.generate(
            AiRequest(
                prompt = promptBundle.promptText,
                metadata = mapOf(
                    "mode" to "default",
                    "active_rule_count" to promptBundle.activeRuleIds.size.toString(),
                    "compose_candidate_count" to promptBundle.composeCandidateCount.toString()
                )
            )
        )

        return GuardrailsAiResult(
            content = response.content,
            model = response.model,
            metadata = response.metadata
        )
    }
}

data class GuardrailsAiResult(
    val content: String,
    val model: String,
    val metadata: Map<String, String>
)
