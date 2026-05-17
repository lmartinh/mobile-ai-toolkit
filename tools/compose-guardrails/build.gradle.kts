plugins {
    kotlin("jvm") version "2.1.21"
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
    implementation(project(":shared:cli-common"))
    implementation(project(":shared:report-common"))
    testImplementation(kotlin("test"))
}
