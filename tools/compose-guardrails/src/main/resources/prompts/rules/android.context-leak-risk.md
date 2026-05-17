# Rule: compose.android.context-leak-risk

- category: android-only
- goal: avoid retaining leaking Android context references.
- recommended severity: error

## What to detect
- Storing `LocalContext.current` or Activity references in long-lived/static/singleton state.

## What not to detect
- Short-lived direct use in composition or callback scope without retention.

## Bad example
```kotlin
object Holder { var ctx: Context? = null }
@Composable fun Screen() { Holder.ctx = LocalContext.current }
```

## Improved example
```kotlin
@Composable fun Screen(onOpen: (Uri) -> Unit) { /* pass events, avoid retained context */ }
```

## Guidance for actionable suggestions
- Suggest passing intents/events outward or using application-safe abstractions.

## False positive notes
- Not every context access is a leak; focus on retention.
