# Rule: compose.effect-key-quality

- category: correctness
- goal: ensure effect scopes restart only when intended.
- recommended severity: warning

## What to detect
- Missing/unstable keys for `LaunchedEffect`/`DisposableEffect` tied to changing inputs.

## What not to detect
- `Unit` key when one-time behavior is explicitly intended.

## Bad example
```kotlin
LaunchedEffect(Unit) { viewModel.load(userId) }
```

## Improved example
```kotlin
LaunchedEffect(userId) { viewModel.load(userId) }
```

## Guidance for actionable suggestions
- Recommend explicit key(s) representing true dependency.

## False positive notes
- One-time load flows may intentionally use `Unit`.
