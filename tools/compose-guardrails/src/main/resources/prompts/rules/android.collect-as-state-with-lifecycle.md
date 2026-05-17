# Rule: compose.android.collect-as-state-with-lifecycle

- category: android-only
- goal: use lifecycle-aware state collection in Android UI.
- recommended severity: warning

## What to detect
- Android Compose UI collecting `Flow` via `collectAsState()` where lifecycle-aware variant is appropriate.

## What not to detect
- Non-Android targets or contexts where lifecycle-runtime-compose is unavailable.

## Bad example
```kotlin
val uiState by viewModel.uiState.collectAsState()
```

## Improved example
```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

## Guidance for actionable suggestions
- Recommend lifecycle-aware collection in Android UI layers.

## False positive notes
- Mark as not applicable for commonMain/multiplatform shared UI.
