plugins {
    kotlin("multiplatform") version "2.4.0"
    id("com.android.library")
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
}
