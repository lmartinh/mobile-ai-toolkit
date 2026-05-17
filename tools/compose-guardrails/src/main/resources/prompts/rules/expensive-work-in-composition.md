# Rule: compose.expensive-work-in-composition

- category: performance
- goal: avoid heavy work on recomposition paths.
- recommended severity: warning

## What to detect
- Sorting/filtering/parsing/heavy allocations directly in composable body without memoization/offloading.

## What not to detect
- Small constant-time formatting.

## Bad example
```kotlin
@Composable
fun ListScreen(items: List<Item>) { val sorted = items.sortedBy { it.score } }
```

## Improved example
```kotlin
@Composable
fun ListScreen(items: List<Item>) { val sorted by remember(items) { derivedStateOf { items.sortedBy { it.score } } } }
```

## Guidance for actionable suggestions
- Suggest `remember/derivedStateOf` or moving work outside composition.

## False positive notes
- Avoid nitpicking trivial operations.
