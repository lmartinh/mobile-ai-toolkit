package dev.mobileai.toolkit.aiclient

import dev.mobileai.toolkit.aiclient.anthropic.AnthropicAiClient
import dev.mobileai.toolkit.aiclient.gemini.GeminiAiClient
import dev.mobileai.toolkit.aiclient.openai.OpenAiClient

object AiClientFactory {
    fun create(config: AiProviderConfig): AiClient {
        return when (config.provider.lowercase()) {
            "fake" -> FakeAiClient()
            "openai" -> OpenAiClient(config)
            "anthropic" -> AnthropicAiClient(config)
            "gemini" -> GeminiAiClient(config)
            else -> throw IllegalArgumentException(
                "Unsupported AI provider: ${config.provider}. Supported providers: fake, openai, anthropic, gemini"
            )
        }
    }
}
