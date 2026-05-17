# Rule: compose.multiplatform.public-api-cleanliness

- category: multiplatform-api-design
- goal: keep public shared composable APIs stable and platform-agnostic.
- recommended severity: info

## What to detect
- Public shared composable params exposing platform-specific types.

## What not to detect
- Internal/private APIs in platform modules.

## Bad example
```kotlin
@Composable fun SharedCard(context: android.content.Context)
```

## Improved example
```kotlin
@Composable fun SharedCard(onOpen: (String) -> Unit)
```

## Guidance for actionable suggestions
- Recommend domain/ui abstractions over platform concrete types.

## False positive notes
- Skip when API is intentionally platform-scoped.
