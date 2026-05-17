package dev.mobileai.toolkit.composeguardrails.core

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.streams.toList

class KotlinFileScanner {
    fun scan(inputPath: Path): List<Path> {
        require(Files.exists(inputPath)) { "Path does not exist: $inputPath" }

        return when {
            inputPath.isRegularFile() -> scanSingleFile(inputPath)
            inputPath.isDirectory() -> scanDirectory(inputPath)
            else -> throw IllegalArgumentException("Path is neither a regular file nor a directory: $inputPath")
        }
    }

    private fun scanSingleFile(filePath: Path): List<Path> {
        if (filePath.extension != "kt") {
            throw IllegalArgumentException("File must have .kt extension: $filePath")
        }
        return listOf(filePath)
    }

    private fun scanDirectory(directoryPath: Path): List<Path> {
        Files.walk(directoryPath).use { stream ->
            return stream
                .filter { Files.isRegularFile(it) }
                .filter { it.extension == "kt" }
                .sorted()
                .toList()
        }
    }
}
