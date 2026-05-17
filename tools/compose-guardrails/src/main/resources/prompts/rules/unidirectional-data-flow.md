# Rule: compose.unidirectional-data-flow

- category: state-management
- goal: keep data flow predictable: state down, events up.
- recommended severity: warning

## What to detect
- Child mutating parent-like state directly or hidden two-way coupling.

## What not to detect
- Explicit event callbacks from child to parent.

## Bad example
```kotlin
@Composable
fun Child(parentState: MutableState<String>) { parentState.value = "x" }
```

## Improved example
```kotlin
@Composable
fun Child(value: String, onValueChange: (String) -> Unit) { }
```

## Guidance for actionable suggestions
- Recommend immutable state params + event callbacks.

## False positive notes
- Not every `MutableState` param is wrong; context matters.
