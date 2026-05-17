package dev.mobileai.toolkit.composeguardrails.core.analysis

import dev.mobileai.toolkit.aiclient.FakeAiClient
import dev.mobileai.toolkit.composeguardrails.core.prompt.PromptBundle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GuardrailsAiAnalyzerTest {
    @Test
    fun `analyze delegates prompt bundle to ai client`() {
        val analyzer = GuardrailsAiAnalyzer(FakeAiClient())

        val result = analyzer.analyze(
            PromptBundle(
                promptText = "example prompt",
                activeRuleIds = listOf("rule.a", "rule.b"),
                composeCandidateCount = 2
            )
        )

        assertEquals("fake-ai-client-v1", result.model)
        assertEquals("fake", result.metadata["provider"])
        assertTrue(result.content.contains("\"prompt_length\": 14"))
        assertTrue(result.content.contains("\"findings\""))
    }
}
