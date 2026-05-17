# Rule: compose.derived-state-usage

- category: performance
- goal: memoize derived values driven by changing state.
- recommended severity: info

## What to detect
- Recomputed derived values on every recomposition where `derivedStateOf` would clarify intent.

## What not to detect
- Cheap derived values where added complexity is not justified.

## Bad example
```kotlin
val isValid = email.contains("@") && pass.length > 7
```

## Improved example
```kotlin
val isValid by remember(email, pass) { derivedStateOf { email.contains("@") && pass.length > 7 } }
```

## Guidance for actionable suggestions
- Recommend only when recomposition pressure is plausible.

## False positive notes
- Do not force `derivedStateOf` for trivial expressions.
