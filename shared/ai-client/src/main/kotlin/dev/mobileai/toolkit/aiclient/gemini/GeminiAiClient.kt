package dev.mobileai.toolkit.aiclient.gemini

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

class GeminiAiClient(
    private val config: AiProviderConfig,
    private val transport: HttpTransport = JdkHttpTransport(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AiClient {

    init {
        require(config.provider.lowercase() == "gemini") { "GeminiAiClient requires provider=gemini" }
        require(!config.apiKey.isNullOrBlank()) { "GeminiAiClient requires non-empty apiKey" }
        require(!config.model.isNullOrBlank()) { "GeminiAiClient requires non-empty model" }
    }

    override fun generate(request: AiRequest): AiResponse {
        val body = buildJsonObject {
            put("contents", buildJsonArray {
                add(buildJsonObject {
                    put("parts", buildJsonArray {
                        add(buildJsonObject {
                            put("text", JsonPrimitive(request.prompt))
                        })
                    })
                })
            })
        }.toString()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/${config.model}:generateContent"

        val result = try {
            transport.postJson(
                url = url,
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "x-goog-api-key" to config.apiKey!!
                ),
                body = body
            )
        } catch (ex: Exception) {
            throw IllegalStateException("Gemini request failed: ${ex.message}")
        }

        if (result.statusCode !in 200..299) {
            throw IllegalStateException("Gemini API error (${result.statusCode}): ${result.body.take(300)}")
        }

        val content = try {
            extractText(result.body)
        } catch (ex: Exception) {
            throw IllegalStateException("Gemini response parse error: ${ex.message}")
        }

        return AiResponse(
            content = content,
            model = config.model!!,
            metadata = mapOf("provider" to "gemini")
        )
    }

    private fun extractText(rawJson: String): String {
        val root = json.parseToJsonElement(rawJson).jsonObject
        val candidates = root["candidates"]?.jsonArray ?: throw IllegalArgumentException("Missing 'candidates' field")
        if (candidates.isEmpty()) throw IllegalArgumentException("Empty 'candidates' array")

        val first = candidates.first().jsonObject
        val content = first["content"]?.jsonObject ?: throw IllegalArgumentException("Missing 'content' field")
        val parts = content["parts"]?.jsonArray ?: throw IllegalArgumentException("Missing 'parts' field")

        return parts.joinToString("") { part ->
            part.jsonObject["text"]?.jsonPrimitive?.content ?: ""
        }.trim()
    }
}
