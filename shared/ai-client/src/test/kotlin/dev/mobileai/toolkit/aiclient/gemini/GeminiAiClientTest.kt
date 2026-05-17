package dev.mobileai.toolkit.aiclient.gemini

import dev.mobileai.toolkit.aiclient.AiProviderConfig
import dev.mobileai.toolkit.aiclient.AiRequest
import dev.mobileai.toolkit.aiclient.HttpResult
import dev.mobileai.toolkit.aiclient.HttpTransport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GeminiAiClientTest {
    @Test
    fun `maps successful response to AiResponse and sends api key in header only`() {
        val key = "gemini-secret-123"
        val recorder = RecordingTransport(
            HttpResult(
                statusCode = 200,
                body = """
                    {
                      "candidates": [
                        {
                          "content": {
                            "parts": [
                              {"text": "gemini-output"}
                            ]
                          }
                        }
                      ]
                    }
                """.trimIndent()
            )
        )

        val client = GeminiAiClient(
            config = AiProviderConfig("gemini", key, "gemini-1.5-pro"),
            transport = recorder
        )

        val response = client.generate(AiRequest(prompt = "hello"))

        assertEquals("gemini-output", response.content)
        assertEquals("gemini", response.metadata["provider"])
        assertEquals(key, recorder.lastHeaders["x-goog-api-key"])
        assertTrue(!recorder.lastUrl.contains(key))
    }

    @Test
    fun `maps api error to graceful failure without exposing api key`() {
        val key = "gemini-secret-123"
        val client = GeminiAiClient(
            config = AiProviderConfig("gemini", key, "gemini-1.5-pro"),
            transport = FixedTransport(HttpResult(403, "forbidden"))
        )

        val error = assertFailsWith<IllegalStateException> {
            client.generate(AiRequest(prompt = "hello"))
        }

        assertTrue(error.message!!.contains("Gemini API error"))
        assertTrue(!error.message!!.contains(key))
    }

    @Test
    fun `malformed response fails gracefully without exposing api key`() {
        val key = "gemini-secret-123"
        val client = GeminiAiClient(
            config = AiProviderConfig("gemini", key, "gemini-1.5-pro"),
            transport = FixedTransport(HttpResult(200, "{}"))
        )

        val error = assertFailsWith<IllegalStateException> {
            client.generate(AiRequest(prompt = "hello"))
        }

        assertTrue(error.message!!.contains("Gemini response parse error"))
        assertTrue(!error.message!!.contains(key))
    }

    private class FixedTransport(private val result: HttpResult) : HttpTransport {
        override fun postJson(url: String, headers: Map<String, String>, body: String, timeoutSeconds: Long): HttpResult = result
    }

    private class RecordingTransport(private val result: HttpResult) : HttpTransport {
        var lastUrl: String = ""
        var lastHeaders: Map<String, String> = emptyMap()

        override fun postJson(url: String, headers: Map<String, String>, body: String, timeoutSeconds: Long): HttpResult {
            lastUrl = url
            lastHeaders = headers
            return result
        }
    }
}
