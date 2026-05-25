plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.1.21"
}

kotlin {
    ios()
    sourceSets {
        commonMain.dependencies {
            implementation("androidx.core:core-ktx:1.13.1")
        }
    }
}
