package dev.mobileai.toolkit.composeguardrails.core.prompt

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PromptAssetLoaderTest {
    @Test
    fun `load reads required prompts and rules in deterministic order`() {
        val promptsDir = createPromptsDirectory()
        Files.writeString(promptsDir.resolve("compose-review.md"), "review")
        Files.writeString(promptsDir.resolve("output-format.md"), "format")

        val rulesDir = Files.createDirectories(promptsDir.resolve("rules"))
        Files.writeString(rulesDir.resolve("z-rule.md"), "rule-z")
        Files.writeString(rulesDir.resolve("a-rule.md"), "rule-a")

        val assets = PromptAssetLoader(promptsDir).load()

        assertEquals("review", assets.composeReview)
        assertEquals("format", assets.outputFormat)
        assertEquals(listOf("a-rule", "z-rule"), assets.rules.map { it.id })
    }

    @Test
    fun `load fails when required file is missing`() {
        val promptsDir = createPromptsDirectory()
        Files.writeString(promptsDir.resolve("compose-review.md"), "review")
        Files.createDirectories(promptsDir.resolve("rules"))

        assertFailsWith<IllegalArgumentException> {
            PromptAssetLoader(promptsDir).load()
        }
    }

    @Test
    fun `load fails when rule directory is empty`() {
        val promptsDir = createPromptsDirectory()
        Files.writeString(promptsDir.resolve("compose-review.md"), "review")
        Files.writeString(promptsDir.resolve("output-format.md"), "format")
        Files.createDirectories(promptsDir.resolve("rules"))

        assertFailsWith<IllegalArgumentException> {
            PromptAssetLoader(promptsDir).load()
        }
    }

    private fun createPromptsDirectory() = Files.createTempDirectory("prompt-loader-test")
}
