# Rule: compose.derived-state-usage

- category: performance (advanced)
- goal: use `derivedStateOf` only when it improves recomposition behavior.
- recommended severity: info

## What to detect
- Expensive or noisy derived computations recalculated on frequent recompositions where memoized derivation is clearly beneficial.

## What not to detect
- Trivial boolean/string expressions.
- Cases where `derivedStateOf` would add indirection without practical benefit.

## Bad example
```kotlin
val filtered = items.filter { it.visible }.sortedBy { it.rank }
```

## Improved example
```kotlin
val filtered by remember(items) {
  derivedStateOf { items.filter { it.visible }.sortedBy { it.rank } }
}
```

## Guidance for actionable suggestions
- Suggest `derivedStateOf` only with clear recomposition/perf rationale.
- Do not suggest it for small, obvious derived values.

## False positive notes
- Advanced rule; skip if performance impact is uncertain.
