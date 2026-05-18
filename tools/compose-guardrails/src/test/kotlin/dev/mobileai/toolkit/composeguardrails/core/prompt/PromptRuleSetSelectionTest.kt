package dev.mobileai.toolkit.composeguardrails.core.prompt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PromptRuleSetSelectionTest {
    @Test
    fun `default rule set is loaded when explicitly selected`() {
        val assets = PromptAssetLoader().load(RuleSet.DEFAULT)
        val ids = assets.rules.map { it.id }.toSet()

        assertTrue("no-business-logic-in-composables" in ids)
        assertTrue("state-hoisting" in ids)
        assertFalse("unstable-parameters" in ids)
    }

    @Test
    fun `advanced rule set loads only advanced rules`() {
        val assets = PromptAssetLoader().load(RuleSet.ADVANCED)
        val ids = assets.rules.map { it.id }.toSet()

        assertTrue("unstable-parameters" in ids)
        assertTrue("derived-state-usage" in ids)
        assertFalse("state-hoisting" in ids)
        assertFalse("no-side-effects-in-composition" in ids)
    }

    @Test
    fun `all rule set includes default and advanced rules`() {
        val defaultIds = PromptAssetLoader().load(RuleSet.DEFAULT).rules.map { it.id }.toSet()
        val advancedIds = PromptAssetLoader().load(RuleSet.ADVANCED).rules.map { it.id }.toSet()
        val allIds = PromptAssetLoader().load(RuleSet.ALL).rules.map { it.id }.toSet()

        assertEquals(defaultIds + advancedIds, allIds)
    }

    @Test
    fun `all rule set prompt includes both default and advanced markers`() {
        val allAssets = PromptAssetLoader().load(RuleSet.ALL)
        val prompt = PromptComposer().compose(allAssets, emptyList()).promptText

        assertTrue(prompt.contains("Rule: state-hoisting"))
        assertTrue(prompt.contains("Rule: expensive-work-in-composition"))
    }

    @Test
    fun `prompt composition includes only selected rule set`() {
        val advancedAssets = PromptAssetLoader().load(RuleSet.ADVANCED)
        val prompt = PromptComposer().compose(advancedAssets, emptyList()).promptText

        assertTrue(prompt.contains("Rule: unstable-parameters"))
        assertTrue(prompt.contains("Rule: derived-state-usage"))
        assertFalse(prompt.contains("Rule: state-hoisting"))
    }
}
