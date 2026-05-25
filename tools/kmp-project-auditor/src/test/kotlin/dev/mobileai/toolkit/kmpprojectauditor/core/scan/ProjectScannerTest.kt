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
    fun `classifies common android ios intermediate and custom source sets`() {
        val tempDir = createTempDirectory()
        tempDir.resolve("src/commonMain/kotlin").createDirectories()
        tempDir.resolve("src/commonTest/kotlin").createDirectories()
        tempDir.resolve("src/androidMain/kotlin").createDirectories()
        tempDir.resolve("src/androidUnitTest/kotlin").createDirectories()
        tempDir.resolve("src/iosMain/kotlin").createDirectories()
        tempDir.resolve("src/iosArm64Main/kotlin").createDirectories()
        tempDir.resolve("src/appleMain/kotlin").createDirectories()
        tempDir.resolve("src/nativeTest/kotlin").createDirectories()
        tempDir.resolve("src/sharedMain/kotlin").createDirectories()
        tempDir.resolve("src/sharedTest/kotlin").createDirectories()
        tempDir.resolve("src/desktopMain/kotlin").createDirectories()
        tempDir.resolve("src/jvmMain/kotlin").createDirectories()
        tempDir.resolve("src/wasmJsMain/kotlin").createDirectories()
        tempDir.resolve("src/fixtures/kotlin").createDirectories()

        val result = scanner.scan(tempDir)

        val kindsByName = result.sourceSetSummaries.associate { it.name to it.kind }
        assertEquals(SourceSetKind.COMMON, kindsByName["commonMain"])
        assertEquals(SourceSetKind.COMMON, kindsByName["commonTest"])
        assertEquals(SourceSetKind.ANDROID, kindsByName["androidMain"])
        assertEquals(SourceSetKind.ANDROID, kindsByName["androidUnitTest"])
        assertEquals(SourceSetKind.IOS, kindsByName["iosMain"])
        assertEquals(SourceSetKind.IOS, kindsByName["iosArm64Main"])
        assertEquals(SourceSetKind.INTERMEDIATE, kindsByName["appleMain"])
        assertEquals(SourceSetKind.INTERMEDIATE, kindsByName["nativeTest"])
        assertEquals(SourceSetKind.INTERMEDIATE, kindsByName["sharedMain"])
        assertEquals(SourceSetKind.INTERMEDIATE, kindsByName["sharedTest"])
        assertEquals(SourceSetKind.INTERMEDIATE, kindsByName["desktopMain"])
        assertEquals(SourceSetKind.INTERMEDIATE, kindsByName["jvmMain"])
        assertEquals(SourceSetKind.INTERMEDIATE, kindsByName["wasmJsMain"])
        assertEquals(SourceSetKind.CUSTOM, kindsByName["fixtures"])
    }

    @Test
    fun `detects capabilities from discovered source sets and gradle target declarations`() {
        val tempDir = createTempDirectory()
        tempDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                kotlin("multiplatform")
                id("com.android.library")
            }

            kotlin {
                androidTarget()
                iosArm64()
            }
            """.trimIndent()
        )
        tempDir.resolve("src/commonMain/kotlin").createDirectories()
        tempDir.resolve("src/commonTest/kotlin").createDirectories()
        tempDir.resolve("src/androidMain/kotlin").createDirectories()
        tempDir.resolve("src/iosMain/kotlin").createDirectories()
        tempDir.resolve("src/appleMain/kotlin").createDirectories()
        tempDir.resolve("src/fixtures/kotlin").createDirectories()

        val result = scanner.scan(tempDir)

        assertTrue(result.hasCommonMain)
        assertTrue(result.hasCommonTest)
        assertTrue(result.hasAndroidSourceSet)
        assertTrue(result.hasIosSourceSet)
        assertTrue(result.hasAndroidTarget)
        assertTrue(result.hasIosTarget)
        assertTrue(result.hasIntermediateSourceSets)
        assertTrue(result.hasCustomSourceSets)
    }

    @Test
    fun `capabilities and notes reflect missing source sets and target mismatches`() {
        val tempDir = createTempDirectory()
        tempDir.resolve("build.gradle.kts").writeText(
            """
            plugins { id("org.jetbrains.kotlin.multiplatform") }
            kotlin { ios() }
            """.trimIndent()
        )
        tempDir.resolve("src/commonMain/kotlin").createDirectories()

        val result = scanner.scan(tempDir)

        assertTrue(result.hasCommonMain)
        assertFalse(result.hasCommonTest)
        assertFalse(result.hasAndroidSourceSet)
        assertFalse(result.hasIosSourceSet)
        assertFalse(result.hasAndroidTarget)
        assertTrue(result.hasIosTarget)
        assertTrue(result.layoutNotes.contains("commonTest source set was not found."))
        assertTrue(result.layoutNotes.contains("iOS target detected but no iOS source set directory was found."))
    }

    @Test
    fun `detects gradle heuristics from supported pattern variants`() {
        val tempDir = createTempDirectory()
        tempDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.multiplatform")
                kotlin("android")
            }
            kotlin {
                iosX64()
                iosSimulatorArm64()
            }
            """.trimIndent()
        )
        tempDir.resolve("module-a/build.gradle").apply {
            parent.createDirectories()
            writeText(
                """
                plugins { id("com.android.application") }
                kotlin { android() }
                """.trimIndent()
            )
        }

        val result = scanner.scan(tempDir)

        assertTrue(result.gradleHeuristics.hasKmpPlugin)
        assertTrue(result.gradleHeuristics.hasAndroidTarget)
        assertTrue(result.gradleHeuristics.hasIosTarget)
        assertContentEquals(
            listOf("build.gradle.kts"),
            result.gradleHeuristics.kmpPluginFiles
        )
        assertContentEquals(
            listOf("build.gradle.kts", "module-a/build.gradle"),
            result.gradleHeuristics.androidTargetFiles
        )
        assertContentEquals(
            listOf("build.gradle.kts"),
            result.gradleHeuristics.iosTargetFiles
        )
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
