# Rule: compose.no-side-effects-in-composition

- category: correctness
- goal: prevent side effects during composition.
- recommended severity: error

## What to detect
- Navigation, logging to backend, repository/network calls directly in composable body.

## What not to detect
- Side effects inside proper effect APIs (`LaunchedEffect`, `DisposableEffect`) with valid keys.

## Bad example
```kotlin
@Composable
fun Screen(repo: Repo) { repo.load() }
```

## Improved example
```kotlin
@Composable
fun Screen(onLoad: suspend () -> Unit) { LaunchedEffect(Unit) { onLoad() } }
```

## Guidance for actionable suggestions
- Move side effects to effect handlers or ViewModel.

## False positive notes
- Avoid flagging pure calculations.
