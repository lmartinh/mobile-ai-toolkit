package dev.mobileai.toolkit.aiclient.anthropic

import dev.mobileai.toolkit.aiclient.AiProviderConfig
import dev.mobileai.toolkit.aiclient.AiRequest
import dev.mobileai.toolkit.aiclient.HttpResult
import dev.mobileai.toolkit.aiclient.HttpTransport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AnthropicAiClientTest {
    @Test
    fun `maps successful response to AiResponse`() {
        val client = AnthropicAiClient(
            config = AiProviderConfig("anthropic", "test-key", "claude-3-5-sonnet"),
            transport = FixedTransport(
                HttpResult(
                    statusCode = 200,
                    body = """
                        {"content":[{"type":"text","text":"{\"findings\":[]}"}]}
                    """.trimIndent()
                )
            )
        )

        val response = client.generate(AiRequest(prompt = "hello"))

        assertEquals("{\"findings\":[]}", response.content)
        assertEquals("anthropic", response.metadata["provider"])
    }

    @Test
    fun `maps api error to graceful failure`() {
        val client = AnthropicAiClient(
            config = AiProviderConfig("anthropic", "test-key", "claude-3-5-sonnet"),
            transport = FixedTransport(HttpResult(401, "unauthorized"))
        )

        val error = assertFailsWith<IllegalStateException> {
            client.generate(AiRequest(prompt = "hello"))
        }

        assertTrue(error.message!!.contains("Anthropic API error"))
    }

    private class FixedTransport(private val result: HttpResult) : HttpTransport {
        override fun postJson(url: String, headers: Map<String, String>, body: String, timeoutSeconds: Long): HttpResult = result
    }
}
