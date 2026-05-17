package dev.mobileai.toolkit.aiclient

data class AiProviderConfig(
    val provider: String,
    val apiKey: String? = null,
    val model: String? = null
)
