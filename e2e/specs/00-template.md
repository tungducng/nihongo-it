# 00 — Spec template (reference for Planner + Generator)

> This file is NOT a test spec. It exists so the Planner and Generator agents
> have a concrete example of the expected format. Copy this structure when
> writing new specs.

> **Project**: `user` (or `admin`) — which Playwright project this spec maps to.
> **External services**: `none` | `openai` | `python-nlp` — gating for tests that
> need OpenAI / Python NLP service. Default: `none`.

## Pre-conditions

- `setup` project (`tests/seed.spec.ts`) has run — provides:
  - `user@e2e.test` / `User#2026` (role: USER)
  - `admin@e2e.test` / `Admin#2026` (role: ADMIN)
  - `.auth/user.json` + `.auth/admin.json` storageState files
- Stack is UP (`./scripts/start-stack.ps1` finished).
- Any additional data this feature needs (e.g. "≥5 vocabulary items in `vocabulary` table") goes here.

## Test cases

### TC-NN-01: Short imperative title (@smoke)

- **Route**: `/some/path`
- **Steps**:
  1. Navigate to `/some/path`.
  2. Click button "Save".
  3. Wait for toast.
- **Assertions**:
  - URL is not `/login` (within 15s).
  - Heading "Some title" is visible.
  - Toast contains "Đã lưu".
  - (Optional API contract): `GET /api/v1/learning/foo` returns `200` and body has `items.length > 0`.
- **Selectors to add** (if any):
  - `data-testid="foo-list-item"` on `<li>` inside `FooList.tsx` — the role-based query isn't unique because items share the same accessible name.

### TC-NN-02: Title…

- **Route**: …
- **Gated**: requires `E2E_OPENAI=1` (if applicable)
- …

## Notes for the Generator

- Map this spec to `e2e/tests/<project>/<feature>.spec.ts`.
- Tests are **independent** — no shared mutable state across test functions.
- Use `getByRole` / `getByLabel` first; `data-testid` only for entries listed above.
- For gated cases, wrap with `test.skip(!E2E_FEATURES.openai, '...')` (or `pythonNlp`).
