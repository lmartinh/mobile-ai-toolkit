# Rule: compose.state-hoisting

- category: state-management
- goal: make reusable composables externally controllable.
- recommended severity: warning

## What to detect
- Reusable leaf composables owning mutable state that callers should provide.

## What not to detect
- Truly local ephemeral UI state with no external coordination need.

## Bad example
```kotlin
@Composable
fun SearchField() { var q by remember { mutableStateOf("") } }
```

## Improved example
```kotlin
@Composable
fun SearchField(query: String, onQueryChange: (String) -> Unit) { }
```

## Guidance for actionable suggestions
- Suggest parameter + callback APIs for reusable components.

## False positive notes
- Do not require hoisting for tiny private UI toggles.
