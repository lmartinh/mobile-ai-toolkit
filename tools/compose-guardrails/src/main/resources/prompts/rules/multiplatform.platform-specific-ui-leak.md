# Rule: compose.multiplatform.platform-specific-ui-leak

- category: multiplatform-commonMain
- goal: prevent platform-specific UI widgets from leaking into shared UI.
- recommended severity: warning

## What to detect
- Android-only UI components/APIs referenced in common/shared composables.

## What not to detect
- Properly isolated platform-specific composables under platform source sets.

## Bad example
```kotlin
@Composable fun SharedScreen() { AndroidView(factory = { ... }) }
```

## Improved example
```kotlin
@Composable fun SharedScreen(platformView: @Composable () -> Unit) { platformView() }
```

## Guidance for actionable suggestions
- Suggest abstraction points and platform injection boundaries.

## False positive notes
- Do not flag code clearly located in androidMain.
