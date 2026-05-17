package dev.mobileai.toolkit.aiclient

object AiClientFactory {
    fun create(provider: String): AiClient {
        return when (provider.lowercase()) {
            "fake" -> FakeAiClient()
            else -> throw IllegalArgumentException("Unsupported AI provider: $provider")
        }
    }
}
