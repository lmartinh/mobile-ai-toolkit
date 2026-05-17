package dev.mobileai.toolkit.aiclient.openai

import dev.mobileai.toolkit.aiclient.AiClient
import dev.mobileai.toolkit.aiclient.AiProviderConfig
import dev.mobileai.toolkit.aiclient.AiRequest
import dev.mobileai.toolkit.aiclient.AiResponse
import dev.mobileai.toolkit.aiclient.HttpTransport
import dev.mobileai.toolkit.aiclient.JdkHttpTransport
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class OpenAiClient(
    private val config: AiProviderConfig,
    private val transport: HttpTransport = JdkHttpTransport(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AiClient {

    init {
        require(config.provider.lowercase() == "openai") { "OpenAiClient requires provider=openai" }
        require(!config.apiKey.isNullOrBlank()) { "OpenAiClient requires non-empty apiKey" }
        require(!config.model.isNullOrBlank()) { "OpenAiClient requires non-empty model" }
    }

    override fun generate(request: AiRequest): AiResponse {
        val requestBody = buildJsonObject {
            put("model", JsonPrimitive(config.model))
            put("messages", buildJsonArray {
                add(
                    buildJsonObject {
                        put("role", JsonPrimitive("user"))
                        put("content", JsonPrimitive(request.prompt))
                    }
                )
            })
            put("temperature", JsonPrimitive(0.1))
        }.toString()

        val result = try {
            transport.postJson(
                url = OPENAI_CHAT_COMPLETIONS_URL,
                headers = mapOf(
                    "Authorization" to "Bearer ${config.apiKey}",
                    "Content-Type" to "application/json"
                ),
                body = requestBody
            )
        } catch (ex: Exception) {
            throw IllegalStateException("OpenAI request failed: ${ex.message}")
        }

        if (result.statusCode !in 200..299) {
            val bodyPreview = result.body.take(300)
            throw IllegalStateException("OpenAI API error (${result.statusCode}): $bodyPreview")
        }

        val content = try {
            extractContent(result.body)
        } catch (ex: Exception) {
            throw IllegalStateException("OpenAI response parse error: ${ex.message}")
        }

        return AiResponse(
            content = content,
            model = config.model!!,
            metadata = mapOf("provider" to "openai")
        )
    }

    private fun extractContent(rawJson: String): String {
        val root = json.parseToJsonElement(rawJson).jsonObject
        val choices = root["choices"]?.jsonArray ?: throw IllegalArgumentException("Missing 'choices' field")
        if (choices.isEmpty()) throw IllegalArgumentException("Empty 'choices' array")

        val firstChoice = choices.first().jsonObject
        val message = firstChoice["message"]?.jsonObject ?: throw IllegalArgumentException("Missing 'message' field")
        val contentElement = message["content"] ?: throw IllegalArgumentException("Missing 'content' field")

        return when (contentElement) {
            is JsonPrimitive -> contentElement.content
            is JsonArray -> contentElement.joinToString("") { part ->
                val partObj = part.jsonObject
                partObj["text"]?.jsonPrimitive?.content ?: ""
            }
            is JsonObject -> contentElement["text"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Unsupported object content format")
        }.trim()
    }

    private companion object {
        const val OPENAI_CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions"
    }
}
