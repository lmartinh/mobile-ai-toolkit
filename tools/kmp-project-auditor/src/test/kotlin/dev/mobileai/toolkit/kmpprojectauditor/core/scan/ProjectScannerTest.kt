package dev.mobileai.toolkit.kmpprojectauditor.core.scan

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectScannerTest {
    private val scanner = ProjectScanner()

    @Test
    fun `fails when path does not exist`() {
        val missingPath = Path.of("/tmp/definitely-missing-kmp-project-auditor")

        assertFailsWith<IllegalArgumentException> {
            scanner.scan(missingPath)
        }
    }

    @Test
    fun `fails when path is not a directory`() {
        val tempDir = createTempDirectory()
        val file = tempDir.resolve("single.txt")
        file.createFile()

        assertFailsWith<IllegalArgumentException> {
            scanner.scan(file)
        }
    }

    @Test
    fun `discovers gradle files source sets and kotlin source roots deterministically`() {
        val tempDir = createTempDirectory()
        tempDir.resolve("settings.gradle.kts").writeText("rootProject.name = \"tmp\"")
        tempDir.resolve("build.gradle.kts").writeText("plugins {}")
        tempDir.resolve("module-a/build.gradle").apply {
            parent.createDirectories()
            writeText("plugins {}")
        }
        tempDir.resolve("src/iosMain/kotlin").createDirectories()
        tempDir.resolve("src/commonMain/kotlin").createDirectories()
        tempDir.resolve("src/androidMain/kotlin").createDirectories()
        tempDir.resolve("src/appleMain/kotlin").createDirectories()

        val result = scanner.scan(tempDir)

        assertContentEquals(
            listOf("build.gradle.kts", "module-a/build.gradle", "settings.gradle.kts"),
            result.gradleFiles
        )
        assertContentEquals(
            listOf("androidMain", "appleMain", "commonMain", "iosMain"),
            result.sourceSets
        )
        assertContentEquals(
            listOf(
                "src/androidMain/kotlin",
                "src/appleMain/kotlin",
                "src/commonMain/kotlin",
                "src/iosMain/kotlin"
            ),
            result.kotlinSourceRoots
        )
    }

    @Test
    fun `detects capabilities from discovered source sets`() {
        val tempDir = createTempDirectory()
        tempDir.resolve("src/commonMain/kotlin").createDirectories()
        tempDir.resolve("src/commonTest/kotlin").createDirectories()
        tempDir.resolve("src/androidMain/kotlin").createDirectories()
        tempDir.resolve("src/iosMain/kotlin").createDirectories()

        val result = scanner.scan(tempDir)

        assertTrue(result.hasCommonMain)
        assertTrue(result.hasCommonTest)
        assertTrue(result.hasAndroidSourceSet)
        assertTrue(result.hasIosSourceSet)
    }

    @Test
    fun `capabilities are false when source sets are missing`() {
        val tempDir = createTempDirectory()
        tempDir.resolve("src/commonMain/kotlin").createDirectories()

        val result = scanner.scan(tempDir)

        assertTrue(result.hasCommonMain)
        assertFalse(result.hasCommonTest)
        assertFalse(result.hasAndroidSourceSet)
        assertFalse(result.hasIosSourceSet)
    }

    @Test
    fun `ignores generated and internal directories during traversal`() {
        val tempDir = createTempDirectory()
        tempDir.resolve("build.gradle.kts").writeText("plugins {}")
        tempDir.resolve("src/commonMain/kotlin").createDirectories()
        tempDir.resolve("build/generated/src/fakeMain/kotlin").createDirectories()
        tempDir.resolve(".gradle/src/fakeGradleMain/kotlin").createDirectories()
        tempDir.resolve(".kotlin/src/fakeKotlinMain/kotlin").createDirectories()
        tempDir.resolve("out/src/fakeOutMain/kotlin").createDirectories()
        tempDir.resolve("build/generated/build.gradle.kts").apply {
            parent.createDirectories()
            writeText("plugins {}")
        }
        tempDir.resolve(".idea/build.gradle").apply {
            parent.createDirectories()
            writeText("plugins {}")
        }

        val result = scanner.scan(tempDir)

        assertContentEquals(listOf("build.gradle.kts"), result.gradleFiles)
        assertContentEquals(listOf("commonMain"), result.sourceSets)
        assertContentEquals(listOf("src/commonMain/kotlin"), result.kotlinSourceRoots)
    }

    @Test
    fun `renders discovered relative paths with forward slashes`() {
        val tempDir = createTempDirectory()
        tempDir.resolve("module-a/build.gradle.kts").apply {
            parent.createDirectories()
            writeText("plugins {}")
        }
        tempDir.resolve("src/commonMain/kotlin").createDirectories()

        val result = scanner.scan(tempDir)

        assertTrue(result.gradleFiles.all { !it.contains('\\') })
        assertTrue(result.kotlinSourceRoots.all { !it.contains('\\') })
        assertEquals("module-a/build.gradle.kts", result.gradleFiles.single())
        assertEquals("src/commonMain/kotlin", result.kotlinSourceRoots.single())
    }
}
