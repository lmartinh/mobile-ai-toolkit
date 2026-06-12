package dev.mobileai.toolkit.kmpprojectauditor.core.scan

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.text.RegexOption.IGNORE_CASE

class ProjectScanner {
    fun scan(projectPath: Path): ProjectScanResult {
        if (!projectPath.exists()) {
            throw IllegalArgumentException("Path does not exist: ${projectPath.toAbsolutePath().normalize().pathString}")
        }
        if (!projectPath.isDirectory()) {
            throw IllegalArgumentException("Path is not a directory: ${projectPath.toAbsolutePath().normalize().pathString}")
        }

        val normalizedRoot = projectPath.toAbsolutePath().normalize()

        val gradleFiles = mutableSetOf<String>()
        val sourceSetDirectories = mutableSetOf<String>()
        val kotlinSourceRoots = mutableSetOf<String>()

        Files.walkFileTree(
            normalizedRoot,
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (dir != normalizedRoot && shouldSkipDirectory(dir)) {
                        return FileVisitResult.SKIP_SUBTREE
                    }

                    if (dir.fileName.toString() == "kotlin" && dir.parent?.parent?.name == "src") {
                        kotlinSourceRoots += toDisplayRelativePath(normalizedRoot, dir)
                    }

                    if (dir.parent?.name == "src") {
                        sourceSetDirectories += dir.fileName.toString()
                    }

                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (file.name in SUPPORTED_GRADLE_FILE_NAMES) {
                        gradleFiles += toDisplayRelativePath(normalizedRoot, file)
                    }

                    return FileVisitResult.CONTINUE
                }
            }
        )

        val orderedGradleFiles = gradleFiles.toList().sorted()
        val orderedSourceSets = sourceSetDirectories.toList().sorted()
        val sourceSetSummaries = orderedSourceSets
            .map { SourceSetSummary(it, classifySourceSet(it)) }
        val gradleHeuristics = detectGradleHeuristics(normalizedRoot, orderedGradleFiles)

        return ProjectScanResult(
            analyzedPath = normalizedRoot,
            gradleFiles = orderedGradleFiles,
            sourceSets = orderedSourceSets,
            sourceSetSummaries = sourceSetSummaries,
            kotlinSourceRoots = kotlinSourceRoots.toList().sorted(),
            gradleHeuristics = gradleHeuristics,
            layoutNotes = buildLayoutNotes(sourceSetSummaries, gradleHeuristics)
        )
    }

    private fun detectGradleHeuristics(
        projectRoot: Path,
        gradleFiles: List<String>
    ): GradleHeuristicsSummary {
        val kmpPluginFiles = mutableSetOf<String>()
        val androidTargetFiles = mutableSetOf<String>()
        val iosTargetFiles = mutableSetOf<String>()

        gradleFiles.forEach { relativePath ->
            val filePath = projectRoot.resolve(relativePath)
            val content = filePath.toFile().readText()

            if (KMP_PLUGIN_PATTERNS.any { it.containsMatchIn(content) }) {
                kmpPluginFiles += relativePath
            }
            if (ANDROID_TARGET_PATTERNS.any { it.containsMatchIn(content) }) {
                androidTargetFiles += relativePath
            }
            if (IOS_TARGET_PATTERNS.any { it.containsMatchIn(content) }) {
                iosTargetFiles += relativePath
            }
        }

        return GradleHeuristicsSummary(
            hasKmpPlugin = kmpPluginFiles.isNotEmpty(),
            hasAndroidTarget = androidTargetFiles.isNotEmpty(),
            hasIosTarget = iosTargetFiles.isNotEmpty(),
            kmpPluginFiles = kmpPluginFiles.toList().sorted(),
            androidTargetFiles = androidTargetFiles.toList().sorted(),
            iosTargetFiles = iosTargetFiles.toList().sorted()
        )
    }

    private fun classifySourceSet(name: String): SourceSetKind {
        val normalized = name.lowercase()
        return when {
            name == "commonMain" || name == "commonTest" -> SourceSetKind.COMMON
            normalized.startsWith("android") -> SourceSetKind.ANDROID
            normalized.startsWith("ios") -> SourceSetKind.IOS
            normalized.startsWith("apple") || normalized.startsWith("native") -> SourceSetKind.INTERMEDIATE
            name.endsWith("Main") || name.endsWith("Test") -> SourceSetKind.INTERMEDIATE
            else -> SourceSetKind.CUSTOM
        }
    }

    private fun buildLayoutNotes(
        sourceSetSummaries: List<SourceSetSummary>,
        gradleHeuristics: GradleHeuristicsSummary
    ): List<String> {
        val hasCommonTest = sourceSetSummaries.any { it.name == "commonTest" }
        val hasAndroidSourceSet = sourceSetSummaries.any { it.kind == SourceSetKind.ANDROID }
        val hasIosSourceSet = sourceSetSummaries.any { it.kind == SourceSetKind.IOS }

        val notes = mutableListOf<String>()
        if (!hasCommonTest) {
            notes += "commonTest source set was not found."
        }
        if (gradleHeuristics.hasAndroidTarget && !hasAndroidSourceSet) {
            notes += "Android target detected but no Android source set directory was found."
        }
        if (hasAndroidSourceSet && !gradleHeuristics.hasAndroidTarget) {
            notes += "Android source sets found but no Android target declaration was detected."
        }
        if (gradleHeuristics.hasIosTarget && !hasIosSourceSet) {
            notes += "iOS target detected but no iOS source set directory was found."
        }
        if (hasIosSourceSet && !gradleHeuristics.hasIosTarget) {
            notes += "iOS source sets found but no iOS target declaration was detected."
        }
        return notes.sorted()
    }

    private fun toDisplayRelativePath(root: Path, path: Path): String {
        return root.relativize(path)
            .pathString
            .replace('\\', '/')
    }

    private companion object {
        val SUPPORTED_GRADLE_FILE_NAMES = setOf(
            "settings.gradle.kts",
            "settings.gradle",
            "build.gradle.kts",
            "build.gradle"
        )
        val IGNORED_DIRECTORY_NAMES = setOf(
            ".git",
            "build",
            ".gradle",
            ".idea",
            ".kotlin",
            "out"
        )

        val TOOLKIT_CHECKOUT_MARKERS = setOf(
            Path.of("gradlew"),
            Path.of("settings.gradle.kts"),
            Path.of("shared", "ai-client", "src"),
            Path.of("shared", "report-common", "src"),
            Path.of("tools", "kmp-project-auditor", "src")
        )

        val KMP_PLUGIN_PATTERNS = listOf(
            Regex("kotlin\\(\\s*\"multiplatform\"\\s*\\)", IGNORE_CASE),
            Regex("id\\(\\s*\"org\\.jetbrains\\.kotlin\\.multiplatform\"\\s*\\)", IGNORE_CASE)
        )

        val ANDROID_TARGET_PATTERNS = listOf(
            Regex("androidTarget\\s*\\(", IGNORE_CASE),
            Regex("\\bandroid\\s*\\(\\s*\\)", IGNORE_CASE),
            Regex("id\\(\\s*\"com\\.android\\.library\"\\s*\\)", IGNORE_CASE),
            Regex("id\\(\\s*\"com\\.android\\.application\"\\s*\\)", IGNORE_CASE),
            Regex("kotlin\\(\\s*\"android\"\\s*\\)", IGNORE_CASE)
        )

        val IOS_TARGET_PATTERNS = listOf(
            Regex("\\bios\\s*\\(\\s*\\)", IGNORE_CASE),
            Regex("\\biosX64\\s*\\(", IGNORE_CASE),
            Regex("\\biosArm64\\s*\\(", IGNORE_CASE),
            Regex("\\biosSimulatorArm64\\s*\\(", IGNORE_CASE)
        )
    }

    private fun shouldSkipDirectory(dir: Path): Boolean {
        val directoryName = dir.fileName.toString()
        if (directoryName in IGNORED_DIRECTORY_NAMES) {
            return true
        }

        return directoryName == "mobile-ai-toolkit" && looksLikeToolkitCheckout(dir)
    }

    private fun looksLikeToolkitCheckout(dir: Path): Boolean {
        return TOOLKIT_CHECKOUT_MARKERS.all { marker ->
            Files.exists(dir.resolve(marker))
        }
    }
}
