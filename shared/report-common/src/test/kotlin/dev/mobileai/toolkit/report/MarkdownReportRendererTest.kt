package dev.mobileai.toolkit.report

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkdownReportRendererTest {
    private val renderer = MarkdownReportRenderer()

    @Test
    fun `render outputs no findings message`() {
        val markdown = renderer.render(
            ReportInput(
                analyzedPath = "/repo/sample",
                kotlinFilesScanned = 3,
                findings = emptyList()
            )
        )

        assertTrue(markdown.contains("# Compose Guardrails Report"))
        assertTrue(markdown.contains("No guardrail violations detected."))
        assertTrue(markdown.contains("- Total findings: 0"))
    }

    @Test
    fun `render outputs deterministic markdown for findings`() {
        val markdown = renderer.render(
            ReportInput(
                analyzedPath = "/repo/sample",
                kotlinFilesScanned = 2,
                parserWarnings = listOf("Skipped invalid finding at index 3"),
                findings = listOf(
                    GuardrailFinding(
                        severity = FindingSeverity.WARNING,
                        ruleId = "compose.state-hoisting",
                        title = "State should be hoisted",
                        filePath = "b.kt",
                        explanation = "State is local.",
                        suggestion = "Move to ViewModel.",
                        codeExample = null
                    ),
                    GuardrailFinding(
                        severity = FindingSeverity.ERROR,
                        ruleId = "compose.no-business-logic-in-composables",
                        title = "Business logic inside Composable",
                        filePath = "a.kt",
                        explanation = "Domain logic in UI layer.",
                        suggestion = "Extract to use case.",
                        codeExample = "viewModel.validate()"
                    )
                )
            )
        )

        assertTrue(markdown.contains("## Parser Warnings"))
        assertTrue(markdown.contains("### File: a.kt"))
        assertTrue(markdown.contains("#### Error: Business logic inside Composable"))
        assertTrue(markdown.contains("```kotlin\nviewModel.validate()\n```"))
        assertTrue(markdown.contains("### File: b.kt"))
        assertTrue(markdown.contains("#### Warning: State should be hoisted"))

        val expectedPrefix = """
            # Compose Guardrails Report
            
            ## Summary
            - Analyzed path: `/repo/sample`
            - Kotlin files scanned: 2
            - Total findings: 2
        """.trimIndent()
        assertTrue(markdown.startsWith(expectedPrefix))

        assertEquals(markdown, renderer.render(
            ReportInput(
                analyzedPath = "/repo/sample",
                kotlinFilesScanned = 2,
                parserWarnings = listOf("Skipped invalid finding at index 3"),
                findings = listOf(
                    GuardrailFinding(FindingSeverity.WARNING, "compose.state-hoisting", "State should be hoisted", "b.kt", "State is local.", "Move to ViewModel.", null),
                    GuardrailFinding(FindingSeverity.ERROR, "compose.no-business-logic-in-composables", "Business logic inside Composable", "a.kt", "Domain logic in UI layer.", "Extract to use case.", "viewModel.validate()")
                )
            )
        ))
    }
}
