package dev.mobileai.toolkit.composeguardrails.core

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KotlinFileScannerTest {
    private val scanner = KotlinFileScanner()

    @Test
    fun `scan returns single file when input is kotlin file`() {
        val directory = Files.createTempDirectory("scanner-test-single")
        val kotlinFile = directory.resolve("Sample.kt")
        Files.writeString(kotlinFile, "class Sample")

        val result = scanner.scan(kotlinFile)

        assertEquals(listOf(kotlinFile), result)
    }

    @Test
    fun `scan throws when input file is not kotlin`() {
        val directory = Files.createTempDirectory("scanner-test-invalid")
        val textFile = directory.resolve("Sample.txt")
        Files.writeString(textFile, "hello")

        assertFailsWith<IllegalArgumentException> {
            scanner.scan(textFile)
        }
    }

    @Test
    fun `scan recursively returns kotlin files from directory`() {
        val root = Files.createTempDirectory("scanner-test-dir")
        val nested = Files.createDirectories(root.resolve("nested"))

        val topLevelKt = root.resolve("A.kt")
        val nestedKt = nested.resolve("B.kt")
        val ignored = nested.resolve("C.txt")

        Files.writeString(topLevelKt, "class A")
        Files.writeString(nestedKt, "class B")
        Files.writeString(ignored, "not kotlin")

        val result = scanner.scan(root)

        assertEquals(listOf(topLevelKt, nestedKt), result)
    }

    @Test
    fun `scan throws when path does not exist`() {
        val missingPath = Path.of("/path/that/does/not/exist-${System.nanoTime()}")

        assertFailsWith<IllegalArgumentException> {
            scanner.scan(missingPath)
        }
    }
}
