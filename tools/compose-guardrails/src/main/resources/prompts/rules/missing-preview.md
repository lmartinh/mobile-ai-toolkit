# Rule: compose.missing-preview

- category: developer-experience
- goal: improve UI iteration with preview coverage.
- recommended severity: info

## What to detect
- User-facing composables with no practical `@Preview`.

## What not to detect
- Composables where preview setup is genuinely impractical without significant infrastructure.

## Bad example
```kotlin
@Composable fun LoginScreen(state: LoginUiState) { ... }
```

## Improved example
```kotlin
@Preview @Composable fun LoginScreenPreview() { LoginScreen(LoginUiState()) }
```

## Guidance for actionable suggestions
- Suggest 1-2 preview variants (default/error/loading).

## False positive notes
- Keep low severity for non-core components.
