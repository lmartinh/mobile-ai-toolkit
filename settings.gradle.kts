plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "mobile-ai-toolkit"

include(
    ":tools:compose-guardrails",
    ":shared:ai-client",
    ":shared:report-common"
)
