# Rule: compose.multiplatform.resources-usage

- category: multiplatform-commonMain
- goal: use multiplatform-friendly resource access patterns.
- recommended severity: warning

## What to detect
- Android resource access patterns (`R.string...`, `colorResource` from Android-only APIs) in shared code.

## What not to detect
- Android-only resource usage in `androidMain`.

## Bad example
```kotlin
Text(stringResource(R.string.title)) // in commonMain shared UI
```

## Improved example
```kotlin
Text(SharedStrings.title)
```

## Guidance for actionable suggestions
- Recommend shared resource abstractions compatible with target platforms.

## False positive notes
- Apply only where shared/common source intent is evident.
