# Rule: compose.remember-usage

## Goal
Use `remember` and `mutableStateOf` safely to avoid stale values, incorrect caching, and misplaced state ownership.

## What to detect
- `remember` used for state that should come from ViewModel/domain state holder.
- `remember` caches tied to changing inputs without keys.
- Nested composables creating long-lived mutable state without clear ownership.

## What not to detect
- Legitimate local UI state with clear lifecycle scope.
- Stateless calculations that do not require persistence.

## Recommended severity
- `warning` by default.
- `error` when stale/incorrect state is likely to cause functional bugs.

## Bad example
```kotlin
@Composable
fun Profile(userId: String) {
    val profile = remember { loadProfile(userId) }
    Text(profile.name)
}
```

## Improved example
```kotlin
@Composable
fun Profile(profile: ProfileUiModel) {
    Text(profile.name)
}
```

## Guidance for actionable suggestions
- Explain why current `remember` scope is risky.
- Suggest moving long-lived state to ViewModel.
- Suggest adding proper keys when cache depends on changing inputs.
