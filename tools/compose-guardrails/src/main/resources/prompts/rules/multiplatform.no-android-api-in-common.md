# Rule: compose.multiplatform.no-android-api-in-common

- category: multiplatform-commonMain
- goal: keep commonMain free of Android-specific APIs.
- recommended severity: error

## What to detect
- `android.*` imports or Android-only classes used in common/shared Compose code.

## What not to detect
- Android source set files (`androidMain`) where Android APIs are expected.

## Bad example
```kotlin
import android.content.Context
```

## Improved example
```kotlin
expect interface PlatformOpener { fun open(url: String) }
```

## Guidance for actionable suggestions
- Suggest expect/actual or platform interface boundaries.

## False positive notes
- Ensure file/source-set context indicates commonMain/shared scope.
