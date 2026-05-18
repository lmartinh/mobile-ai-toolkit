# Rule: compose.large-composable

- category: maintainability (advanced)
- goal: keep composables cohesive and understandable.
- recommended severity: info

## What to detect
- Composables mixing multiple distinct responsibilities with high nesting and difficult readability.

## What not to detect
- Verbose yet cohesive layouts with clear helper decomposition.
- Cases where size alone is not causing maintainability issues.

## Bad example
```kotlin
@Composable fun Dashboard(...) { /* huge function mixing toolbar, filters, chart, dialogs */ }
```

## Improved example
```kotlin
@Composable fun Dashboard(...) {
  DashboardHeader(...)
  DashboardFilters(...)
  DashboardContent(...)
  DashboardDialogs(...)
}
```

## Guidance for actionable suggestions
- Call out concrete mixed concerns, not line-count alone.
- Suggest 1-2 realistic split points.

## False positive notes
- Advanced rule; avoid subjective style-only findings.
