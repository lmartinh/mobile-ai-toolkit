package dev.mobileai.toolkit.kmpprojectauditor.core.cli

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class ReportOutputWriter {
    fun write(report: String, outputPath: Path) {
        try {
            outputPath.parent?.let { Files.createDirectories(it) }
            Files.writeString(outputPath, report, StandardCharsets.UTF_8)
        } catch (error: Exception) {
            throw IllegalArgumentException("Unable to write report to: ${outputPath.toAbsolutePath().normalize()}")
        }
    }
}
