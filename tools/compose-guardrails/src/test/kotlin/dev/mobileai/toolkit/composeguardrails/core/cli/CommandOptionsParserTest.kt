package dev.mobileai.toolkit.composeguardrails.core.cli

import dev.mobileai.toolkit.composeguardrails.core.prompt.RuleSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CommandOptionsParserTest {
    private val parser = CommandOptionsParser()

    @Test
    fun `default behavior uses default rule set when omitted`() {
        val parsed = parser.parse(arrayOf("guardrails", "check", "src"))

        assertEquals(RuleSet.DEFAULT, parsed.ruleSet)
        assertEquals("src", parsed.inputPath.toString())
    }

    @Test
    fun `explicit default rule set`() {
        val parsed = parser.parse(arrayOf("guardrails", "check", "src", "--rule-set", "default"))

        assertEquals(RuleSet.DEFAULT, parsed.ruleSet)
    }

    @Test
    fun `explicit advanced rule set`() {
        val parsed = parser.parse(arrayOf("guardrails", "check", "src", "--rule-set", "advanced"))

        assertEquals(RuleSet.ADVANCED, parsed.ruleSet)
    }

    @Test
    fun `explicit all rule set`() {
        val parsed = parser.parse(arrayOf("guardrails", "check", "src", "--rule-set", "all"))

        assertEquals(RuleSet.ALL, parsed.ruleSet)
    }

    @Test
    fun `invalid rule-set value fails with clear message`() {
        val error = assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("guardrails", "check", "src", "--rule-set", "random"))
        }

        assertTrue(error.message!!.contains("Supported values: default, advanced, all"))
    }
}
