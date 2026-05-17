package dev.mobileai.toolkit.aiclient.openai

import dev.mobileai.toolkit.aiclient.AiProviderConfig
import dev.mobileai.toolkit.aiclient.AiRequest
import dev.mobileai.toolkit.aiclient.HttpResult
import dev.mobileai.toolkit.aiclient.HttpTransport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OpenAiClientTest {
    @Test
    fun `maps successful response to AiResponse`() {
        val client = OpenAiClient(
            config = AiProviderConfig("openai", "test-key", "gpt-4.1-mini"),
            transport = FixedTransport(
                HttpResult(
                    200,
                    """
                    {
                      "choices": [
                        {
                          "message": {
                            "content": "{\"findings\":[]}"
                          }
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )
        )

        val response = client.generate(AiRequest(prompt = "hello"))

        assertEquals("{\"findings\":[]}", response.content)
        assertEquals("openai", response.metadata["provider"])
    }

    @Test
    fun `maps api error to graceful failure without exposing api key`() {
        val key = "sk-secret-123"
        val client = OpenAiClient(
            config = AiProviderConfig("openai", key, "gpt-4.1-mini"),
            transport = FixedTransport(HttpResult(401, "unauthorized"))
        )

        val error = assertFailsWith<IllegalStateException> {
            client.generate(AiRequest(prompt = "hello"))
        }

        assertTrue(error.message!!.contains("OpenAI API error"))
        assertTrue(!error.message!!.contains(key))
    }

    @Test
    fun `malformed response fails gracefully without exposing api key`() {
        val key = "sk-secret-123"
        val client = OpenAiClient(
            config = AiProviderConfig("openai", key, "gpt-4.1-mini"),
            transport = FixedTransport(HttpResult(200, "{\"choices\":[]}"))
        )

        val error = assertFailsWith<IllegalStateException> {
            client.generate(AiRequest(prompt = "hello"))
        }

        assertTrue(error.message!!.contains("OpenAI response parse error"))
        assertTrue(!error.message!!.contains(key))
    }

    private class FixedTransport(private val result: HttpResult) : HttpTransport {
        override fun postJson(url: String, headers: Map<String, String>, body: String, timeoutSeconds: Long): HttpResult = result
    }
}
