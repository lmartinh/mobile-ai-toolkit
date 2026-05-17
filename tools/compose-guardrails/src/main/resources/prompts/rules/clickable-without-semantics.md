# Rule: compose.clickable-without-semantics

- category: accessibility
- goal: ensure interactive elements expose clear semantics.
- recommended severity: warning

## What to detect
- Generic containers with `clickable` but no role/label semantics.

## What not to detect
- Standard Material components that already provide semantics.

## Bad example
```kotlin
Box(Modifier.clickable { onOpen() }) { Text("Open") }
```

## Improved example
```kotlin
Box(
  Modifier
    .semantics { role = Role.Button; contentDescription = "Open details" }
    .clickable { onOpen() }
) { Text("Open") }
```

## Guidance for actionable suggestions
- Suggest role and meaningful label when missing.

## False positive notes
- Avoid duplicating semantics guidance for already-semantic controls.
