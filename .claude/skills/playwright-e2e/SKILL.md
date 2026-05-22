---
name: playwright-e2e
description: Use when adding, debugging, or running Playwright end-to-end tests for Nihongo IT.
  Defines the stack startup contract, the three-agent flow (Planner → Generator → Healer),
  seed/auth conventions, and the postgres-only-in-docker constraint.
---

# Playwright E2E — Nihongo IT

End-to-end suite at `e2e/` covering the user app (`localhost:3000`), the admin app (`localhost:3001`), and the BE stack reachable via API Gateway (`localhost:8080`).

See `docs/plans/2026-05-21-playwright-e2e-plan.md` for the full plan.

## When to invoke this skill

- Adding a new test for a feature → spawn the **Planner** agent first
- Converting a spec markdown to runnable tests → spawn the **Generator** agent
- A test failed → spawn the **Healer** agent with the failing test path + trace
- Running the suite locally → follow the quickstart below

## Stack startup contract

Only Postgres runs in Docker. Everything else (Eureka, gateway, 4 BE services, 2 FE apps) is launched by `e2e/scripts/start-stack.ps1` as background processes with readiness polling.

```powershell
cd e2e
./scripts/start-stack.ps1            # boots full stack (~3 min cold)
./scripts/start-stack.ps1 -SkipFE    # backend only (faster for API-only specs)
./scripts/start-stack.ps1 -IncludeAI # opt-in: ai-service (broken on Spring Boot 4)

npm test                             # runs suite (browser visible by default)
./scripts/stop-stack.ps1             # kills all PIDs + stops postgres container
```

### Critical env flags set by start-stack.ps1

| Flag | Why |
|---|---|
| `SPRING_FLYWAY_ENABLED=false` | Devtools+Flyway+JPA race breaks bootRun on fresh DB. E2E uses one-time SQL apply instead. |
| `SPRING_JPA_HIBERNATE_DDL_AUTO=none` | Skip strict schema validation — some entity/column mismatches exist but don't affect runtime. |
| `APP_RATE_LIMIT_ENABLED=false` | Gateway rate-limit filter respects this. Production keeps default true. |
| `SPRING_DEVTOOLS_RESTART_ENABLED=false` | Devtools' classloader caused half-closed chunked responses during bootRun. |
| `SPRING_THREADS_VIRTUAL_ENABLED=false` | Same response-stream issue. |
| `JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Ho_Chi_Minh` | Postgres rejects `Asia/Saigon`. |

## Auth + seed convention

`tests/seed.spec.ts` runs first (project `setup`):

1. **Upsert** the two test users in `user_service` DB directly via SQL (`helpers/db.ts → upsertUser`) — bcryptjs `$2b$10$`. Idempotent.
2. **Raw Node http login** through gateway to capture the `refresh_token` cookie. The BE's chunked response is half-closed (real bug), so we use a tolerant raw http call instead of axios.
3. **Inject both cookie variants** into the Playwright context (gateway path + FE root path) so the Next.js `proxy.ts` gate also sees it.
4. **Visit `/`** in a real browser to let the FE's `axios.interceptors.response` complete the refresh → `storageState` captures everything.

Result: `.auth/user.json` + `.auth/admin.json` files. Used by `user` and `admin` projects via Playwright project `dependencies`.

### Test users

| Role | Email | Password |
|------|-------|----------|
| user | `user@e2e.test` | `User#2026` |
| admin | `admin@e2e.test` | `Admin#2026` |

Override via `.env` (`E2E_USER_EMAIL` etc.). NEVER delete these via `truncate()` — `helpers/db.ts → truncateLearningOwned()` is safe; full `TRUNCATE users` would destroy seed state.

## External-service gating

Tests that need OpenAI or Python NLP must wrap the assertion in `test.skip()`:

```typescript
import { E2E_FEATURES } from '@helpers/feature-flags'

test.describe('Conversation chat', () => {
  test('chat UI renders', async ({ page }) => { /* always runs */ })

  test.skip(!E2E_FEATURES.openai, 'requires E2E_OPENAI=1')(
    'AI replies to message',
    async ({ page }) => { /* only when E2E_OPENAI=1 */ }
  )
})
```

Default = both flags OFF. Every spec touching AI/Python keeps ≥1 render-only test ungated so the suite always exercises the page's UI.

## Selectors

Hierarchy (prefer top-down):
1. **Role + accessible name** — `page.getByRole('button', { name: 'Đăng nhập' })`
2. **Label / placeholder** — `page.getByLabel('Email')`
3. **`data-testid`** — fallback for ambiguous lists / dynamic content

All `data-testid` values live in `e2e/helpers/selectors.ts`. Convention: `<area>-<element>[-<modifier>]` (e.g. `vocab-list-item`, `admin-user-row-deactivate`).

## Page Object Model

One file per page in `e2e/helpers/pages/`. Extend `BasePage`:

```typescript
import type { Page } from '@playwright/test'
import { BasePage } from './BasePage'

export class FooPage extends BasePage {
  protected readonly path = '/foo'
  readonly title = this.page.getByRole('heading', { name: 'Foo' })

  constructor(page: Page) { super(page) }
}
```

Expose POM via `e2e/fixtures/auth.fixture.ts`:

```typescript
export const test = base.extend<{ fooPage: FooPage }>({
  fooPage: async ({ page }, use) => { await use(new FooPage(page)) },
})
```

Tests then `import { test, expect } from '@fixtures/auth.fixture'` and use `fooPage` directly.

## Spec blueprint format (Planner output → `e2e/specs/<NN>-<area>.md`)

```markdown
# 03 — Flashcards (user)

## Pre-conditions
- `seed` project has run.
- At least 5 vocabulary items in `vocabulary` table (seeded).

## Test cases

### TC-03-01: Create flashcard from vocabulary (@smoke)
- **Route**: `/vocabulary/[id]`
- **Steps**:
  1. Navigate to `/vocabulary` and click first item.
  2. Click button "Tạo flashcard".
- **Assertions**:
  - Toast contains "Đã tạo".
  - `GET /api/v1/learning/flashcards` includes new card.

### TC-03-02: Study session — rate cards 1..4
…
```

## The three-agent flow

1. **`playwright-test-planner`** — opens the running app via Playwright MCP browser tools, explores the feature interactively, writes Markdown blueprint to `e2e/specs/<NN>-<area>.md`.
2. **`playwright-test-generator`** — reads a spec, writes `.spec.ts` to `e2e/tests/<area>/`. NO browser tools — pure code generation from spec + existing POM patterns.
3. **`playwright-test-healer`** — given a failing test path + trace, decides selector drift vs real regression. Auto-fixes selectors/waits; escalates regressions without touching the code.

See `.claude/agents/playwright-test-*.md` for the agent contracts.

## Useful npm scripts (e2e/)

| Goal | Command |
|------|---------|
| Full suite | `npm test` |
| Smoke only | `npm run test:smoke` |
| User app project only | `npm run test:user` |
| Admin app project only | `npm run test:admin` |
| UI mode (interactive debug) | `npm run test:ui` |
| Step-through debugger | `npm run test:debug` |
| Open last HTML report | `npm run report` |
| View a trace | `npm run trace test-results/<...>/trace.zip` |

## Verification before committing test changes

1. `npx tsc --noEmit` — type-check
2. `npm test` — full run, ≤ 2 min on a warm stack
3. Confirm no new flaky test (re-run 3x if you suspect timing)
4. If you added a new POM or selector, update `e2e/helpers/selectors.ts` even if just commenting which test uses it.

## Troubleshooting cheat sheet

| Symptom | Likely cause |
|---------|--------------|
| `connection refused on 5433` | Postgres not running → `./scripts/start-stack.ps1` |
| `Process from config.webServer was not able to start` | Don't use Playwright `webServer[]` — we removed it. Stack lifecycle is the script's job. |
| Test redirects to `/login` mid-test | FE got 401 from a learning-service/ai-service endpoint that triggered axios interceptor's `window.location.href = /login`. Either the stack is missing that BE service or auth state is stale. |
| `apiRequestContext.post: aborted` | BE half-closed chunked response. Use the raw-Node-http pattern from `seed.spec.ts → rawLogin`. |
| `Schema validation: missing table [...]` | Service started against a DB that wasn't migrated. Re-run the SQL apply step in `start-stack.ps1` notes. |
| `Spring AI: RestClientAutoConfiguration not present` | ai-service skipped by default (Spring AI 1.0.0 ↔ Spring Boot 4 incompat). Pass `-IncludeAI` only when actively debugging that service. |
