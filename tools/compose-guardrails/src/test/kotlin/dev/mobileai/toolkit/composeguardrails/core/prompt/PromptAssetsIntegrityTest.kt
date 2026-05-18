package dev.mobileai.toolkit.composeguardrails.core.prompt

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList
import kotlin.test.Test
import kotlin.test.assertTrue

class PromptAssetsIntegrityTest {
    @Test
    fun `all indexed default rule files exist and are loadable`() {
        val fileNames = loadIndex("default-index.txt")
        assertTrue(fileNames.isNotEmpty(), "Default rule index must not be empty")

        fileNames.forEach { fileName ->
            val content = loadRule(fileName)
            assertTrue(content.isNotBlank(), "Default rule resource missing or empty: $fileName")
        }
    }

    @Test
    fun `all indexed advanced rule files exist and are loadable`() {
        val fileNames = loadIndex("advanced-index.txt")
        assertTrue(fileNames.isNotEmpty(), "Advanced rule index must not be empty")

        fileNames.forEach { fileName ->
            val content = loadRule(fileName)
            assertTrue(content.isNotBlank(), "Advanced rule resource missing or empty: $fileName")
        }
    }

    @Test
    fun `default active rule ids match expected conservative catalog`() {
        val assets = PromptAssetLoader().load(RuleSet.DEFAULT)
        val actual = assets.rules.map { "compose.${it.id}" }.toSet()
        val expected = setOf(
            "compose.no-business-logic-in-composables",
            "compose.state-hoisting",
            "compose.viewmodel-in-leaf-composable",
            "compose.unidirectional-data-flow",
            "compose.no-side-effects-in-composition",
            "compose.effect-key-quality",
            "compose.lazy-list-keys",
            "compose.missing-modifier-parameter",
            "compose.modifier-parameter-position",
            "compose.missing-content-description",
            "compose.clickable-without-semantics",
            "compose.android.collect-as-state-with-lifecycle",
            "compose.android.context-leak-risk",
            "compose.multiplatform.no-android-api-in-common",
            "compose.multiplatform.platform-specific-ui-leak",
            "compose.multiplatform.public-api-cleanliness"
        )

        assertTrue(actual == expected, "Default rule set does not match expected conservative catalog")
    }

    @Test
    fun `every indexed rule contains required metadata sections`() {
        val allRuleFiles = loadIndex("default-index.txt") + loadIndex("advanced-index.txt")
        val requiredMarkers = listOf(
            "# Rule: compose.",
            "- category:",
            "- goal:",
            "- recommended severity:",
            "## What to detect",
            "## What not to detect",
            "## Bad example",
            "## Improved example",
            "## Guidance for actionable suggestions",
            "## False positive notes"
        )

        allRuleFiles.forEach { fileName ->
            val content = loadRule(fileName)
            requiredMarkers.forEach { marker ->
                assertTrue(content.contains(marker), "Rule $fileName is missing required section: $marker")
            }
        }
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
    fun `all examples contain expected report and kotlin file`() {
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
    fun `example reports only reference known active rule ids and no legacy ids`() {
        val knownRuleIds = (loadIndex("default-index.txt") + loadIndex("advanced-index.txt"))
            .map { "compose.${it.removeSuffix(".md")}" }
            .toSet()

        val legacyRuleIds = setOf(
            "compose.previews",
            "compose.remember-usage",
            "compose.separation-of-concerns"
        )

        val reportFiles = Files.walk(Path.of("examples")).use { stream ->
            stream.filter { Files.isRegularFile(it) && it.fileName.toString() == "expected-report.md" }.toList()
        }

        reportFiles.forEach { report ->
            val text = Files.readString(report)
            assertTrue(text.contains("# Compose Guardrails Report"), "Missing report title in $report")
            assertTrue(text.contains("## Summary"), "Missing Summary section in $report")
            assertTrue(text.contains("## Findings"), "Missing Findings section in $report")

            legacyRuleIds.forEach { legacy ->
                assertTrue(!text.contains(legacy), "Legacy rule id found in ${report.fileName}: $legacy")
            }

            Regex("compose\\.[a-z0-9.-]+").findAll(text).map { it.value }.toSet().forEach { ruleId ->
                assertTrue(ruleId in knownRuleIds, "Unknown rule id in example report $report: $ruleId")
            }

            Regex("Severity: `([a-z]+)`").findAll(text).forEach { match ->
                val severity = match.groupValues[1]
                assertTrue(severity in setOf("error", "warning", "info"), "Invalid severity '$severity' in $report")
            }
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
    fun `renamed expensive work example exists and old remember misuse example is removed`() {
        val newDir = Path.of("examples/expensive-work-in-composition-sample")
        val oldDir = Path.of("examples/remember-misuse-sample")

        assertTrue(Files.exists(newDir), "Expected renamed example directory to exist")
        assertTrue(!Files.exists(oldDir), "Old example directory should not exist anymore")
    }

    @Test
    fun `prompt composition includes rule content from selected catalog`() {
        val assets = PromptAssetLoader().load(RuleSet.DEFAULT)
        val composed = PromptComposer().compose(assets, emptyList()).promptText

        assertTrue(composed.contains("## Active Rules"))
        assertTrue(composed.contains("Rule: no-business-logic-in-composables"))
        assertTrue(composed.contains("Rule: android.collect-as-state-with-lifecycle"))
    }

    private fun loadIndex(indexName: String): List<String> {
        val classLoader = PromptAssetsIntegrityTest::class.java.classLoader
        val content = classLoader
            .getResourceAsStream("prompts/rules/$indexName")
            ?.bufferedReader(Charsets.UTF_8)
            ?.readText()
            ?: error("Missing prompts/rules/$indexName")

        return content.lines().map { it.trim() }.filter { it.isNotBlank() }.distinct()
    }

    private fun loadRule(fileName: String): String {
        val classLoader = PromptAssetsIntegrityTest::class.java.classLoader
        return classLoader
            .getResourceAsStream("prompts/rules/$fileName")
            ?.bufferedReader(Charsets.UTF_8)
            ?.readText()
            ?.trim()
            ?: ""
    }
}
