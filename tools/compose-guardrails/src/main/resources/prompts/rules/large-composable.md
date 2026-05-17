# Rule: compose.large-composable

- category: maintainability
- goal: keep composables cohesive and decomposed.
- recommended severity: warning

## What to detect
- Very large composables mixing multiple responsibilities and deep nesting.

## What not to detect
- Verbose but cohesive UI sections with clear structure.

## Bad example
```kotlin
@Composable fun Dashboard(...) { /* huge function with mixed concerns */ }
```

## Improved example
```kotlin
@Composable fun Dashboard(...) { Header(...); Content(...); Footer(...) }
```

## Guidance for actionable suggestions
- Suggest concrete split points into child composables.

## False positive notes
- Avoid hard line-count thresholds without context.
