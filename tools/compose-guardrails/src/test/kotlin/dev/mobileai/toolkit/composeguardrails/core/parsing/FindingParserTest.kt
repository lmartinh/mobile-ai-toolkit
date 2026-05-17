package dev.mobileai.toolkit.composeguardrails.core.parsing

import dev.mobileai.toolkit.report.FindingSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FindingParserTest {
    private val parser = FindingParser()

    @Test
    fun `parse returns findings from valid response`() {
        val raw = """
            {
              "findings": [
                {
                  "severity": "warning",
                  "rule_id": "compose.state-hoisting",
                  "title": "State should be hoisted",
                  "file_path": "LoginScreen.kt",
                  "explanation": "State is tightly coupled to UI",
                  "suggestion": "Move state to ViewModel",
                  "code_example": "val uiState by viewModel.uiState"
                }
              ]
            }
        """.trimIndent()

        val parsed = parser.parse(raw)

        assertEquals(1, parsed.findings.size)
        assertEquals(FindingSeverity.WARNING, parsed.findings.single().severity)
        assertTrue(parsed.warnings.isEmpty())
    }

    @Test
    fun `parse defaults invalid severity to info with warning`() {
        val raw = """
            {"findings": [{
              "severity": "critical",
              "rule_id": "compose.rule",
              "title": "Bad",
              "file_path": "A.kt",
              "explanation": "x",
              "suggestion": "y"
            }]}
        """.trimIndent()

        val parsed = parser.parse(raw)

        assertEquals(1, parsed.findings.size)
        assertEquals(FindingSeverity.INFO, parsed.findings.single().severity)
        assertTrue(parsed.warnings.any { it.contains("invalid severity") })
    }

    @Test
    fun `parse skips invalid finding entries and keeps valid ones`() {
        val raw = """
            {
              "findings": [
                {
                  "severity": "warning",
                  "rule_id": "compose.valid",
                  "title": "Valid finding",
                  "file_path": "A.kt",
                  "explanation": "x",
                  "suggestion": "y"
                },
                {
                  "severity": "error",
                  "rule_id": "compose.invalid",
                  "title": "Missing fields"
                }
              ]
            }
        """.trimIndent()

        val parsed = parser.parse(raw)

        assertEquals(1, parsed.findings.size)
        assertEquals("compose.valid", parsed.findings.single().ruleId)
        assertTrue(parsed.warnings.any { it.contains("Skipped invalid finding") })
    }

    @Test
    fun `parse returns warning when findings array is missing`() {
        val parsed = parser.parse("{}")

        assertTrue(parsed.findings.isEmpty())
        assertTrue(parsed.warnings.any { it.contains("Missing findings array") })
    }
}
