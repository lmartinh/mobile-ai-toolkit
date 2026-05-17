# Rule: compose.lazy-list-keys

- category: correctness
- goal: preserve item identity and state in lazy lists.
- recommended severity: warning

## What to detect
- `LazyColumn/LazyRow` items without stable keys for mutable/reorderable lists.

## What not to detect
- Static lists with guaranteed stable ordering and no per-item state.

## Bad example
```kotlin
items(users) { user -> UserRow(user) }
```

## Improved example
```kotlin
items(users, key = { it.id }) { user -> UserRow(user) }
```

## Guidance for actionable suggestions
- Suggest concrete stable key field.

## False positive notes
- If list is static and keyless use is harmless, severity can be info.
