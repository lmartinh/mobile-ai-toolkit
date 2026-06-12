plugins {
    base
    kotlin("jvm") version "2.4.0" apply false
    kotlin("plugin.serialization") version "2.4.0" apply false
}

allprojects {
    group = "dev.mobileai.toolkit"
    version = providers.gradleProperty("releaseVersion")
        .orElse("0.1.0-SNAPSHOT")
        .get()

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
