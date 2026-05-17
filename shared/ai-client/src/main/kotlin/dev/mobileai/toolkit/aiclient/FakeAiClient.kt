package dev.mobileai.toolkit.aiclient

class FakeAiClient : AiClient {
    override fun generate(request: AiRequest): AiResponse {
        val mode = request.metadata["mode"] ?: "default"

        val content = when (mode) {
            "empty" -> ""
            "error-like" -> "FAKE_RESPONSE: could not infer findings"
            else -> "FAKE_RESPONSE: prompt_length=${request.prompt.length}; mode=$mode"
        }

        return AiResponse(
            content = content,
            model = "fake-ai-client-v1",
            metadata = mapOf(
                "provider" to "fake",
                "mode" to mode
            )
        )
    }
}
