# Compose Guardrails

## Supported Guardrails

Core Compose:
- `compose.no-business-logic-in-composables`
- `compose.state-hoisting`
- `compose.viewmodel-in-leaf-composable`
- `compose.unidirectional-data-flow`
- `compose.no-side-effects-in-composition`
- `compose.effect-key-quality`
- `compose.expensive-work-in-composition`
- `compose.unstable-parameters`
- `compose.lazy-list-keys`
- `compose.derived-state-usage`
- `compose.large-composable`
- `compose.missing-modifier-parameter`
- `compose.modifier-parameter-position`
- `compose.hardcoded-dimensions-and-colors`
- `compose.missing-preview`
- `compose.preview-with-real-dependencies`
- `compose.missing-content-description`
- `compose.clickable-without-semantics`

Android-only:
- `compose.android.collect-as-state-with-lifecycle`
- `compose.android.context-leak-risk`

Multiplatform/commonMain:
- `compose.multiplatform.no-android-api-in-common`
- `compose.multiplatform.resources-usage`
- `compose.multiplatform.platform-specific-ui-leak`
- `compose.multiplatform.public-api-cleanliness`

## Severity levels
- `error`: likely correctness/architecture issue with meaningful risk.
- `warning`: important quality issue that should generally be fixed.
- `info`: lower-risk improvement opportunity.

## Current limitations
- Detection is text-based and heuristic (no AST parsing yet).
- Findings may include false positives/false negatives.
- Provider layer has no streaming or retry/backoff yet.

## Recommendation
Treat output as assisted review. Validate findings manually before applying changes.
