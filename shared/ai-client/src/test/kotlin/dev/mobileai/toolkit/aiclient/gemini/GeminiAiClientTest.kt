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
    fun `maps successful response to AiResponse`() {
        val client = GeminiAiClient(
            config = AiProviderConfig("gemini", "test-key", "gemini-1.5-pro"),
            transport = FixedTransport(
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
        )

        val response = client.generate(AiRequest(prompt = "hello"))

        assertEquals("gemini-output", response.content)
        assertEquals("gemini", response.metadata["provider"])
    }

    @Test
    fun `maps api error to graceful failure`() {
        val client = GeminiAiClient(
            config = AiProviderConfig("gemini", "test-key", "gemini-1.5-pro"),
            transport = FixedTransport(HttpResult(403, "forbidden"))
        )

        val error = assertFailsWith<IllegalStateException> {
            client.generate(AiRequest(prompt = "hello"))
        }

        assertTrue(error.message!!.contains("Gemini API error"))
    }

    private class FixedTransport(private val result: HttpResult) : HttpTransport {
        override fun postJson(url: String, headers: Map<String, String>, body: String, timeoutSeconds: Long): HttpResult = result
    }
}
