# Compose Guardrails Prompt System

This page explains how `compose-guardrails` forms prompts and how the prompt contract stays stable.

## Where prompt assets live

Prompt assets are stored as Markdown resources under `tools/compose-guardrails/src/main/resources/prompts/`:

- `compose-review.md` - base review instructions
- `output-format.md` - structured output contract
- `rules/*.md` - individual guardrail rule prompts
- `rules/default-index.txt` - default rule catalog
- `rules/advanced-index.txt` - advanced rule catalog

`PromptAssetLoader` loads these assets from the classpath, so packaged distributions and tests use the same resources as local development.

## Why the prompts are Markdown resources

Markdown keeps the prompt text reviewable, versioned, and easy to diff.
It also keeps prompt wording out of Kotlin source, which makes rule tuning and doc review simpler for contributors.

## How the final prompt is composed

`PromptComposer` combines the loaded assets into one prompt with these sections:

1. `# Compose Guardrails Review Request`
2. `## Base Review Instructions`
3. `## Output Requirements`
4. `## Active Rules`
5. `## Analysis Context`

For each selected rule, the composer includes:

- the rule ID
- the rule Markdown content

For each Compose candidate file, the composer includes:

- file path
- detected `@Composable` function names and approximate line numbers
- the full file content as a Kotlin code block

If no Compose candidate files are detected, the prompt states that explicitly.

## Rule catalog selection

`compose-guardrails` supports three rule-set modes:

- `default` - conservative rules intended for routine CI
- `advanced` - lower-confidence or noisier rules for opt-in use
- `all` - the union of both catalogs

The default catalog is the recommended baseline. Advanced rules should stay conservative and only report when evidence is clear.

## Structured output contract

The prompt requires one JSON object with a top-level `findings` field.

- If there are no findings, the response must be exactly `{ "findings": [] }`
- Extra prose or Markdown fences are not allowed
- Each finding must include the fields documented in `output-format.md`

The parser is intentionally strict about this contract. If the response is malformed or missing the required top-level field, it is treated as invalid and surfaced through parser warnings instead of being silently accepted as empty output.

## Fake provider and deterministic CI

`shared/ai-client/FakeAiClient` gives deterministic responses for tests and CI.
That lets the prompt pipeline, parser, and report rendering be exercised without real API calls.

## How to modify prompts safely

When changing prompt wording or rule content:

- keep prompt assets in Markdown files under `src/main/resources/prompts/`
- update the relevant rule file and its catalog index when adding or removing rules
- keep the output contract aligned with `output-format.md`
- avoid provider-specific prompt branches unless there is a strong reason
- prefer small wording changes over broad prompt rewrites

## Tests to run after prompt changes

At minimum, run:

```bash
./gradlew :tools:compose-guardrails:test
```

Useful follow-up checks when prompt assets or output contracts change:

- prompt asset integrity tests
- parser contract tests
- example report tests

## Pattern for future tools

Future tools in this monorepo should follow the same pattern unless there is a strong reason not to:

- Markdown prompt assets under the tool module
- deterministic classpath loading
- explicit output contract
- parser tests for malformed and valid payloads
- fake-provider-compatible behavior for CI
- no provider-specific prompt forks without justification

