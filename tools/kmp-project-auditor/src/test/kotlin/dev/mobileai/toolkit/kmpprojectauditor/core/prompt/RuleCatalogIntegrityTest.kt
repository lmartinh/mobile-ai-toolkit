package dev.mobileai.toolkit.kmpprojectauditor.core.prompt

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RuleCatalogIntegrityTest {
    private val rulesDocPath = Path.of("docs/rules.md")
    private val promptRulesIndexPath = Path.of("src/main/resources/prompts/rules/index.txt")

    @Test
    fun `rules doc includes implemented deterministic rules`() {
        val rulesDoc = rulesDocPath.readText()

        deterministicRuleIds().forEach { ruleId ->
            assertTrue(rulesDoc.contains(ruleId), "Missing deterministic rule in docs/rules.md: $ruleId")
        }
    }

    @Test
    fun `rules doc includes ai assisted and future rule ids`() {
        val rulesDoc = rulesDocPath.readText()

        aiAndFutureRuleIds().forEach { ruleId ->
            assertTrue(rulesDoc.contains(ruleId), "Missing AI/future rule in docs/rules.md: $ruleId")
        }
    }

    @Test
    fun `prompt rule index files exist and contain required sections`() {
        val indexedFiles = promptRulesIndexPath.readText()
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        assertEquals(indexedFiles.sorted(), indexedFiles, "Rule index should be stable and sorted")

        indexedFiles.forEach { fileName ->
            val filePath = Path.of("src/main/resources/prompts/rules/$fileName")
            assertTrue(filePath.exists(), "Indexed rule file does not exist: $fileName")
            val content = filePath.readText().trim()
            assertTrue(content.isNotEmpty(), "Indexed rule file is empty: $fileName")
            assertTrue(content.contains("# Rule Intent"), "Missing 'Rule Intent' section in $fileName")
            assertTrue(content.contains("# Evidence To Use"), "Missing 'Evidence To Use' section in $fileName")
            assertTrue(content.contains("# Do Not Report"), "Missing 'Do Not Report' section in $fileName")
            assertTrue(content.contains("# False-Positive Notes"), "Missing 'False-Positive Notes' section in $fileName")
        }
    }

    @Test
    fun `expected reports only reference documented rule ids`() {
        val documentedRuleIds = extractRuleIds(rulesDocPath.readText())
        val cleanReportRuleIds = extractRuleIds(Path.of("examples/clean-kmp-library/expected-report.md").readText())
        val badReportRuleIds = extractRuleIds(Path.of("examples/bad-kmp-library/expected-report.md").readText())

        (cleanReportRuleIds + badReportRuleIds).forEach { ruleId ->
            assertTrue(documentedRuleIds.contains(ruleId), "Expected report references undocumented rule id: $ruleId")
        }
    }

    @Test
    fun `expected reports align with current deterministic rule intent`() {
        val cleanReport = Path.of("examples/clean-kmp-library/expected-report.md").readText()
        val badReport = Path.of("examples/bad-kmp-library/expected-report.md").readText()

        assertTrue(cleanReport.contains("No deterministic findings found."))
        deterministicRuleIds().forEach { ruleId ->
            if (ruleId != "kmp.source-sets.android-target-without-source-set" &&
                ruleId != "kmp.source-sets.android-source-set-without-target" &&
                ruleId != "kmp.source-sets.ios-source-set-without-target"
            ) {
                assertTrue(badReport.contains(ruleId), "Bad expected report should include deterministic rule id: $ruleId")
            }
        }
    }

    private fun deterministicRuleIds(): Set<String> = setOf(
        "kmp.common.no-android-api",
        "kmp.common.no-ios-api",
        "kmp.tests.missing-common-test",
        "kmp.source-sets.android-target-without-source-set",
        "kmp.source-sets.ios-target-without-source-set",
        "kmp.source-sets.android-source-set-without-target",
        "kmp.source-sets.ios-source-set-without-target",
        "kmp.dependencies.common-platform-leak"
    )

    private fun aiAndFutureRuleIds(): Set<String> = setOf(
        "kmp.ai.source-set-clarity",
        "kmp.project.structure",
        "kmp.source-sets.intermediate-clarity",
        "kmp.dependencies.platform-placement",
        "kmp.resources.common-usage",
        "kmp.publishing.metadata",
        "kmp.api.public-surface-cleanliness",
        "kmp.docs.consumer-setup",
        "kmp.expect-actual.missing-actual",
        "kmp.expect-actual.unnecessary-expect",
        "kmp.tests.source-set-coverage"
    )

    private fun extractRuleIds(text: String): Set<String> {
        return Regex("kmp\\.[a-z0-9.-]+")
            .findAll(text)
            .map { it.value }
            .toSet()
    }
}
