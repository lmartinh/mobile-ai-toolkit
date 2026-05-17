package dev.mobileai.toolkit.aiclient.anthropic

import dev.mobileai.toolkit.aiclient.AiClient
import dev.mobileai.toolkit.aiclient.AiProviderConfig
import dev.mobileai.toolkit.aiclient.AiRequest
import dev.mobileai.toolkit.aiclient.AiResponse
import dev.mobileai.toolkit.aiclient.HttpTransport
import dev.mobileai.toolkit.aiclient.JdkHttpTransport
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AnthropicAiClient(
    private val config: AiProviderConfig,
    private val transport: HttpTransport = JdkHttpTransport(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AiClient {

    init {
        require(config.provider.lowercase() == "anthropic") { "AnthropicAiClient requires provider=anthropic" }
        require(!config.apiKey.isNullOrBlank()) { "AnthropicAiClient requires non-empty apiKey" }
        require(!config.model.isNullOrBlank()) { "AnthropicAiClient requires non-empty model" }
    }

    override fun generate(request: AiRequest): AiResponse {
        val body = buildJsonObject {
            put("model", JsonPrimitive(config.model))
            put("max_tokens", JsonPrimitive(4096))
            put("messages", buildJsonArray {
                add(buildJsonObject {
                    put("role", JsonPrimitive("user"))
                    put("content", JsonPrimitive(request.prompt))
                })
            })
        }.toString()

        val result = try {
            transport.postJson(
                url = ANTHROPIC_MESSAGES_URL,
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "x-api-key" to config.apiKey!!,
                    "anthropic-version" to "2023-06-01"
                ),
                body = body
            )
        } catch (ex: Exception) {
            throw IllegalStateException("Anthropic request failed: ${ex.message}")
        }

        if (result.statusCode !in 200..299) {
            throw IllegalStateException("Anthropic API error (${result.statusCode}): ${result.body.take(300)}")
        }

        val content = try {
            extractText(result.body)
        } catch (ex: Exception) {
            throw IllegalStateException("Anthropic response parse error: ${ex.message}")
        }

        return AiResponse(
            content = content,
            model = config.model!!,
            metadata = mapOf("provider" to "anthropic")
        )
    }

    private fun extractText(rawJson: String): String {
        val root = json.parseToJsonElement(rawJson).jsonObject
        val contentArr = root["content"]?.jsonArray ?: throw IllegalArgumentException("Missing 'content' field")
        if (contentArr.isEmpty()) throw IllegalArgumentException("Empty 'content' array")

        return contentArr.joinToString("") { item ->
            val obj = item.jsonObject
            obj["text"]?.jsonPrimitive?.content ?: ""
        }.trim()
    }

    private companion object {
        const val ANTHROPIC_MESSAGES_URL = "https://api.anthropic.com/v1/messages"
    }
}
