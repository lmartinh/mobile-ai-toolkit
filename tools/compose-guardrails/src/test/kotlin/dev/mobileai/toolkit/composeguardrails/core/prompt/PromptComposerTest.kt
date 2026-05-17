package dev.mobileai.toolkit.composeguardrails.core.prompt

import dev.mobileai.toolkit.composeguardrails.core.ComposableFunctionCandidate
import dev.mobileai.toolkit.composeguardrails.core.ComposeFileAnalysis
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PromptComposerTest {
    private val composer = PromptComposer()

    @Test
    fun `compose builds prompt with sorted rules and compose context`() {
        val file = Files.createTempFile("compose-prompt", ".kt")
        Files.writeString(file, "@Composable\nfun Screen() {}")

        val assets = PromptAssets(
            composeReview = "review prompt",
            outputFormat = "output format",
            rules = listOf(
                RulePrompt("z-rule", "z content"),
                RulePrompt("a-rule", "a content")
            )
        )
        val analyses = listOf(
            ComposeFileAnalysis(
                filePath = file,
                hasComposeImport = true,
                composableFunctions = listOf(ComposableFunctionCandidate("Screen", 2))
            )
        )

        val bundle = composer.compose(assets, analyses)

        assertEquals(listOf("a-rule", "z-rule"), bundle.activeRuleIds)
        assertEquals(1, bundle.composeCandidateCount)
        assertTrue(bundle.promptText.contains("### Rule: a-rule"))
        assertTrue(bundle.promptText.contains("### File: ${file}"))
        assertTrue(bundle.promptText.contains("- Screen (line 2)"))
    }

    @Test
    fun `compose handles no compose candidates`() {
        val assets = PromptAssets(
            composeReview = "review prompt",
            outputFormat = "output format",
            rules = listOf(RulePrompt("rule", "rule content"))
        )
        val analyses = emptyList<ComposeFileAnalysis>()

        val bundle = composer.compose(assets, analyses)

        assertEquals(0, bundle.composeCandidateCount)
        assertTrue(bundle.promptText.contains("No Compose candidate files were detected."))
    }
}
