package dev.mobileai.toolkit.aiclient

import dev.mobileai.toolkit.aiclient.anthropic.AnthropicAiClient
import dev.mobileai.toolkit.aiclient.gemini.GeminiAiClient
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AiClientFactoryTest {
    @Test
    fun `factory returns fake client for fake provider`() {
        val client = AiClientFactory.create(AiProviderConfig(provider = "fake"))
        assertIs<FakeAiClient>(client)
    }

    @Test
    fun `factory returns openai client for openai provider`() {
        val client = AiClientFactory.create(
            AiProviderConfig(provider = "openai", apiKey = "test-key", model = "gpt-4.1-mini")
        )
        assertIs<OpenAiClient>(client)
    }

    @Test
    fun `factory returns anthropic client for anthropic provider`() {
        val client = AiClientFactory.create(
            AiProviderConfig(provider = "anthropic", apiKey = "test-key", model = "claude-3-5-sonnet")
        )
        assertIs<AnthropicAiClient>(client)
    }

    @Test
    fun `factory returns gemini client for gemini provider`() {
        val client = AiClientFactory.create(
            AiProviderConfig(provider = "gemini", apiKey = "test-key", model = "gemini-1.5-pro")
        )
        assertIs<GeminiAiClient>(client)
    }

    @Test
    fun `factory provider name is case-insensitive`() {
        val client = AiClientFactory.create(
            AiProviderConfig(provider = "GeMiNi", apiKey = "test-key", model = "gemini-1.5-pro")
        )
        assertIs<GeminiAiClient>(client)
    }

    @Test
    fun `factory fails for unsupported provider with clear message`() {
        val error = assertFailsWith<IllegalArgumentException> {
            AiClientFactory.create(AiProviderConfig(provider = "unknown"))
        }

        assertTrue(error.message!!.contains("Supported providers"))
    }
}
