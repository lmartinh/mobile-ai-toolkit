package dev.mobileai.toolkit.kmpprojectauditor.core.audit

import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanResult
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.extension
import kotlin.io.path.pathString

class DeterministicKmpAuditor {
    fun audit(scanResult: ProjectScanResult): List<KmpFinding> {
        val findings = mutableListOf<KmpFinding>()

        findings += detectCommonMainPlatformImports(scanResult)
        findings += detectMissingCommonTest(scanResult)
        findings += detectTargetSourceSetMismatch(scanResult)
        findings += detectCommonDependencyPlatformLeak(scanResult)

        return findings
            .distinctBy { listOf(it.ruleId, it.file, it.evidence ?: "", it.title) }
            .sortedWith(
                compareBy<KmpFinding> { it.severity.rank }
                    .thenBy { it.ruleId }
                    .thenBy { it.file }
                    .thenBy { it.title }
            )
    }

    private fun detectCommonMainPlatformImports(scanResult: ProjectScanResult): List<KmpFinding> {
        val commonRoots = scanResult.kotlinSourceRoots.filter { it.endsWith("/commonMain/kotlin") }
        val findings = mutableListOf<KmpFinding>()

        commonRoots.forEach { root ->
            val rootPath = scanResult.analyzedPath.resolve(root)
            if (!Files.exists(rootPath)) return@forEach

            visitKotlinFiles(rootPath) { kotlinFile ->
                val relativeFile = toDisplayRelativePath(scanResult.analyzedPath, kotlinFile)
                kotlinFile.toFile().readLines().forEach { line ->
                    val trimmed = line.trim()
                    when {
                        isAndroidOnlyImport(trimmed) -> {
                            findings += KmpFinding(
                                ruleId = RULE_COMMON_NO_ANDROID_API,
                                severity = KmpFindingSeverity.WARNING,
                                title = "Android API import in commonMain",
                                file = relativeFile,
                                explanation = "commonMain should stay platform-neutral and avoid Android-specific imports.",
                                suggestion = "Move Android-specific code to androidMain or hide it behind expect/actual.",
                                evidence = trimmed
                            )
                        }

                        trimmed.startsWith("import platform.") ||
                            trimmed.startsWith("import kotlinx.cinterop.") -> {
                            findings += KmpFinding(
                                ruleId = RULE_COMMON_NO_IOS_API,
                                severity = KmpFindingSeverity.WARNING,
                                title = "iOS/Native API import in commonMain",
                                file = relativeFile,
                                explanation = "commonMain should avoid iOS/native-specific APIs.",
                                suggestion = "Move iOS/native code to iosMain/native source sets or use expect/actual.",
                                evidence = trimmed
                            )
                        }
                    }
                }
            }
        }

        return findings
    }

    private fun detectMissingCommonTest(scanResult: ProjectScanResult): List<KmpFinding> {
        if (scanResult.hasCommonMain && !scanResult.hasCommonTest) {
            return listOf(
                KmpFinding(
                    ruleId = RULE_MISSING_COMMON_TEST,
                    severity = KmpFindingSeverity.INFO,
                    title = "Missing commonTest source set",
                    file = "<project>",
                    explanation = "Shared code exists in commonMain but commonTest was not found.",
                    suggestion = "Add commonTest to cover shared business logic with multiplatform tests."
                )
            )
        }
        return emptyList()
    }

    private fun detectTargetSourceSetMismatch(scanResult: ProjectScanResult): List<KmpFinding> {
        if (!scanResult.gradleHeuristics.hasKmpPlugin) {
            return emptyList()
        }

        val findings = mutableListOf<KmpFinding>()

        if (scanResult.hasAndroidTarget && !scanResult.hasAndroidSourceSet) {
            findings += KmpFinding(
                ruleId = RULE_ANDROID_TARGET_WITHOUT_SOURCE_SET,
                severity = KmpFindingSeverity.WARNING,
                title = "Android target without Android source set",
                file = "<project>",
                explanation = "Android target declarations were detected, but no Android source set directory was found.",
                suggestion = "Add androidMain/androidTest source sets or remove Android target declarations.",
                evidence = "Detected in: ${scanResult.gradleHeuristics.androidTargetFiles.joinToString(", ")}"
            )
        }

        if (scanResult.hasIosTarget && !scanResult.hasIosSourceSet) {
            findings += KmpFinding(
                ruleId = RULE_IOS_TARGET_WITHOUT_SOURCE_SET,
                severity = KmpFindingSeverity.WARNING,
                title = "iOS target without iOS source set",
                file = "<project>",
                explanation = "iOS target declarations were detected, but no iOS source set directory was found.",
                suggestion = "Add iosMain/iosTest source sets or remove iOS target declarations.",
                evidence = "Detected in: ${scanResult.gradleHeuristics.iosTargetFiles.joinToString(", ")}"
            )
        }

        if (!scanResult.hasAndroidTarget && scanResult.hasAndroidSourceSet) {
            findings += KmpFinding(
                ruleId = RULE_ANDROID_SOURCE_SET_WITHOUT_TARGET,
                severity = KmpFindingSeverity.WARNING,
                title = "Android source set without Android target",
                file = "<project>",
                explanation = "Android source sets were found, but no Android target declaration was detected.",
                suggestion = "Declare an Android target or remove Android-specific source sets."
            )
        }

        if (!scanResult.hasIosTarget && scanResult.hasIosSourceSet) {
            findings += KmpFinding(
                ruleId = RULE_IOS_SOURCE_SET_WITHOUT_TARGET,
                severity = KmpFindingSeverity.WARNING,
                title = "iOS source set without iOS target",
                file = "<project>",
                explanation = "iOS source sets were found, but no iOS target declaration was detected.",
                suggestion = "Declare iOS targets or remove iOS-specific source sets."
            )
        }

        return findings
    }

    private fun detectCommonDependencyPlatformLeak(scanResult: ProjectScanResult): List<KmpFinding> {
        val findings = mutableListOf<KmpFinding>()

        scanResult.gradleFiles.forEach { relativeGradleFile ->
            val gradlePath = scanResult.analyzedPath.resolve(relativeGradleFile)
            if (!Files.exists(gradlePath)) return@forEach

            val lines = gradlePath.toFile().readLines()
            var inCommonMainDependenciesBlock = false
            var blockDepth = 0

            lines.forEach { line ->
                val trimmed = line.trim()

                if (COMMON_MAIN_IMPLEMENTATION_CALL.any { it.containsMatchIn(trimmed) } &&
                    PLATFORM_DEPENDENCY_NOTATION.any { it.containsMatchIn(trimmed) }
                ) {
                    findings += KmpFinding(
                        ruleId = RULE_COMMON_DEPENDENCIES_PLATFORM_LEAK,
                        severity = KmpFindingSeverity.WARNING,
                        title = "Platform-specific dependency in common dependencies",
                        file = relativeGradleFile,
                        explanation = "commonMain dependencies should avoid Android-specific artifacts.",
                        suggestion = "Move Android dependencies to androidMain-specific configurations.",
                        evidence = trimmed
                    )
                }

                if (!inCommonMainDependenciesBlock && COMMON_DEPENDENCIES_BLOCK_START.any { it.containsMatchIn(trimmed) }) {
                    inCommonMainDependenciesBlock = true
                    blockDepth = countOpeningBraces(trimmed) - countClosingBraces(trimmed)
                    return@forEach
                }

                if (inCommonMainDependenciesBlock) {
                    if (PLATFORM_DEPENDENCY_NOTATION.any { it.containsMatchIn(trimmed) }) {
                        findings += KmpFinding(
                            ruleId = RULE_COMMON_DEPENDENCIES_PLATFORM_LEAK,
                            severity = KmpFindingSeverity.WARNING,
                            title = "Platform-specific dependency in common dependencies",
                            file = relativeGradleFile,
                            explanation = "commonMain dependencies should avoid Android-specific artifacts.",
                            suggestion = "Move Android dependencies to androidMain-specific configurations.",
                            evidence = trimmed
                        )
                    }

                    blockDepth += countOpeningBraces(trimmed)
                    blockDepth -= countClosingBraces(trimmed)
                    if (blockDepth <= 0) {
                        inCommonMainDependenciesBlock = false
                        blockDepth = 0
                    }
                }
            }
        }

        return findings
    }

    private fun countOpeningBraces(line: String): Int = line.count { it == '{' }

    private fun countClosingBraces(line: String): Int = line.count { it == '}' }

    private fun isAndroidOnlyImport(trimmedLine: String): Boolean {
        if (!trimmedLine.startsWith("import ")) {
            return false
        }

        val importedName = trimmedLine.removePrefix("import ").trim()
        if (ANDROID_COMMONMAIN_ALLOWED_PREFIXES.any { importedName.startsWith(it) }) {
            return false
        }

        return ANDROID_ONLY_IMPORT_PREFIXES.any { importedName.startsWith(it) }
    }

    private fun visitKotlinFiles(root: Path, onFile: (Path) -> Unit) {
        Files.walkFileTree(
            root,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (file.extension == "kt") {
                        onFile(file)
                    }
                    return FileVisitResult.CONTINUE
                }
            }
        )
    }

    private fun toDisplayRelativePath(root: Path, path: Path): String {
        return root.relativize(path)
            .pathString
            .replace('\\', '/')
    }

    private val KmpFindingSeverity.rank: Int
        get() = when (this) {
            KmpFindingSeverity.ERROR -> 0
            KmpFindingSeverity.WARNING -> 1
            KmpFindingSeverity.INFO -> 2
        }

    private companion object {
        const val RULE_COMMON_NO_ANDROID_API = "kmp.common.no-android-api"
        const val RULE_COMMON_NO_IOS_API = "kmp.common.no-ios-api"
        const val RULE_MISSING_COMMON_TEST = "kmp.tests.missing-common-test"
        const val RULE_ANDROID_TARGET_WITHOUT_SOURCE_SET = "kmp.source-sets.android-target-without-source-set"
        const val RULE_IOS_TARGET_WITHOUT_SOURCE_SET = "kmp.source-sets.ios-target-without-source-set"
        const val RULE_ANDROID_SOURCE_SET_WITHOUT_TARGET = "kmp.source-sets.android-source-set-without-target"
        const val RULE_IOS_SOURCE_SET_WITHOUT_TARGET = "kmp.source-sets.ios-source-set-without-target"
        const val RULE_COMMON_DEPENDENCIES_PLATFORM_LEAK = "kmp.dependencies.common-platform-leak"

        val COMMON_DEPENDENCIES_BLOCK_START = listOf(
            Regex("^commonMain\\s*\\.\\s*dependencies\\s*\\{"),
            Regex("^commonMain\\s*\\{\\s*dependencies\\s*\\{"),
            Regex("^commonMain\\s*\\(\\s*\\)\\s*\\.\\s*dependencies\\s*\\{")
        )

        val COMMON_MAIN_IMPLEMENTATION_CALL = listOf(
            Regex("^commonMainImplementation\\s*\\(")
        )

        val PLATFORM_DEPENDENCY_NOTATION = listOf(
            Regex("\"androidx\\."),
            Regex("\"com\\.android\\."),
            Regex("\"android\\.")
        )

        val ANDROID_COMMONMAIN_ALLOWED_PREFIXES = listOf(
            "androidx.compose."
        )

        val ANDROID_ONLY_IMPORT_PREFIXES = listOf(
            "android.",
            "androidx.activity.",
            "androidx.appcompat.",
            "androidx.core.",
            "androidx.fragment.",
            "androidx.lifecycle.",
            "androidx.navigation.",
            "androidx.work."
        )
    }
}
