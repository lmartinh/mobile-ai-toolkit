package dev.mobileai.toolkit.composeguardrails.core.prompt

import java.net.URLClassLoader
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PromptAssetLoaderTest {
    @Test
    fun `load reads required prompts and rules in deterministic order from classpath`() {
        val resourcesRoot = createResourcesRoot()
        writePrompts(resourcesRoot)

        val classLoader = URLClassLoader(arrayOf(resourcesRoot.toUri().toURL()), null)
        val assets = PromptAssetLoader(classLoader = classLoader).load(RuleSet.DEFAULT)

        assertEquals("review", assets.composeReview)
        assertEquals("format", assets.outputFormat)
        assertEquals(listOf("a-rule", "z-rule"), assets.rules.map { it.id })
    }

    @Test
    fun `load works regardless of process working directory`() {
        val assets = PromptAssetLoader().load(RuleSet.DEFAULT)

        assertTrue(assets.composeReview.contains("Compose Review Prompt"))
        assertTrue(assets.outputFormat.contains("Return **valid JSON**"))
        assertTrue(assets.rules.isNotEmpty())
    }

    @Test
    fun `load fails when required resource is missing`() {
        val resourcesRoot = createResourcesRoot()
        resourcesRoot.resolve("prompts").createDirectories()

        val classLoader = URLClassLoader(arrayOf(resourcesRoot.toUri().toURL()), null)

        assertFailsWith<IllegalArgumentException> {
            PromptAssetLoader(classLoader = classLoader).load(RuleSet.DEFAULT)
        }
    }

    @Test
    fun `load fails when rules index is empty`() {
        val resourcesRoot = createResourcesRoot()
        val promptsRoot = resourcesRoot.resolve("prompts")
        val rulesRoot = promptsRoot.resolve("rules")
        rulesRoot.createDirectories()

        Files.writeString(promptsRoot.resolve("compose-review.md"), "review")
        Files.writeString(promptsRoot.resolve("output-format.md"), "format")
        Files.writeString(rulesRoot.resolve("default-index.txt"), "")

        val classLoader = URLClassLoader(arrayOf(resourcesRoot.toUri().toURL()), null)

        assertFailsWith<IllegalArgumentException> {
            PromptAssetLoader(classLoader = classLoader).load(RuleSet.DEFAULT)
        }
    }

    private fun createResourcesRoot() = Files.createTempDirectory("prompt-loader-classpath-test")

    private fun writePrompts(resourcesRoot: java.nio.file.Path) {
        val promptsRoot = resourcesRoot.resolve("prompts")
        val rulesRoot = promptsRoot.resolve("rules")
        rulesRoot.createDirectories()

        Files.writeString(promptsRoot.resolve("compose-review.md"), "review")
        Files.writeString(promptsRoot.resolve("output-format.md"), "format")
        Files.writeString(rulesRoot.resolve("default-index.txt"), "z-rule.md\na-rule.md\n")
        Files.writeString(rulesRoot.resolve("advanced-index.txt"), "z-rule.md\n")
        Files.writeString(rulesRoot.resolve("z-rule.md"), "rule-z")
        Files.writeString(rulesRoot.resolve("a-rule.md"), "rule-a")
    }
}
