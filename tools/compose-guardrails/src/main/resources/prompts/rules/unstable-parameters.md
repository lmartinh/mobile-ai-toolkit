# Rule: compose.unstable-parameters

- category: performance
- goal: reduce unnecessary recompositions from unstable inputs.
- recommended severity: info

## What to detect
- Passing frequently recreated lambdas/objects where stable alternatives exist.

## What not to detect
- Cases where instability impact is negligible or unavoidable.

## Bad example
```kotlin
Child(onClick = { doSomething(id) })
```

## Improved example
```kotlin
val onClick = remember(id) { { doSomething(id) } }
Child(onClick = onClick)
```

## Guidance for actionable suggestions
- Suggest stable memoized callbacks/objects when impact is clear.

## False positive notes
- Do not over-report micro-optimizations.
