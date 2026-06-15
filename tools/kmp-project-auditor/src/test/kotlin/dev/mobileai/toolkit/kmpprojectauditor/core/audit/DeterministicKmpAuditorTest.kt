package dev.mobileai.toolkit.kmpprojectauditor.core.audit

import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanner
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeterministicKmpAuditorTest {
    private val scanner = ProjectScanner()
    private val auditor = DeterministicKmpAuditor()

    @Test
    fun `compose multiplatform imports in commonMain are allowed while android only imports are flagged`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }")
        project.resolve("src/commonMain/kotlin/Sample.kt").apply {
            parent.createDirectories()
            writeText(
                """
                import android.content.Context
                import androidx.activity.ComponentActivity
                import androidx.appcompat.app.AppCompatActivity
                import androidx.core.content.ContextCompat
                import androidx.lifecycle.ViewModel
                import androidx.compose.runtime.Composable
                import androidx.compose.foundation.layout.Column
                import androidx.compose.material3.MaterialTheme
                import androidx.compose.ui.Modifier
                """.trimIndent()
            )
        }

        val findings = auditor.audit(scanner.scan(project))

        val ruleFindings = findings.filter { it.ruleId == "kmp.common.no-android-api" }
        assertEquals(5, ruleFindings.size)
        assertTrue(ruleFindings.all { it.file == "src/commonMain/kotlin/Sample.kt" })
        assertTrue(ruleFindings.any { it.evidence == "import android.content.Context" })
        assertTrue(ruleFindings.any { it.evidence == "import androidx.activity.ComponentActivity" })
        assertTrue(ruleFindings.any { it.evidence == "import androidx.appcompat.app.AppCompatActivity" })
        assertTrue(ruleFindings.any { it.evidence == "import androidx.core.content.ContextCompat" })
        assertTrue(ruleFindings.any { it.evidence == "import androidx.lifecycle.ViewModel" })
        assertTrue(findings.none { it.evidence == "import androidx.compose.runtime.Composable" })
        assertTrue(findings.none { it.evidence == "import androidx.compose.foundation.layout.Column" })
        assertTrue(findings.none { it.evidence == "import androidx.compose.material3.MaterialTheme" })
        assertTrue(findings.none { it.evidence == "import androidx.compose.ui.Modifier" })
    }

    @Test
    fun `platform and cinterop imports in commonMain create findings`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }")
        project.resolve("src/commonMain/kotlin/Sample.kt").apply {
            parent.createDirectories()
            writeText(
                """
                import platform.Foundation.NSString
                import kotlinx.cinterop.CPointer
                """.trimIndent()
            )
        }

        val findings = auditor.audit(scanner.scan(project))

        val ruleFindings = findings.filter { it.ruleId == "kmp.common.no-ios-api" }
        assertEquals(2, ruleFindings.size)
    }

    @Test
    fun `kotlin stdlib imports and platform imports outside commonMain do not create commonMain findings`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }\nkotlin { ios() }")
        project.resolve("src/commonMain/kotlin/Shared.kt").apply {
            parent.createDirectories()
            writeText("import kotlin.collections.List")
        }
        project.resolve("src/iosMain/kotlin/Ios.kt").apply {
            parent.createDirectories()
            writeText("import platform.Foundation.NSString")
        }

        val findings = auditor.audit(scanner.scan(project))

        assertTrue(findings.none { it.ruleId == "kmp.common.no-android-api" })
        assertTrue(findings.none { it.ruleId == "kmp.common.no-ios-api" })
    }

    @Test
    fun `commonMain without commonTest creates missing common test finding`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }")
        project.resolve("src/commonMain/kotlin/Shared.kt").apply {
            parent.createDirectories()
            writeText("class Shared")
        }

        val findings = auditor.audit(scanner.scan(project))

        assertTrue(findings.any { it.ruleId == "kmp.tests.missing-common-test" })
    }

    @Test
    fun `commonMain with commonTest does not create missing common test finding`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }")
        project.resolve("src/commonMain/kotlin/Shared.kt").apply {
            parent.createDirectories()
            writeText("class Shared")
        }
        project.resolve("src/commonTest/kotlin/SharedTest.kt").apply {
            parent.createDirectories()
            writeText("class SharedTest")
        }

        val findings = auditor.audit(scanner.scan(project))

        assertTrue(findings.none { it.ruleId == "kmp.tests.missing-common-test" })
    }

    @Test
    fun `target source-set mismatch findings are created`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText(
            """
            plugins { id("org.jetbrains.kotlin.multiplatform") }
            kotlin {
                androidTarget()
                ios()
            }
            """.trimIndent()
        )
        project.resolve("src/commonMain/kotlin/Shared.kt").apply {
            parent.createDirectories()
            writeText("class Shared")
        }

        val findings = auditor.audit(scanner.scan(project))

        assertTrue(findings.any { it.ruleId == "kmp.source-sets.android-target-without-source-set" })
        assertTrue(findings.any { it.ruleId == "kmp.source-sets.ios-target-without-source-set" })
    }

    @Test
    fun `source-set without target findings are created in kmp context`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText(
            """
            plugins { id("org.jetbrains.kotlin.multiplatform") }
            """.trimIndent()
        )
        project.resolve("src/commonMain/kotlin/Shared.kt").apply {
            parent.createDirectories()
            writeText("class Shared")
        }
        project.resolve("src/commonTest/kotlin/SharedTest.kt").apply {
            parent.createDirectories()
            writeText("class SharedTest")
        }
        project.resolve("src/androidMain/kotlin/AndroidCode.kt").apply {
            parent.createDirectories()
            writeText("class AndroidCode")
        }
        project.resolve("src/iosMain/kotlin/IosCode.kt").apply {
            parent.createDirectories()
            writeText("class IosCode")
        }

        val findings = auditor.audit(scanner.scan(project))

        assertTrue(findings.any { it.ruleId == "kmp.source-sets.android-source-set-without-target" })
        assertTrue(findings.any { it.ruleId == "kmp.source-sets.ios-source-set-without-target" })
    }

    @Test
    fun `source-set without target findings still appear when the plugin is not explicitly detected`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { }")
        project.resolve("src/commonMain/kotlin/Shared.kt").apply {
            parent.createDirectories()
            writeText("class Shared")
        }
        project.resolve("src/commonTest/kotlin/SharedTest.kt").apply {
            parent.createDirectories()
            writeText("class SharedTest")
        }
        project.resolve("src/androidMain/kotlin/AndroidCode.kt").apply {
            parent.createDirectories()
            writeText("class AndroidCode")
        }
        project.resolve("src/iosMain/kotlin/IosCode.kt").apply {
            parent.createDirectories()
            writeText("class IosCode")
        }

        val findings = auditor.audit(scanner.scan(project))

        assertTrue(findings.any { it.ruleId == "kmp.source-sets.android-source-set-without-target" })
        assertTrue(findings.any { it.ruleId == "kmp.source-sets.ios-source-set-without-target" })
    }

    @Test
    fun `flags jvm only api imports in commonMain and ignores androidMain`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }")
        project.resolve("src/commonMain/kotlin/JvmOnly.kt").apply {
            parent.createDirectories()
            writeText(
                """
                import java.time.LocalDate
                import javax.crypto.Cipher
                // java.time.LocalDate should not be counted from comments
                val today = java.time.LocalDate.now()
                """.trimIndent()
            )
        }
        project.resolve("src/androidMain/kotlin/JvmOnlyAndroid.kt").apply {
            parent.createDirectories()
            writeText(
                """
                import java.time.LocalDate
                import javax.crypto.Cipher
                """.trimIndent()
            )
        }

        val findings = auditor.audit(scanner.scan(project))
            .filter { it.ruleId == "kmp.common.jvm-only-api" }

        assertEquals(3, findings.size)
        assertTrue(findings.all { it.file == "src/commonMain/kotlin/JvmOnly.kt" })
        assertTrue(findings.any { it.evidence == "import java.time.LocalDate" })
        assertTrue(findings.any { it.evidence == "import javax.crypto.Cipher" })
        assertTrue(findings.any { it.evidence == "java.time." })
    }

    @Test
    fun `flags missing actual declarations for expect class and expect fun`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }")
        project.resolve("src/commonMain/kotlin/Platform.kt").apply {
            parent.createDirectories()
            writeText(
                """
                expect class PlatformInfo
                expect fun platformName(): String
                """.trimIndent()
            )
        }

        val findings = auditor.audit(scanner.scan(project))

        val missingActuals = findings.filter { it.ruleId == "kmp.expect-actual.missing-actual" }
        assertEquals(2, missingActuals.size)
        assertTrue(missingActuals.any { it.file == "src/commonMain/kotlin/Platform.kt" && it.lineNumber == 1 })
        assertTrue(missingActuals.any { it.file == "src/commonMain/kotlin/Platform.kt" && it.lineNumber == 2 })
    }

    @Test
    fun `does not flag expect declarations when matching actual declarations exist`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }")
        project.resolve("src/commonMain/kotlin/Platform.kt").apply {
            parent.createDirectories()
            writeText(
                """
                expect class PlatformInfo
                expect fun platformName(): String
                """.trimIndent()
            )
        }
        project.resolve("src/androidMain/kotlin/PlatformAndroid.kt").apply {
            parent.createDirectories()
            writeText(
                """
                actual class PlatformInfo
                actual fun platformName(): String = "android"
                """.trimIndent()
            )
        }

        val findings = auditor.audit(scanner.scan(project))

        assertTrue(findings.none { it.ruleId == "kmp.expect-actual.missing-actual" })
        assertTrue(findings.none { it.ruleId == "kmp.expect-actual.orphan-actual" })
    }

    @Test
    fun `flags orphan actual declarations in platform source sets and ignores commonMain`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }")
        project.resolve("src/commonMain/kotlin/Common.kt").apply {
            parent.createDirectories()
            writeText(
                """
                actual class PlatformInfo
                """.trimIndent()
            )
        }
        project.resolve("src/iosMain/kotlin/PlatformIos.kt").apply {
            parent.createDirectories()
            writeText(
                """
                actual class PlatformInfo
                """.trimIndent()
            )
        }

        val findings = auditor.audit(scanner.scan(project))

        val orphanActuals = findings.filter { it.ruleId == "kmp.expect-actual.orphan-actual" }
        assertEquals(1, orphanActuals.size)
        assertEquals("src/iosMain/kotlin/PlatformIos.kt", orphanActuals.single().file)
        assertEquals(1, orphanActuals.single().lineNumber)
    }

    @Test
    fun `flags android specific compose local context usage and ignores regular compose imports`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }")
        project.resolve("src/commonMain/kotlin/ComposeUsage.kt").apply {
            parent.createDirectories()
            writeText(
                """
                import androidx.compose.ui.platform.LocalContext
                import androidx.compose.runtime.Composable
                val context = LocalContext.current
                """.trimIndent()
            )
        }

        val findings = auditor.audit(scanner.scan(project))

        val composeFindings = findings.filter { it.ruleId == "kmp.compose.common-localcontext-usage" }
        assertEquals(2, composeFindings.size)
        assertTrue(composeFindings.any { it.evidence == "import androidx.compose.ui.platform.LocalContext" })
        assertTrue(composeFindings.any { it.evidence == "LocalContext.current" })
        assertTrue(findings.none { it.evidence == "import androidx.compose.runtime.Composable" })
    }

    @Test
    fun `flags android resource access in commonMain and ignores compose multiplatform resources`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }")
        project.resolve("src/commonMain/kotlin/ResourcesUsage.kt").apply {
            parent.createDirectories()
            writeText(
                """
                import android.content.res.Resources
                import generated.resources.Res
                val a = R.string.app_name
                val b = LocalContext.current.resources.getString(R.string.app_name)
                val c = Res.string.app_name
                """.trimIndent()
            )
        }

        val findings = auditor.audit(scanner.scan(project))

        val resourceFindings = findings.filter { it.ruleId == "kmp.compose.resources.android-res-in-common" }
        assertTrue(resourceFindings.size >= 2)
        assertTrue(resourceFindings.any { it.evidence == "R.string." })
        assertTrue(resourceFindings.any { it.evidence == "LocalContext.current.resources" || it.evidence == ".resources.getString(" })
        assertTrue(findings.any { it.ruleId == "kmp.common.no-android-api" && it.evidence == "import android.content.res.Resources" })
        assertTrue(findings.none { it.evidence == "Res.string.app_name" })
        assertTrue(findings.none { it.evidence == "import generated.resources.Res" })
    }

    @Test
    fun `common dependency block with android dependency creates finding`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText(
            """
            plugins { id("org.jetbrains.kotlin.multiplatform") }
            kotlin {
                sourceSets {
                    commonMain.dependencies {
                        implementation("androidx.core:core-ktx:1.13.1")
                    }
                }
            }
            """.trimIndent()
        )
        project.resolve("src/commonMain/kotlin/Shared.kt").apply {
            parent.createDirectories()
            writeText("class Shared")
        }

        val findings = auditor.audit(scanner.scan(project))

        assertTrue(findings.any { it.ruleId == "kmp.dependencies.common-platform-leak" })
    }

    @Test
    fun `platform dependency outside common dependency block does not trigger common leak rule`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText(
            """
            plugins { id("org.jetbrains.kotlin.multiplatform") }
            androidMainImplementation("androidx.core:core-ktx:1.13.1")
            """.trimIndent()
        )
        project.resolve("src/commonMain/kotlin/Shared.kt").apply {
            parent.createDirectories()
            writeText("class Shared")
        }

        val findings = auditor.audit(scanner.scan(project))

        assertTrue(findings.none { it.ruleId == "kmp.dependencies.common-platform-leak" })
    }

    @Test
    fun `findings are deterministic and slash normalized`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.multiplatform")
                id("com.android.library")
            }
            kotlin { ios() }
            commonMainImplementation("androidx.core:core-ktx:1.13.1")
            """.trimIndent()
        )
        project.resolve("src/commonMain/kotlin/Zeta.kt").apply {
            parent.createDirectories()
            writeText("import android.content.Context")
        }
        project.resolve("src/commonMain/kotlin/Alpha.kt").apply {
            parent.createDirectories()
            writeText("import platform.Foundation.NSString")
        }

        val findings = auditor.audit(scanner.scan(project))

        assertTrue(findings.isNotEmpty())
        assertTrue(findings.all { !it.file.contains('\\') })
        assertContentEquals(findings.sortedWith(compareBy<KmpFinding> { it.severity }.thenBy { it.ruleId }.thenBy { it.file }.thenBy { it.title }), findings)
    }

    @Test
    fun `generated directories are ignored for platform import checks`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText("plugins { kotlin(\"multiplatform\") }")
        project.resolve("src/commonMain/kotlin/Shared.kt").apply {
            parent.createDirectories()
            writeText("class Shared")
        }
        project.resolve("build/generated/src/commonMain/kotlin/Fake.kt").apply {
            parent.createDirectories()
            writeText("import android.content.Context")
        }

        val findings = auditor.audit(scanner.scan(project))

        assertFalse(findings.any { it.file.contains("build/generated") })
        assertTrue(findings.none { it.ruleId == "kmp.common.no-android-api" })
    }

    @Test
    fun `clean fixture has no deterministic findings and bad fixture has expected deterministic findings`() {
        val cleanScan = scanner.scan(Path.of("examples/clean-kmp-library"))
        val badScan = scanner.scan(Path.of("examples/bad-kmp-library"))

        val cleanFindings = auditor.audit(cleanScan)
        val badFindings = auditor.audit(badScan)

        assertTrue(cleanFindings.isEmpty())
        assertTrue(badFindings.isNotEmpty())
        assertTrue(badFindings.any { it.ruleId == "kmp.common.no-android-api" })
        assertTrue(badFindings.any { it.ruleId == "kmp.common.no-ios-api" })
        assertTrue(badFindings.any { it.ruleId == "kmp.common.jvm-only-api" })
        assertTrue(badFindings.any { it.ruleId == "kmp.compose.common-localcontext-usage" })
        assertTrue(badFindings.any { it.ruleId == "kmp.compose.resources.android-res-in-common" })
        assertTrue(badFindings.any { it.ruleId == "kmp.dependencies.common-platform-leak" })
        assertTrue(badFindings.any { it.ruleId == "kmp.expect-actual.missing-actual" })
        assertTrue(badFindings.any { it.ruleId == "kmp.expect-actual.orphan-actual" })
        assertTrue(badFindings.any { it.ruleId == "kmp.source-sets.android-source-set-without-target" })
        assertTrue(badFindings.any { it.ruleId == "kmp.source-sets.ios-source-set-without-target" })
        assertTrue(badFindings.any { it.ruleId == "kmp.tests.missing-common-test" })
    }
}
