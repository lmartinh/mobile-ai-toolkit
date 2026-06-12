plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.4.0"
}

kotlin {
    ios()
    sourceSets {
        commonMain.dependencies {
            implementation("androidx.core:core-ktx:1.17.0")
        }
    }
}
