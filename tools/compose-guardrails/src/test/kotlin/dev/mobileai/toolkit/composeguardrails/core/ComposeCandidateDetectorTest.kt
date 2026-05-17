package dev.mobileai.toolkit.composeguardrails.core

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComposeCandidateDetectorTest {
    private val detector = ComposeCandidateDetector()

    @Test
    fun `detect marks file as compose candidate when compose import exists`() {
        val file = createKotlinFile(
            "ImportOnly.kt",
            """
            package sample

            import androidx.compose.runtime.Composable

            class NotComposableYet
            """.trimIndent()
        )

        val analysis = detector.detect(listOf(file)).single()

        assertTrue(analysis.isComposeCandidate)
        assertTrue(analysis.hasComposeImport)
        assertTrue(analysis.composableFunctions.isEmpty())
    }

    @Test
    fun `detect extracts composable function with same-line annotation`() {
        val file = createKotlinFile(
            "SimpleComposable.kt",
            """
            package sample

            @Composable fun Greeting(name: String) {
                println(name)
            }
            """.trimIndent()
        )

        val analysis = detector.detect(listOf(file)).single()

        assertTrue(analysis.isComposeCandidate)
        assertEquals(1, analysis.composableFunctions.size)
        assertEquals("Greeting", analysis.composableFunctions.single().functionName)
        assertEquals(3, analysis.composableFunctions.single().line)
    }

    @Test
    fun `detect extracts composable function with multi-line annotations`() {
        val file = createKotlinFile(
            "MultilineComposable.kt",
            """
            package sample

            @Preview
            @Composable
            fun LoginScreen() {
            }
            """.trimIndent()
        )

        val analysis = detector.detect(listOf(file)).single()

        assertTrue(analysis.isComposeCandidate)
        assertEquals(1, analysis.composableFunctions.size)
        assertEquals("LoginScreen", analysis.composableFunctions.single().functionName)
        assertEquals(5, analysis.composableFunctions.single().line)
    }

    @Test
    fun `detect does not mark regular kotlin file as compose candidate`() {
        val file = createKotlinFile(
            "Plain.kt",
            """
            package sample

            fun helper() = 1 + 1
            """.trimIndent()
        )

        val analysis = detector.detect(listOf(file)).single()

        assertFalse(analysis.isComposeCandidate)
        assertFalse(analysis.hasComposeImport)
        assertTrue(analysis.composableFunctions.isEmpty())
    }

    private fun createKotlinFile(name: String, content: String) =
        Files.createTempDirectory("compose-detector-test").resolve(name).also {
            Files.writeString(it, content)
        }
}
