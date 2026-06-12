# Compose Guardrails Report

## Summary
- Analyzed path: `tools/compose-guardrails/examples/bad-compose-sample`
- Kotlin files scanned: 1
- Total findings: 1
- Affected files: 1
- Findings by severity: error=0, warning=1, info=0

## Findings

### File: LoginScreen.kt
#### Warning: Business logic inside Composable
- Rule: `compose.no-business-logic-in-composables`
- Explanation: Validation logic appears directly in UI code.
- Suggestion: Move validation to ViewModel or domain use case.
- Code example:
```kotlin
viewModel.validateCredentials(email, password)
```
