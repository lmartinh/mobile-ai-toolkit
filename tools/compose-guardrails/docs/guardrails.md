# Compose Guardrails

## Supported Guardrails

Default rules (enabled by default, conservative):
- `compose.no-business-logic-in-composables`
- `compose.state-hoisting`
- `compose.viewmodel-in-leaf-composable`
- `compose.unidirectional-data-flow`
- `compose.no-side-effects-in-composition`
- `compose.effect-key-quality`
- `compose.lazy-list-keys`
- `compose.missing-modifier-parameter`
- `compose.modifier-parameter-position`
- `compose.missing-content-description`
- `compose.clickable-without-semantics`

Android-only (default):
- `compose.android.collect-as-state-with-lifecycle`
- `compose.android.context-leak-risk`

Multiplatform/commonMain (default):
- `compose.multiplatform.no-android-api-in-common`
- `compose.multiplatform.platform-specific-ui-leak`
- `compose.multiplatform.public-api-cleanliness`

Advanced rules (opt-in):
- `compose.expensive-work-in-composition`
- `compose.unstable-parameters`
- `compose.derived-state-usage`
- `compose.large-composable`
- `compose.hardcoded-dimensions-and-colors`
- `compose.missing-preview`
- `compose.preview-with-real-dependencies`
- `compose.multiplatform.resources-usage`

## Rule catalog assessment
- The default catalog is intentionally conservative and optimized for higher-signal findings.
- The advanced catalog groups exploratory checks that are more subjective, performance-sensitive, or more likely to produce noise.
- Treat `default` as the recommended baseline for routine analysis.
- Treat `advanced` as an opt-in pass when broader coverage is more valuable than strict signal density.

## Severity levels
- `error`: likely correctness/architecture issue with meaningful risk.
- `warning`: important quality issue that should generally be fixed.
- `info`: lower-risk improvement opportunity.

## Current limitations
- Detection is text-based and heuristic (no AST parsing yet).
- Findings may include false positives/false negatives.
- Provider layer has no streaming or retry/backoff yet.
- Default rules are intentionally conservative to reduce noise.

## Rule-set selection
- `default`: conservative baseline for routine analysis.
- `advanced`: exploratory quality/performance checks with higher noise risk.
- `all`: union of default and advanced sets.

## CLI rule-set assessment
- `mobile-ai guardrails check <path>` uses `default` when no rule set is specified.
- `--rule-set default` loads only default rules.
- `--rule-set advanced` loads only advanced rules.
- `--rule-set all` loads both catalogs without duplicating rules.
- Unsupported values fail with a clear CLI error instead of silently falling back.

## Recommendation
Treat output as assisted review. Validate findings manually before applying changes.
