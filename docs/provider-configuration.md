# Provider Configuration

`mobile-ai-toolkit` reads provider settings from environment variables.

## Environment variables

- `MOBILE_AI_PROVIDER`: `fake`, `openai`, `anthropic`, or `gemini`
- `MOBILE_AI_API_KEY`: required for `openai`, `anthropic`, and `gemini`
- `MOBILE_AI_MODEL`: required for real providers in the current implementation

## Recommended default

For deterministic local development and CI, use `MOBILE_AI_PROVIDER=fake`.

## Recommended models

| Provider | Model |
| --- | --- |
| `openai` | `gpt-4.1-mini` |
| `anthropic` | `claude-3-5-sonnet` |
| `gemini` | `gemini-1.5-pro` |
| `fake` | Not required |

Some repository workflows set these values for convenience. Direct CLI usage and reusable actions should pass `MOBILE_AI_MODEL` explicitly when using a real provider.

## Security

- Keep API keys in environment variables or GitHub Secrets.
- Do not commit API keys or tokens to source control.
- Forks should configure their own secrets.

## OpenAI API key permissions

Use a restricted OpenAI key for this workflow instead of an unrestricted key.

| Permission area | Access |
| --- | --- |
| List models | Read |
| Responses (`/v1/responses`) | Write |
| Chat completions (`/v1/chat/completions`) | Write |
| Text-to-speech | None |
| Realtime | None |
| Embeddings | None |
| Images | None |
| Moderations | None |
| Assistants | None |
| Threads | None |
| Evals | None |

`List models` is useful for diagnostics, while `Responses` and `Chat completions` cover the supported OpenAI API paths. The other permissions are not needed for the toolkit's AI reports.
