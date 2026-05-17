package dev.mobileai.toolkit.aiclient

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FakeAiClientTest {
    @Test
    fun `fake client returns deterministic default response`() {
        val client = FakeAiClient()

        val response = client.generate(
            AiRequest(
                prompt = "hello",
                metadata = mapOf("mode" to "default")
            )
        )

        assertEquals("fake-ai-client-v1", response.model)
        assertEquals("fake", response.metadata["provider"])
        assertTrue(response.content.contains("\"prompt_length\": 5"))
        assertTrue(response.content.contains("\"findings\""))
    }

    @Test
    fun `factory returns fake client`() {
        val client = AiClientFactory.create("fake")

        val response = client.generate(AiRequest(prompt = "sample"))

        assertEquals("fake", response.metadata["provider"])
    }

    @Test
    fun `factory fails for unsupported provider`() {
        assertFailsWith<IllegalArgumentException> {
            AiClientFactory.create("unknown")
        }
    }
}
