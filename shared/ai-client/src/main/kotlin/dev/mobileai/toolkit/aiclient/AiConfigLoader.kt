package dev.mobileai.toolkit.aiclient

class AiConfigLoader(private val env: Map<String, String> = System.getenv()) {
    fun load(): AiProviderConfig {
        val provider = env["MOBILE_AI_PROVIDER"]?.trim()?.lowercase().orEmpty().ifBlank { "fake" }
        val apiKey = env["MOBILE_AI_API_KEY"]?.trim()?.ifBlank { null }
        val model = env["MOBILE_AI_MODEL"]?.trim()?.ifBlank { null }

        return when (provider) {
            "fake" -> AiProviderConfig(provider = "fake", apiKey = apiKey, model = model)
            "openai", "anthropic", "gemini" -> {
                require(!apiKey.isNullOrBlank()) {
                    "MOBILE_AI_API_KEY is required when MOBILE_AI_PROVIDER=$provider"
                }
                require(!model.isNullOrBlank()) {
                    "MOBILE_AI_MODEL is required when MOBILE_AI_PROVIDER=$provider"
                }
                AiProviderConfig(provider = provider, apiKey = apiKey, model = model)
            }
            else -> throw IllegalArgumentException("Unsupported AI provider: $provider. Supported providers: fake, openai, anthropic, gemini")
        }
    }
}
