package dev.mobileai.toolkit.composeguardrails.core.prompt

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList
import kotlin.test.Test
import kotlin.test.assertTrue

class PromptAssetsIntegrityTest {
    @Test
    fun `all indexed rule files exist and are loadable`() {
        val classLoader = PromptAssetsIntegrityTest::class.java.classLoader
        val indexContent = classLoader
            .getResourceAsStream("prompts/rules/index.txt")
            ?.bufferedReader(Charsets.UTF_8)
            ?.readText()
            ?: error("Missing prompts/rules/index.txt")

        val fileNames = indexContent.lines().map { it.trim() }.filter { it.isNotBlank() }
        assertTrue(fileNames.isNotEmpty(), "Rule index must not be empty")

        fileNames.forEach { fileName ->
            val resourcePath = "prompts/rules/$fileName"
            val content = classLoader
                .getResourceAsStream(resourcePath)
                ?.bufferedReader(Charsets.UTF_8)
                ?.readText()

            assertTrue(!content.isNullOrBlank(), "Rule resource missing or empty: $resourcePath")
        }
    }

    @Test
    fun `default active rule ids match expected catalog`() {
        val assets = PromptAssetLoader().load()
        val actual = assets.rules.map { "compose.${it.id}" }.toSet()
        val expected = setOf(
            "compose.android.collect-as-state-with-lifecycle",
            "compose.android.context-leak-risk",
            "compose.clickable-without-semantics",
            "compose.derived-state-usage",
            "compose.effect-key-quality",
            "compose.expensive-work-in-composition",
            "compose.hardcoded-dimensions-and-colors",
            "compose.large-composable",
            "compose.lazy-list-keys",
            "compose.missing-content-description",
            "compose.missing-modifier-parameter",
            "compose.missing-preview",
            "compose.modifier-parameter-position",
            "compose.multiplatform.no-android-api-in-common",
            "compose.multiplatform.platform-specific-ui-leak",
            "compose.multiplatform.public-api-cleanliness",
            "compose.multiplatform.resources-usage",
            "compose.no-business-logic-in-composables",
            "compose.no-side-effects-in-composition",
            "compose.preview-with-real-dependencies",
            "compose.state-hoisting",
            "compose.unidirectional-data-flow",
            "compose.unstable-parameters",
            "compose.viewmodel-in-leaf-composable"
        )

        assertTrue(actual == expected, "Default rule set does not match expected catalog")
    }

    @Test
    fun `main prompt files exist`() {
        val classLoader = PromptAssetsIntegrityTest::class.java.classLoader
        val composeReview = classLoader.getResourceAsStream("prompts/compose-review.md")
        val outputFormat = classLoader.getResourceAsStream("prompts/output-format.md")

        assertTrue(composeReview != null, "Missing prompts/compose-review.md")
        assertTrue(outputFormat != null, "Missing prompts/output-format.md")
    }

    @Test
    fun `all examples contain expected report`() {
        val examplesRoot = Path.of("examples")
        val exampleDirs = Files.list(examplesRoot).use { stream ->
            stream.filter { Files.isDirectory(it) }.toList()
        }

        assertTrue(exampleDirs.isNotEmpty(), "No example directories found")

        exampleDirs.forEach { dir ->
            val expected = dir.resolve("expected-report.md")
            assertTrue(Files.exists(expected), "Missing expected-report.md in ${dir.fileName}")

            val kotlinFiles = Files.list(dir).use { stream ->
                stream.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".kt") }.toList()
            }
            assertTrue(kotlinFiles.isNotEmpty(), "Missing Kotlin file in ${dir.fileName}")
        }
    }

    @Test
    fun `clean example is documented as no findings`() {
        val expected = Path.of("examples/clean-compose-sample/expected-report.md")
        val text = Files.readString(expected)

        assertTrue(text.contains("Expected findings: 0"))
        assertTrue(text.contains("No guardrail violations detected."))
    }

    @Test
    fun `prompt composition includes loaded rule content`() {
        val assets = PromptAssetLoader().load()
        val composed = PromptComposer().compose(assets, emptyList()).promptText

        assertTrue(composed.contains("## Active Rules"))
        assertTrue(composed.contains("Rule: large-composable"))
        assertTrue(composed.contains("Rule: unidirectional-data-flow"))
        assertTrue(composed.contains("Rule: multiplatform.no-android-api-in-common"))
    }
}
