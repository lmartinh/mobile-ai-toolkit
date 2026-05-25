plugins {
    kotlin("multiplatform") version "2.1.21"
    id("com.android.library")
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
}
