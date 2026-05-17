# Rule: compose.preview-with-real-dependencies

- category: developer-experience
- goal: keep previews lightweight and deterministic.
- recommended severity: warning

## What to detect
- Previews that call real network/database/platform services.

## What not to detect
- Fake/stubbed preview dependencies.

## Bad example
```kotlin
@Preview @Composable fun UserPreview() { UserScreen(repo = RealRepo()) }
```

## Improved example
```kotlin
@Preview @Composable fun UserPreview() { UserScreen(state = fakeState) }
```

## Guidance for actionable suggestions
- Recommend fake data and pure preview entrypoints.

## False positive notes
- Ignore obvious no-op dependency placeholders.
