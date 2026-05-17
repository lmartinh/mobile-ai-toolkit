# Rule: compose.modifier-parameter-position

- category: api-design
- goal: keep composable APIs idiomatic and predictable.
- recommended severity: info

## What to detect
- `modifier` parameter not placed as the first optional parameter after required ones (or in non-idiomatic position).

## What not to detect
- Legacy APIs where changing order would break binary/source compatibility and is intentionally frozen.

## Bad example
```kotlin
@Composable fun Card(title: String, onClick: () -> Unit, modifier: Modifier = Modifier)
```

## Improved example
```kotlin
@Composable fun Card(title: String, modifier: Modifier = Modifier, onClick: () -> Unit)
```

## Guidance for actionable suggestions
- Suggest parameter reordering only when safe.

## False positive notes
- Prefer low severity for compatibility-sensitive APIs.
