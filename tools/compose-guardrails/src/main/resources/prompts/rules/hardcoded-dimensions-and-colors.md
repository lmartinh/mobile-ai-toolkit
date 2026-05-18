# Rule: compose.hardcoded-dimensions-and-colors

- category: design-system (advanced)
- goal: improve consistency by preferring theme/tokens over repeated magic values.
- recommended severity: info

## What to detect
- Repeated hardcoded color/spacing/text-size constants in production UI paths.

## What not to detect
- Isolated one-off constants in previews/prototypes.
- Small local values when no token system exists yet and no consistency issue is evident.

## Bad example
```kotlin
Text("Title", color = Color(0xFF123456), modifier = Modifier.padding(13.dp))
```

## Improved example
```kotlin
Text("Title", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(Dimensions.m))
```

## Guidance for actionable suggestions
- Suggest concrete token/theme alternatives only when practical.
- Avoid noisy reports on isolated constants with low impact.

## False positive notes
- Advanced rule; report conservatively.
