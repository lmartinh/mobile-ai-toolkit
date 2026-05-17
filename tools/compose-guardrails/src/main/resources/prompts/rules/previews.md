# Rule: compose.previews

## Goal
Encourage useful `@Preview` coverage for faster UI iteration and safer visual refactoring.

## What to detect
- Public composables/screens with no previews where previews are feasible.
- Preview coverage that misses meaningful UI states (default/error/loading/empty) for key screens.

## What not to detect
- Composables that are not practical to preview without heavy setup (unless simple fakes can solve it).
- Cases where preview absence is justified by module constraints (if evident).

## Recommended severity
- `info` by default.
- `warning` for core user-facing screens with no preview coverage.

## Bad example
```kotlin
@Composable
fun LoginScreen(state: LoginUiState, onAction: (LoginAction) -> Unit) {
    // ... no preview in file
}
```

## Improved example
```kotlin
@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(state = LoginUiState(), onAction = {})
}
```

## Guidance for actionable suggestions
- Recommend one or two concrete preview variants.
- Keep guidance lightweight and practical.
