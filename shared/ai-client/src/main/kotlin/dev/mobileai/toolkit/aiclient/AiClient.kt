package dev.mobileai.toolkit.aiclient

interface AiClient {
    fun generate(request: AiRequest): AiResponse
}

data class AiRequest(
    val prompt: String,
    val metadata: Map<String, String> = emptyMap()
)

data class AiResponse(
    val content: String,
    val model: String,
    val metadata: Map<String, String> = emptyMap()
)
