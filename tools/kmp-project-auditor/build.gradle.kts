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
    testImplementation(kotlin("test"))
}
