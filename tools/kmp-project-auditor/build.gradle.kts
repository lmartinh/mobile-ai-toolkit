plugins {
    kotlin("jvm")
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("dev.mobileai.toolkit.kmpprojectauditor.MainKt")
}

dependencies {
    implementation(project(":shared:ai-client"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    testImplementation(kotlin("test"))
}
