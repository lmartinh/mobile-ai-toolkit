package dev.mobileai.toolkit.kmpprojectauditor.core.scan

import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.SimpleFileVisitor
import java.nio.file.FileVisitResult
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.pathString

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
                    if (dir != normalizedRoot && dir.fileName.toString() in IGNORED_DIRECTORY_NAMES) {
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

        return ProjectScanResult(
            analyzedPath = normalizedRoot,
            gradleFiles = gradleFiles.toList().sorted(),
            sourceSets = sourceSetDirectories.toList().sorted(),
            kotlinSourceRoots = kotlinSourceRoots.toList().sorted()
        )
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
            "build",
            ".gradle",
            ".idea",
            ".kotlin",
            "out"
        )
    }
}
