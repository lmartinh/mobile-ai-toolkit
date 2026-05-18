# Rule: compose.multiplatform.resources-usage

- category: multiplatform-commonMain (advanced)
- goal: keep shared UI resource access compatible across targets.
- recommended severity: warning

## What to detect
- Android-specific resource patterns in shared/common UI code (e.g., `R.*`, Android-only resource helpers).

## What not to detect
- Android resource access inside `androidMain`.
- Shared code already using multiplatform-friendly resource abstractions.

## Bad example
```kotlin
// common/shared UI file
Text(stringResource(R.string.title))
```

## Improved example
```kotlin
// shared API through project resource abstraction
Text(AppStrings.title())
```

## Guidance for actionable suggestions
- Suggest platform-agnostic resource wrappers suitable for Compose Multiplatform/library setups.
- Keep suggestions generic (avoid forcing a single resource library unless already present).

## False positive notes
- Apply only when shared/common source context is evident.
