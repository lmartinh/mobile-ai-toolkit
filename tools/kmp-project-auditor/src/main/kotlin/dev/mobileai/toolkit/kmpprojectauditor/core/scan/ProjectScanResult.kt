package dev.mobileai.toolkit.kmpprojectauditor.core.scan

import java.nio.file.Path

data class ProjectScanResult(
    val analyzedPath: Path,
    val gradleFiles: List<String>,
    val sourceSets: List<String>,
    val kotlinSourceRoots: List<String>
) {
    val hasCommonMain: Boolean = sourceSets.contains("commonMain")
    val hasCommonTest: Boolean = sourceSets.contains("commonTest")
    val hasAndroidSourceSet: Boolean = sourceSets.any { it.contains("android", ignoreCase = true) }
    val hasIosSourceSet: Boolean = sourceSets.any { it.contains("ios", ignoreCase = true) }
}
