package dev.mobileai.toolkit.aiclient

class FakeAiClient : AiClient {
    override fun generate(request: AiRequest): AiResponse {
        val mode = request.metadata["mode"] ?: "default"

        val content = when (mode) {
            "empty" -> """{"findings": []}"""
            "error-like" -> """{"message":"could not infer findings"}"""
            else -> """
                {
                  "findings": [
                    {
                      "severity": "warning",
                      "rule_id": "compose.no-business-logic-in-composables",
                      "title": "Business logic inside Composable",
                      "file_path": "LoginScreen.kt",
                      "explanation": "Validation logic appears directly in UI code.",
                      "suggestion": "Move validation to ViewModel or domain use case.",
                      "code_example": "viewModel.validateCredentials(email, password)"
                    }
                  ],
                  "meta": {
                    "prompt_length": ${request.prompt.length},
                    "mode": "$mode"
                  }
                }
            """.trimIndent()
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
