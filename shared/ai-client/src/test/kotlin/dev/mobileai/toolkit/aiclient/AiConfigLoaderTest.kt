package dev.mobileai.toolkit.aiclient

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AiConfigLoaderTest {
    @Test
    fun `load defaults to fake when provider is missing`() {
        val config = AiConfigLoader(emptyMap()).load()

        assertEquals("fake", config.provider)
        assertEquals(null, config.apiKey)
        assertEquals(null, config.model)
    }

    @Test
    fun `load returns openai config when env vars are valid`() {
        val config = AiConfigLoader(
            mapOf(
                "MOBILE_AI_PROVIDER" to "openai",
                "MOBILE_AI_API_KEY" to "test-key",
                "MOBILE_AI_MODEL" to "gpt-4.1-mini"
            )
        ).load()

        assertEquals("openai", config.provider)
        assertEquals("test-key", config.apiKey)
        assertEquals("gpt-4.1-mini", config.model)
    }

    @Test
    fun `load provider name is case-insensitive`() {
        val config = AiConfigLoader(
            mapOf(
                "MOBILE_AI_PROVIDER" to "AnThRoPiC",
                "MOBILE_AI_API_KEY" to "test-key",
                "MOBILE_AI_MODEL" to "claude-3-5-sonnet"
            )
        ).load()

        assertEquals("anthropic", config.provider)
    }

    @Test
    fun `load fails when key is missing for real provider`() {
        val error = assertFailsWith<IllegalArgumentException> {
            AiConfigLoader(
                mapOf(
                    "MOBILE_AI_PROVIDER" to "openai",
                    "MOBILE_AI_MODEL" to "gpt-4.1-mini"
                )
            ).load()
        }

        assertTrue(error.message!!.contains("MOBILE_AI_API_KEY"))
    }

    @Test
    fun `load fails when model is missing for real provider`() {
        val error = assertFailsWith<IllegalArgumentException> {
            AiConfigLoader(
                mapOf(
                    "MOBILE_AI_PROVIDER" to "gemini",
                    "MOBILE_AI_API_KEY" to "test-key"
                )
            ).load()
        }

        assertTrue(error.message!!.contains("MOBILE_AI_MODEL"))
    }

    @Test
    fun `load fails for unsupported provider with clear message`() {
        val error = assertFailsWith<IllegalArgumentException> {
            AiConfigLoader(mapOf("MOBILE_AI_PROVIDER" to "unknown")).load()
        }

        assertTrue(error.message!!.contains("Supported providers"))
    }

    @Test
    fun `fake provider works without credentials`() {
        val config = AiConfigLoader(mapOf("MOBILE_AI_PROVIDER" to "fake")).load()

        assertEquals("fake", config.provider)
        assertEquals(null, config.apiKey)
        assertEquals(null, config.model)
    }
}
