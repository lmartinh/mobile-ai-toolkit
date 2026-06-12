plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("dev.mobileai.toolkit.composeguardrails.MainKt")
}

dependencies {
    implementation(project(":shared:ai-client"))
    implementation(project(":shared:report-common"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    testImplementation(kotlin("test"))
}
