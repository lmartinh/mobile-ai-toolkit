# Rule: compose.hardcoded-dimensions-and-colors

- category: design-system
- goal: favor theme/tokens over scattered magic values.
- recommended severity: info

## What to detect
- Repeated hardcoded color/spacing/text-size values in UI code.

## What not to detect
- Truly local one-off values in prototypes or isolated previews.

## Bad example
```kotlin
Text("Title", color = Color(0xFF123456), modifier = Modifier.padding(13.dp))
```

## Improved example
```kotlin
Text("Title", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(Dimensions.m))
```

## Guidance for actionable suggestions
- Suggest concrete theme/token alternatives.

## False positive notes
- Avoid over-reporting single experimental constants.
