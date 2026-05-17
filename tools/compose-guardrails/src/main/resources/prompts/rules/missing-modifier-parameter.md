# Rule: compose.missing-modifier-parameter

- category: api-design
- goal: keep reusable composables flexible for callers.
- recommended severity: info

## What to detect
- Public reusable composable without `modifier: Modifier = Modifier`.

## What not to detect
- Private/internal helper composables with tightly scoped usage.

## Bad example
```kotlin
@Composable fun Avatar(name: String) { ... }
```

## Improved example
```kotlin
@Composable fun Avatar(name: String, modifier: Modifier = Modifier) { ... }
```

## Guidance for actionable suggestions
- Recommend adding modifier param with default.

## False positive notes
- Skip tightly bound one-off internal composables.
