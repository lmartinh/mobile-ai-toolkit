# Rule: compose.missing-preview

- category: developer-experience (advanced)
- goal: improve iteration speed with practical preview coverage.
- recommended severity: info

## What to detect
- Important user-facing composables lacking feasible previews.

## What not to detect
- Cases where preview setup is non-trivial and would require heavy runtime dependencies.
- Internal utility composables with little preview value.

## Bad example
```kotlin
@Composable fun LoginScreen(state: LoginUiState) { ... }
```

## Improved example
```kotlin
@Preview @Composable fun LoginScreenPreview() { LoginScreen(LoginUiState()) }
```

## Guidance for actionable suggestions
- Suggest only high-value previews (default/error/loading) when clearly useful.
- Keep severity low and avoid forcing previews everywhere.

## False positive notes
- Advanced rule; omit if preview value is uncertain.
