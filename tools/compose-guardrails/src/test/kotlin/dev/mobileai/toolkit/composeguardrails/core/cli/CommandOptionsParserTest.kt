package dev.mobileai.toolkit.composeguardrails.core.cli

import dev.mobileai.toolkit.composeguardrails.core.prompt.RuleSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommandOptionsParserTest {
    private val parser = CommandOptionsParser()

    @Test
    fun `default behavior uses default rule set when omitted`() {
        val parsed = parser.parse(arrayOf("guardrails", "check", "src"))

        assertEquals(RuleSet.DEFAULT, parsed.ruleSet)
        assertEquals("src", parsed.inputPath.toString())
        assertNull(parsed.outputPath)
    }

    @Test
    fun `explicit default rule set`() {
        val parsed = parser.parse(arrayOf("guardrails", "check", "src", "--rule-set", "default"))

        assertEquals(RuleSet.DEFAULT, parsed.ruleSet)
        assertNull(parsed.outputPath)
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
    fun `output path is parsed correctly`() {
        val parsed = parser.parse(arrayOf("guardrails", "check", "src", "--output", "artifacts/report.md"))

        assertEquals("artifacts/report.md", parsed.outputPath.toString())
        assertEquals(RuleSet.DEFAULT, parsed.ruleSet)
    }

    @Test
    fun `output works with explicit rule set`() {
        val parsed = parser.parse(
            arrayOf(
                "guardrails",
                "check",
                "src",
                "--rule-set",
                "advanced",
                "--output",
                "artifacts/report.md"
            )
        )

        assertEquals(RuleSet.ADVANCED, parsed.ruleSet)
        assertEquals("artifacts/report.md", parsed.outputPath.toString())
    }

    @Test
    fun `output works before rule set option`() {
        val parsed = parser.parse(
            arrayOf(
                "guardrails",
                "check",
                "src",
                "--output",
                "artifacts/report.md",
                "--rule-set",
                "all"
            )
        )

        assertEquals(RuleSet.ALL, parsed.ruleSet)
        assertEquals("artifacts/report.md", parsed.outputPath.toString())
    }

    @Test
    fun `invalid rule-set value fails with clear message`() {
        val error = assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("guardrails", "check", "src", "--rule-set", "random"))
        }

        assertTrue(error.message!!.contains("Supported values: default, advanced, all"))
    }

    @Test
    fun `missing output value fails clearly`() {
        val error = assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("guardrails", "check", "src", "--output"))
        }

        assertTrue(error.message!!.contains("Missing value for --output"))
    }
}
