# Rule: compose.missing-content-description

- category: accessibility
- goal: provide meaningful semantics for non-decorative visuals.
- recommended severity: warning

## What to detect
- Clickable or meaningful icons/images without content description.

## What not to detect
- Decorative images intentionally marked with `contentDescription = null`.

## Bad example
```kotlin
Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.clickable { ... })
```

## Improved example
```kotlin
Icon(Icons.Default.Delete, contentDescription = "Delete item", modifier = Modifier.clickable { ... })
```

## Guidance for actionable suggestions
- Suggest concise user-facing description text.

## False positive notes
- Do not flag purely decorative content.
