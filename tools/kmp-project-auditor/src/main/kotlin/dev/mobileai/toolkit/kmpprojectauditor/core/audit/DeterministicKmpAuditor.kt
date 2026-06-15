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
        findings += detectJvmOnlyApiInCommonMain(scanResult)
        findings += detectExpectActualMismatches(scanResult)
        findings += detectComposeCommonLocalContextUsage(scanResult)
        findings += detectComposeResourcesAndroidResInCommon(scanResult)
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

    private fun detectJvmOnlyApiInCommonMain(scanResult: ProjectScanResult): List<KmpFinding> {
        val findings = mutableListOf<KmpFinding>()

        visitCommonMainKotlinFiles(scanResult) { relativeFile, lineNumber, trimmed ->
            val importMatch = jvmOnlyImportPattern(trimmed)
            val usageMatch = jvmOnlyUsagePattern(trimmed)

            when {
                importMatch != null -> {
                    findings += buildFinding(
                        ruleId = RULE_COMMON_JVM_ONLY_API,
                        title = "JVM-only API import in commonMain",
                        file = relativeFile,
                        lineNumber = lineNumber,
                        explanation = "commonMain should avoid JVM-only APIs when the module targets iOS/Native.",
                        suggestion = "Move the implementation behind expect/actual, inject an abstraction, or use a multiplatform library.",
                        evidence = importMatch
                    )
                }

                usageMatch != null -> {
                    findings += buildFinding(
                        ruleId = RULE_COMMON_JVM_ONLY_API,
                        title = "JVM-only API usage in commonMain",
                        file = relativeFile,
                        lineNumber = lineNumber,
                        explanation = "commonMain should avoid JVM-only APIs when the module targets iOS/Native.",
                        suggestion = "Move the implementation behind expect/actual, inject an abstraction, or use a multiplatform library.",
                        evidence = usageMatch
                    )
                }
            }
        }

        return findings
    }

    private fun detectExpectActualMismatches(scanResult: ProjectScanResult): List<KmpFinding> {
        val declarations = collectExpectActualDeclarations(scanResult)
        val expectDeclarations = declarations.filter { it.declarationKeyword == "expect" && it.sourceSetName == "commonMain" }
        val actualDeclarations = declarations.filter { it.declarationKeyword == "actual" }
            .filterNot { it.sourceSetName == "commonMain" }
            .filterNot { it.sourceSetName.contains("test", ignoreCase = true) }

        val actualSignatures = actualDeclarations.mapTo(linkedSetOf(), DeclarationOccurrence::signature)
        val expectSignatures = expectDeclarations.mapTo(linkedSetOf(), DeclarationOccurrence::signature)

        val findings = mutableListOf<KmpFinding>()

        expectDeclarations
            .filterNot { it.signature in actualSignatures }
            .forEach { expectDeclaration ->
                findings += buildFinding(
                    ruleId = RULE_EXPECT_ACTUAL_MISSING_ACTUAL,
                    title = "Expect declaration without matching actual",
                    file = expectDeclaration.file,
                    lineNumber = expectDeclaration.lineNumber,
                    explanation = "An expect ${expectDeclaration.kind} named ${expectDeclaration.name} was found in commonMain, but no matching actual declaration was detected in platform source sets.",
                    suggestion = "Add a matching actual declaration in the relevant platform source set or remove the expect declaration if it is not needed.",
                    evidence = "${expectDeclaration.declarationKeyword} ${expectDeclaration.kind} ${expectDeclaration.name}"
                )
            }

        actualDeclarations
            .filterNot { it.signature in expectSignatures }
            .forEach { actualDeclaration ->
                findings += buildFinding(
                    ruleId = RULE_EXPECT_ACTUAL_ORPHAN_ACTUAL,
                    title = "Actual declaration without matching expect",
                    file = actualDeclaration.file,
                    lineNumber = actualDeclaration.lineNumber,
                    explanation = "An actual ${actualDeclaration.kind} named ${actualDeclaration.name} was found in a platform source set, but no matching expect declaration was detected in commonMain.",
                    suggestion = "Add a matching expect declaration in commonMain or remove the actual declaration if it is not needed.",
                    evidence = "${actualDeclaration.declarationKeyword} ${actualDeclaration.kind} ${actualDeclaration.name}"
                )
            }

        return findings
    }

    private fun detectComposeCommonLocalContextUsage(scanResult: ProjectScanResult): List<KmpFinding> {
        val findings = mutableListOf<KmpFinding>()

        visitCommonMainKotlinFiles(scanResult) { relativeFile, lineNumber, trimmed ->
            val evidence = when {
                trimmed.contains("import androidx.compose.ui.platform.LocalContext") ->
                    "import androidx.compose.ui.platform.LocalContext"
                trimmed.contains("LocalContext.current") ->
                    "LocalContext.current"
                trimmed.contains("import androidx.compose.ui.platform.LocalView") ->
                    "import androidx.compose.ui.platform.LocalView"
                trimmed.contains("LocalView.current") ->
                    "LocalView.current"
                trimmed.contains("import androidx.compose.ui.platform.LocalLifecycleOwner") ->
                    "import androidx.compose.ui.platform.LocalLifecycleOwner"
                trimmed.contains("LocalLifecycleOwner.current") ->
                    "LocalLifecycleOwner.current"
                trimmed.contains("import androidx.activity.compose.BackHandler") ->
                    "import androidx.activity.compose.BackHandler"
                else -> null
            }

            if (evidence != null) {
                findings += buildFinding(
                    ruleId = RULE_COMPOSE_COMMON_LOCAL_CONTEXT_USAGE,
                    title = "Android-specific Compose platform access in commonMain",
                    file = relativeFile,
                    lineNumber = lineNumber,
                    explanation = "commonMain contains Android-specific Compose/platform usage that should not be relied on as shared code.",
                    suggestion = "Move platform behavior behind expect/actual, pass dependencies from platform code, or use a multiplatform-safe abstraction.",
                    evidence = evidence
                )
            }
        }

        return findings
    }

    private fun detectComposeResourcesAndroidResInCommon(scanResult: ProjectScanResult): List<KmpFinding> {
        val findings = mutableListOf<KmpFinding>()

        visitCommonMainKotlinFiles(scanResult) { relativeFile, lineNumber, trimmed ->
            val evidence = when {
                trimmed.contains("import android.content.res.") ->
                    trimmed.substringAfter("import ").trim()
                trimmed.contains("LocalContext.current.resources") ->
                    "LocalContext.current.resources"
                trimmed.contains(".resources.getString(") ->
                    ".resources.getString("
                trimmed.contains(".resources.getDrawable(") ->
                    ".resources.getDrawable("
                trimmed.contains(".resources.getColor(") ->
                    ".resources.getColor("
                ANDROID_RESOURCE_REFERENCE_PATTERNS.firstOrNull { it.containsMatchIn(trimmed) } != null ->
                    ANDROID_RESOURCE_REFERENCE_PATTERNS.first { it.containsMatchIn(trimmed) }.find(trimmed)?.value
                else -> null
            }

            if (evidence != null) {
                findings += buildFinding(
                    ruleId = RULE_COMPOSE_RESOURCES_ANDROID_RES_IN_COMMON,
                    title = "Android resource access in commonMain",
                    file = relativeFile,
                    lineNumber = lineNumber,
                    explanation = "Android resource access was detected in common code.",
                    suggestion = "Use Compose Multiplatform resources, pass platform-specific values from platform source sets, or move the access behind expect/actual.",
                    evidence = evidence
                )
            }
        }

        return findings
    }

    private fun visitCommonMainKotlinFiles(
        scanResult: ProjectScanResult,
        onLine: (relativeFile: String, lineNumber: Int, trimmedLine: String) -> Unit
    ) {
        scanSourceSetKotlinFiles(scanResult, includeSourceSet = { sourceSetName, _, _ ->
            sourceSetName == "commonMain"
        }) { _, relativeFile, file ->
            file.toFile().readLines().forEachIndexed { index, line ->
                val trimmed = normalizeLine(line)
                if (trimmed != null) {
                    onLine(relativeFile, index + 1, trimmed)
                }
            }
        }
    }

    private fun scanSourceSetKotlinFiles(
        scanResult: ProjectScanResult,
        includeSourceSet: (String, String, Path) -> Boolean,
        onFile: (sourceSetName: String, relativeFile: String, file: Path) -> Unit
    ) {
        scanResult.kotlinSourceRoots.forEach { root ->
            val sourceSetName = sourceSetNameFromRoot(root) ?: return@forEach
            val rootPath = scanResult.analyzedPath.resolve(root)
            if (!Files.exists(rootPath)) return@forEach

            visitKotlinFiles(rootPath) { kotlinFile ->
                val relativeFile = toDisplayRelativePath(scanResult.analyzedPath, kotlinFile)
                if (includeSourceSet(sourceSetName, relativeFile, kotlinFile)) {
                    onFile(sourceSetName, relativeFile, kotlinFile)
                }
            }
        }
    }

    private fun collectExpectActualDeclarations(scanResult: ProjectScanResult): List<DeclarationOccurrence> {
        val declarations = mutableListOf<DeclarationOccurrence>()

        scanSourceSetKotlinFiles(scanResult, includeSourceSet = { _, _, _ -> true }) { sourceSetName, relativeFile, file ->
            val fileText = file.toFile().readLines()
            fileText.forEachIndexed { index, line ->
                val trimmed = normalizeLine(line) ?: return@forEachIndexed
                parseExpectActualDeclaration(trimmed)?.let { declaration ->
                    declarations += declaration.copy(
                        file = relativeFile,
                        lineNumber = index + 1,
                        sourceSetName = sourceSetName
                    )
                }
            }
        }

        return declarations
    }

    private fun parseExpectActualDeclaration(trimmedLine: String): DeclarationOccurrence? {
        val match = EXPECT_ACTUAL_DECLARATION_PATTERN.find(trimmedLine) ?: return null
        val declarationKeyword = match.groupValues[1].lowercase()
        val kind = match.groupValues[2].lowercase()
        val name = match.groupValues[3]
        return DeclarationOccurrence(
            declarationKeyword = declarationKeyword,
            kind = kind,
            name = name,
            file = "",
            lineNumber = 0,
            sourceSetName = ""
        )
    }

    private fun buildFinding(
        ruleId: String,
        title: String,
        file: String,
        lineNumber: Int?,
        explanation: String,
        suggestion: String,
        evidence: String? = null
    ): KmpFinding {
        return KmpFinding(
            ruleId = ruleId,
            severity = KmpFindingSeverity.WARNING,
            title = title,
            file = file,
            explanation = explanation,
            suggestion = suggestion,
            evidence = evidence,
            lineNumber = lineNumber
        )
    }

    private fun sourceSetNameFromRoot(root: String): String? {
        return Path.of(root).parent?.fileName?.toString()
    }

    private fun normalizeLine(line: String): String? {
        val withoutComment = line.substringBefore("//")
        val trimmed = withoutComment.trim()
        if (trimmed.isBlank()) return null
        if (trimmed.startsWith("/*") || trimmed.startsWith("*") || trimmed.startsWith("*/")) return null
        return trimmed
    }

    private fun jvmOnlyImportPattern(trimmedLine: String): String? {
        if (!trimmedLine.startsWith("import ")) {
            return null
        }

        val importedName = trimmedLine.removePrefix("import ").trim()
        return when {
            importedName.startsWith("java.")
            || importedName.startsWith("javax.")
            || importedName.startsWith("kotlin.io.path.")
            || importedName.startsWith("sun.") -> trimmedLine

            else -> null
        }
    }

    private fun jvmOnlyUsagePattern(trimmedLine: String): String? {
        return JVM_ONLY_USAGE_PATTERNS.firstOrNull { it.containsMatchIn(trimmedLine) }?.find(trimmedLine)?.value
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
        if (!scanResult.hasKmpContext) {
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

    private data class DeclarationOccurrence(
        val declarationKeyword: String,
        val kind: String,
        val name: String,
        val file: String,
        val lineNumber: Int,
        val sourceSetName: String
    ) {
        val signature: String = "$kind|$name"
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
        const val RULE_COMMON_JVM_ONLY_API = "kmp.common.jvm-only-api"
        const val RULE_MISSING_COMMON_TEST = "kmp.tests.missing-common-test"
        const val RULE_ANDROID_TARGET_WITHOUT_SOURCE_SET = "kmp.source-sets.android-target-without-source-set"
        const val RULE_IOS_TARGET_WITHOUT_SOURCE_SET = "kmp.source-sets.ios-target-without-source-set"
        const val RULE_ANDROID_SOURCE_SET_WITHOUT_TARGET = "kmp.source-sets.android-source-set-without-target"
        const val RULE_IOS_SOURCE_SET_WITHOUT_TARGET = "kmp.source-sets.ios-source-set-without-target"
        const val RULE_EXPECT_ACTUAL_MISSING_ACTUAL = "kmp.expect-actual.missing-actual"
        const val RULE_EXPECT_ACTUAL_ORPHAN_ACTUAL = "kmp.expect-actual.orphan-actual"
        const val RULE_COMPOSE_COMMON_LOCAL_CONTEXT_USAGE = "kmp.compose.common-localcontext-usage"
        const val RULE_COMPOSE_RESOURCES_ANDROID_RES_IN_COMMON = "kmp.compose.resources.android-res-in-common"
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

        val EXPECT_ACTUAL_DECLARATION_PATTERN = Regex(
            """\b(expect|actual)\b.*?\b(class|object|interface|fun|val|var)\s+([A-Za-z_][A-Za-z0-9_]*)\b"""
        )

        val JVM_ONLY_USAGE_PATTERNS = listOf(
            Regex("""\bjava\.(?:time|io|util)\."""),
            Regex("""\bjavax\."""),
            Regex("""\bkotlin\.io\.path\."""),
            Regex("""\bsun\.""")
        )

        val ANDROID_RESOURCE_REFERENCE_PATTERNS = listOf(
            Regex("""\bR\.(string|drawable|color|plurals|font|raw|array)\.""")
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
