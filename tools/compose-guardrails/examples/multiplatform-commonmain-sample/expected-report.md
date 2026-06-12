# Compose Guardrails Report

## Summary
- Analyzed path: `tools/compose-guardrails/examples/multiplatform-commonmain-sample`
- Kotlin files scanned: 1
- Expected findings: 2

## Findings

### File: SharedScreen.kt
- Rule: `compose.multiplatform.no-android-api-in-common`
- Severity: `error`
- Title: Android API referenced in shared/common UI code

- Rule: `compose.multiplatform.public-api-cleanliness`
- Severity: `info`
- Title: Public shared API exposes platform-specific type
