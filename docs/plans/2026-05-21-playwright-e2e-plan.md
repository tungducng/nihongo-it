# Playwright E2E — full stack regression suite + self-healing agents

**Date**: 2026-05-21
**Status**: Draft (plan only — not yet executing)
**Owner**: ndtung723

---

## 1. Goal

Xây dựng bộ test E2E Playwright chạy **toàn bộ tính năng hiện có** của Nihongo IT (cả `frontend-user` và `frontend-admin`), tích hợp pattern 3 agent của Playwright (**Planner / Generator / Healer**) qua Claude Code skills + subagents để tự sinh, tự sửa khi UI đổi.

### Constraints (user-stated)

1. **Database**: 1 container Postgres duy nhất chạy trên Docker, chứa nhiều DB riêng cho mỗi service (đang có sẵn — `postgres_init.sql`).
2. **Mọi thứ khác chạy local**: 5 Spring Boot services + 2 Next.js apps đều `bootRun` / `npm run dev` trên host. Python NLP service chỉ chạy khi muốn test phần speech (xem §3a).
3. **End-to-end thật** — không mock backend. Test chạm DB → tự reset giữa các run.
4. **Browser visible (headed mode)** — chạy với UI mở, KHÔNG headless. Xem chuột bay quanh, dễ debug, dễ ghi hình demo. Headless chỉ bật trong CI.
5. **Auto-fix** — khi test fail vì UI drift, agent Healer phải tự cập nhật selector / wait / Page Object.
6. **Best practice production** — Page Object Model, `data-testid`, storageState auth, trace+video+screenshot on failure, HTML report.

### Non-goals

- Không test Python NLP / OTel pipeline riêng (chạm gián tiếp qua app flow).
- Không load-test / performance benchmark.
- Không test mobile / responsive (desktop Chromium-only ở P1; mở rộng sau).
- **Không test các flow phụ thuộc OpenAI API hoặc Python speech analysis trong default run** — xem §3a.

---

## 2. Inventory — what we'll cover

### frontend-user (port 3000)

| Route group | Routes | Test coverage |
|---|---|---|
| `(auth)` | `/login`, `/register`, `/forgot-password`, `/reset-password` | Auth happy path + invalid creds + expired reset token |
| `(public)` | `/furigana`, `/translation` | Public tools, no auth |
| `(app)/vocabulary` | `/`, `/[id]`, `/saved`, `/learning`, `/category`, `/category/[id]`, `/topic`, `/topic/[id]` | List, detail, save, browse by category/topic |
| `(app)/flashcards` | `/`, `/study`, `/stats` | Create flashcard from vocab → study session → rating 1-4 → see stats |
| `(app)/conversation` | `/`, `/[id]`, `/[id]/practice` | Open chat scenario → exchange messages |
| `(app)/speech` | `/` | Speech practice (audio recording — may be limited in CI) |
| `(app)/statistics` | `/` | Personal stats |
| `(app)/account` | `/`, `/change-password` | Profile edit + password change |
| `(app)/profile` | `/` | View profile |

### frontend-admin (port 3001 dev / 3002 docker)

| Area | Routes | Test coverage |
|---|---|---|
| Dashboard | `/` | Render summary cards from merged BE responses |
| Users | `/users`, `/users/[id]` | List, search, role toggle, deactivate/activate |
| Vocabulary | `/vocabulary` | CRUD vocabulary item |
| Categories | `/categories` | CRUD category |
| Topics | `/topics` | CRUD topic |
| Conversations | `/conversations`, `/conversations/[id]`, `/conversations/[id]/edit` | CRUD scenarios |
| Statistics | `/statistics`, `/statistics/users`, `/statistics/users/[id]` | Charts render, per-user drill-down |

**Total**: 21 user routes + 14 admin routes = ~35 routes, target **~70 test cases** across 11 spec files.

---

## 3a. External-service gating — tests that need OpenAI / Python NLP

Một số flow chạm dịch vụ ngoài deterministic kém / không có sẵn local:

| Flow | Phụ thuộc | Mặc định | Test mức nào |
|---|---|---|---|
| `/conversation/[id]/practice` — chat exchange | `ai-service` → OpenAI API | **SKIP** | Chỉ test render UI, mở chat → SKIP step gửi tin nhắn |
| `/translation` (AI translation) | `ai-service` → OpenAI | **SKIP** | Render form + validation, SKIP gửi câu thật |
| `/speech` — pronunciation analysis | `python` FastAPI → SudachiPy | **SKIP** | Render UI + load recorder permission flow, SKIP audio upload |
| `/furigana` | `ai-service` Kuromoji (local lib) hoặc Python | Phụ thuộc — confirm ở P1 | Test nếu chạm chỉ Kuromoji local; SKIP nếu cần Python |
| TTS audio playback (flashcards/vocab) | `ai-service` → OpenAI TTS | **SKIP** assertion về audio thực; test chỉ kiểm UI button |

**Pattern code**:

```ts
// e2e/helpers/feature-flags.ts
export const E2E_FEATURES = {
  openai: process.env.E2E_OPENAI === '1',
  pythonNlp: process.env.E2E_PYTHON_NLP === '1',
} as const

// trong spec
import { E2E_FEATURES } from '@helpers/feature-flags'

test.describe('Conversation practice', () => {
  test('renders chat UI', async ({ page }) => {
    // luôn chạy — chỉ render
  })

  test.skip(!E2E_FEATURES.openai, 'requires OPENAI_API_KEY')(
    'sends message and receives AI reply',
    async ({ page }) => {
      // chỉ chạy khi E2E_OPENAI=1
    }
  )
})
```

**Lợi ích**:
- Default run (CI hoặc local) không bao giờ fail vì OpenAI/Python — gated tests bị skip rõ ràng trong report
- Khi user bật `E2E_OPENAI=1 E2E_PYTHON_NLP=1` thì chạy đầy đủ
- Mỗi spec luôn có **ít nhất 1 test "render-only"** không gated → suite vẫn cover UI render của những trang đó

**Quan trọng**: `ai-service` và `python` vẫn được khởi động bình thường trong `webServer[]` (nếu không thì FE call API sẽ lỗi 502, làm hỏng cả render test). Chỉ là test code không gửi prompt thật. Có thể stub OPENAI_API_KEY thành `sk-test-stub` cho ai-service khởi động không panic — `application.yml` cần fallback null-safe (verify ở P1).

---

## 3. Directory layout (new — at repo root)

```
nihongo-it/
├── e2e/                                  # NEW — top-level test workspace
│   ├── package.json                      # @playwright/test
│   ├── playwright.config.ts              # webServer[] launches all services
│   ├── tsconfig.json
│   ├── .env.example                      # ADMIN_EMAIL/PASSWORD, USER_EMAIL/PASSWORD
│   ├── .gitignore                        # ignores reports/, playwright-report/, test-results/
│   ├── README.md                         # quickstart + agent invocation
│   │
│   ├── specs/                            # Markdown blueprints (Planner output)
│   │   ├── 00-seed.md
│   │   ├── 01-auth-user.md
│   │   ├── 02-vocabulary-user.md
│   │   ├── 03-flashcards.md
│   │   ├── 04-conversation.md
│   │   ├── 05-speech.md
│   │   ├── 06-translation-furigana.md
│   │   ├── 07-account.md
│   │   ├── 08-admin-auth.md
│   │   ├── 09-admin-dashboard.md
│   │   ├── 10-admin-users.md
│   │   ├── 11-admin-vocabulary-categories-topics.md
│   │   ├── 12-admin-conversations.md
│   │   └── 13-admin-statistics.md
│   │
│   ├── tests/                            # Generated .spec.ts (Generator output)
│   │   ├── seed.spec.ts                  # @seed tag — runs once before all
│   │   ├── user/
│   │   │   ├── auth.spec.ts
│   │   │   ├── vocabulary.spec.ts
│   │   │   ├── flashcards.spec.ts
│   │   │   ├── conversation.spec.ts
│   │   │   ├── speech.spec.ts
│   │   │   ├── translation.spec.ts
│   │   │   └── account.spec.ts
│   │   └── admin/
│   │       ├── auth.spec.ts
│   │       ├── dashboard.spec.ts
│   │       ├── users.spec.ts
│   │       ├── vocabulary.spec.ts
│   │       ├── conversations.spec.ts
│   │       └── statistics.spec.ts
│   │
│   ├── fixtures/                         # Playwright fixtures
│   │   ├── auth.fixture.ts               # storageState per role (user / admin)
│   │   ├── api.fixture.ts                # axios with X-User-Id direct (bypass gateway for seed)
│   │   └── db.fixture.ts                 # pg client → cleanup helpers
│   │
│   ├── helpers/
│   │   ├── pages/                        # Page Object Model
│   │   │   ├── BasePage.ts
│   │   │   ├── LoginPage.ts
│   │   │   ├── VocabularyListPage.ts
│   │   │   ├── FlashcardStudyPage.ts
│   │   │   ├── AdminUsersPage.ts
│   │   │   └── ...
│   │   ├── selectors.ts                  # Single source of truth for data-testid
│   │   ├── seed-data.ts                  # Fixed UUIDs + payloads
│   │   └── db-reset.ts                   # TRUNCATE allowed tables
│   │
│   ├── scripts/
│   │   ├── start-stack.ps1               # PS variant (user is on Win 11)
│   │   ├── start-stack.sh                # bash variant
│   │   ├── stop-stack.sh
│   │   └── wait-for.sh                   # poll /actuator/health
│   │
│   └── reports/                          # gitignored
│
├── .claude/skills/
│   └── playwright-e2e/                   # NEW skill
│       └── SKILL.md
│
└── .claude/agents/                       # NEW directory
    ├── playwright-test-planner.md
    ├── playwright-test-generator.md
    └── playwright-test-healer.md
```

**Why a separate top-level `e2e/`** (not inside one of the FE apps):
- Tests span BOTH frontends and the BE — ownership-wise it's its own concern.
- Mirrors how the project keeps `python/`, `services/`, `frontend-*/` as siblings.
- Generator agent writes only into `e2e/` — clean blast radius.

---

## 4. Stack startup model

User stated only Postgres in Docker; everything else local. Two viable patterns:

### Option A — Playwright `webServer[]` orchestrates everything (RECOMMENDED)

`playwright.config.ts`:

```ts
export default defineConfig({
  use: {
    // Headed by default — user wants to see the browser. CI flips to headless via env.
    headless: !!process.env.CI,
    launchOptions: {
      slowMo: process.env.CI ? 0 : 100,   // chậm 100ms/step ở local cho dễ xem
      args: ['--use-fake-device-for-media-stream'], // mic/camera test không hỏi quyền
    },
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  webServer: [
    // Postgres is already started outside Playwright (see Why below)
    { command: 'cd ../services && ./gradlew :eureka-server:bootRun',  port: 8761, reuseExistingServer: true, timeout: 180_000 },
    { command: 'cd ../services && ./gradlew :api-gateway:bootRun',    port: 8080, reuseExistingServer: true, timeout: 180_000 },
    { command: 'cd ../services && ./gradlew :user-service:bootRun',   port: 8086, reuseExistingServer: true, timeout: 180_000 },
    { command: 'cd ../services && ./gradlew :ai-service:bootRun',     port: 8087, reuseExistingServer: true, timeout: 180_000 },
    { command: 'cd ../services && ./gradlew :learning-service:bootRun', port: 8088, reuseExistingServer: true, timeout: 180_000 },
    { command: 'cd ../services && ./gradlew :notification:bootRun',   port: 8089, reuseExistingServer: true, timeout: 180_000 },
    { command: 'cd ../frontend-user && npm run dev',  url: 'http://localhost:3000', reuseExistingServer: true, timeout: 120_000 },
    { command: 'cd ../frontend-admin && npm run dev', url: 'http://localhost:3001', reuseExistingServer: true, timeout: 120_000 },
  ],
  // ...
})
```

**Lưu ý headed mode**:
- Cần X server / Wayland trên Linux; trên Windows/macOS chạy thẳng OK
- `npm test` mở browser thật → đừng chạy nhiều test song song lúc đầu (`workers: 1` cho lần đầu, scale sau)
- CI tự headless: `CI=1 npx playwright test`

**Why postgres is NOT in `webServer`**: Playwright `webServer` polls for a TCP port or HTTP 200 on a URL; both work for postgres. But the *bootstrap step* (creating per-service DBs from `postgres_init.sql`) only runs when the data volume is empty — Playwright can't detect that. Cleaner to keep postgres lifecycle outside test runner.

**Recommended UX**:
```bash
cd e2e
./scripts/start-stack.sh    # boots postgres only — leaves rest to Playwright
npm test                    # Playwright auto-starts everything else
```

### Option B — `start-stack.sh` boots everything; Playwright assumes ready

Simpler from Playwright's POV but harder to debug (one mega-script vs Playwright's per-server logs). **Reject**.

### Decision: **Option A**.

`scripts/start-stack.sh` only does:
```bash
docker compose --env-file ../docker/.env -f ../docker/docker-compose.yaml up -d postgres
./wait-for.sh localhost:5433
```

`stop-stack.sh` does:
```bash
docker compose -f ../docker/docker-compose.yaml stop postgres   # keep data
# Optionally:
# pkill -f 'gradle.*bootRun'
# pkill -f 'next dev'
```

---

## 5. Test data strategy

**Three layers** (avoid order-dependent tests):

### Layer 1 — Static seed (`tests/seed.spec.ts`, tag `@seed`)

Runs **once** before everything else (`globalSetup` or `dependsOn` in projects). Creates:
- 1 admin user (`admin@e2e.test` / `Admin#2026`)
- 1 normal user (`user@e2e.test` / `User#2026`)
- 5 vocabulary items, 2 categories, 2 topics
- 1 conversation scenario template

Uses **direct SQL via `pg` client** (faster than going through the REST API) — `helpers/db-reset.ts` exposes `seed()` and `truncate()`.

### Layer 2 — Per-test data via API fixture

Tests that need data not in static seed call REST endpoints with admin auth — e.g., a test that needs 10 flashcards creates them via `POST /api/v1/learning/flashcards` in `beforeAll`.

### Layer 3 — Cleanup

`globalTeardown` truncates only test-owned tables (NOT `users`/`roles` static seed — those persist for storageState reuse):
- `flashcards`, `review_logs`, `user_progress`, `feedback`, `saved_vocabulary` (learning DB)
- Optionally vocabulary/category/topic if a test wrote them outside seed.

**DO NOT truncate** the static seed users — that breaks the cached `storageState.json` files.

---

## 6. Auth — `storageState` per role

Two storage state files generated by `tests/seed.spec.ts`:

```
e2e/.auth/
├── user.json     # storageState for user@e2e.test
└── admin.json    # storageState for admin@e2e.test
```

Then in `playwright.config.ts` projects:

```ts
projects: [
  { name: 'setup', testMatch: /seed\.spec\.ts/ },
  { name: 'user',
    dependencies: ['setup'],
    use: { storageState: '.auth/user.json', baseURL: 'http://localhost:3000' },
    testMatch: /tests\/user\/.*\.spec\.ts/,
  },
  { name: 'admin',
    dependencies: ['setup'],
    use: { storageState: '.auth/admin.json', baseURL: 'http://localhost:3001' },
    testMatch: /tests\/admin\/.*\.spec\.ts/,
  },
]
```

Cuts login flow from every test → ~30s saved per suite run.

**Token storage**: The app stores access token in **memory** (`@/lib/tokenStore`); refresh token in **httpOnly cookie**. Playwright's `storageState` captures cookies — refresh works. On `beforeEach`, the FE's axios interceptor calls `/auth/refresh` to populate memory token. **Verify this works** during P1 — if not, the alternative is to inject a known JWT directly into local storage / a custom cookie.

---

## 7. Selector strategy

The codebase currently has **zero `data-testid` attributes** (verified: `grep -r data-testid frontend-*/src` returns 0).

### Hierarchy

1. **Role + accessible name** — first choice (`getByRole('button', { name: 'Đăng nhập' })`)
2. **Label / placeholder** — for inputs (`getByLabel('Email')`)
3. **`data-testid`** — fallback for: ambiguous elements, dynamic content (a card in a list), elements without good semantic role

### Convention when `data-testid` is needed

```
data-testid="<area>-<element>[-<modifier>]"

e.g.
data-testid="vocab-list-item"          // each row in vocab list
data-testid="vocab-list-item-save"     // save button inside each row
data-testid="admin-user-row"           // each row in admin users table
data-testid="admin-user-row-deactivate"
```

Centralized in `e2e/helpers/selectors.ts`:

```ts
export const SEL = {
  vocab: {
    listItem: '[data-testid="vocab-list-item"]',
    saveBtn: '[data-testid="vocab-list-item-save"]',
  },
  // ...
} as const
```

### P2 deliverable: add `data-testid` to ~15 critical components

Planner agent identifies elements that are hard to grab by role; Generator agent doesn't add testids itself (out of scope — it just uses what exists). Adding testids is a **human-approved PR** before running the generator at scale.

---

## 8. The three agents — wiring + contract

### Agent 1: `playwright-test-planner`

**Where**: `.claude/agents/playwright-test-planner.md`

**Purpose**: Explore a running app via Playwright MCP browser, then write a Markdown blueprint into `e2e/specs/<feature>.md`.

**Input** (user prompt to subagent):
- A feature name (e.g. `flashcards`)
- A list of routes touched
- Reference to existing spec format (`e2e/specs/00-seed.md` as template)

**Tools**: All `mcp__plugin_playwright_playwright__browser_*`, Read, Write, Glob, Grep.

**Output spec format** (each spec follows this):

```markdown
# 03 — Flashcards (user)

## Pre-conditions
- Seed user `user@e2e.test` logged in (storageState).
- At least 5 vocab items in `vocabulary` table.

## Test cases

### TC-03-01: Create flashcard from vocabulary
- **Route**: `/vocabulary/[id]` → click "Tạo flashcard"
- **Steps**:
  1. Navigate to `/vocabulary` and click first item
  2. Click button "Tạo flashcard"
  3. Wait for toast "Đã tạo flashcard"
- **Assertions**:
  - Toast contains "Đã tạo"
  - `GET /api/v1/learning/flashcards` includes new card

### TC-03-02: Study session — rate cards 1..4
- ...
```

**Process**: Launches browser → logs in as user → walks every interactive element on the feature's routes → records what happens.

### Agent 2: `playwright-test-generator`

**Where**: `.claude/agents/playwright-test-generator.md`

**Purpose**: Convert a `specs/*.md` blueprint into one or more `tests/*.spec.ts` files.

**Input**: path to spec file.

**Tools**: Read, Write, Edit, Glob, Grep. NO browser tools (it doesn't explore — just writes code from the blueprint).

**Constraints baked into agent**:
- MUST import Page Object from `helpers/pages/`. If a needed POM doesn't exist, write it (or stub it) and note it in the agent's report.
- MUST use selectors from `helpers/selectors.ts` — no inline magic strings.
- MUST use the `user` or `admin` Playwright project (declared in spec frontmatter).
- MUST add a `@smoke` tag for one happy path per spec → enables `playwright test --grep @smoke`.
- MUST gate any test that calls OpenAI / Python NLP behind `test.skip(!E2E_FEATURES.openai, ...)` or `!E2E_FEATURES.pythonNlp` (xem §3a). Mỗi spec luôn giữ ít nhất 1 test render-only không gate.

**Output**: TypeScript spec file in `e2e/tests/<area>/<feature>.spec.ts` + a Markdown summary back to caller.

### Agent 3: `playwright-test-healer`

**Where**: `.claude/agents/playwright-test-healer.md`

**Purpose**: When a test fails, decide between "selector/timing drift → auto-fix" and "actual regression → escalate".

**Input**: path to failing test + path to trace zip (`test-results/<test>/trace.zip`).

**Tools**: Bash (run `playwright show-trace`, `playwright test --grep`), all browser MCP tools, Read, Edit, Grep.

**Decision flow** (encoded in the agent prompt):

```
1. Run playwright show-trace <trace.zip>
2. Identify the failing action (last attempted before error)
3. Check the failing selector in DOM (open the trace's "Before" snapshot)
4. If selector is missing → was it renamed / moved?
     → Search codebase for similar testid / text → propose update to helpers/selectors.ts
5. If selector exists but action timed out → check network panel:
     → Stuck request? Likely BE regression — REPORT, don't fix
     → No request? Probably needs explicit await — add waitForResponse / waitForLoadState
6. If selector + timing OK but assertion failed → REGRESSION
     → Write a short report (what changed in BE/FE), don't touch the test
7. After any fix, re-run only the affected spec; iterate up to 3 times
```

**Report format**: short paragraph + diff of what it changed (or refused to change).

---

## 9. Playwright MCP usage — already installed

The Claude Code plugin Playwright MCP is already loaded:

```
mcp__plugin_playwright_playwright__browser_navigate
mcp__plugin_playwright_playwright__browser_snapshot
mcp__plugin_playwright_playwright__browser_click
mcp__plugin_playwright_playwright__browser_fill_form
mcp__plugin_playwright_playwright__browser_evaluate
mcp__plugin_playwright_playwright__browser_network_requests
... (~25 tools)
```

**Use cases**:
- **Planner**: `browser_navigate` → `browser_snapshot` → `browser_click` → record what happens. Snapshot is preferred over `take_screenshot` because it's accessibility-tree text (cheap, structured, perfect for LLMs to plan against).
- **Healer**: same set + `browser_network_requests` to inspect failed XHR.

**Generator does NOT use MCP** — it's pure code generation from spec + existing patterns.

---

## 10. Skill — orchestrator

`.claude/skills/playwright-e2e/SKILL.md` — entry point users invoke when working on E2E.

Sketch:

```markdown
---
name: playwright-e2e
description: Use when adding, debugging, or running Playwright end-to-end tests for Nihongo IT.
  Establishes stack startup, three-agent flow (Planner → Generator → Healer), seed/auth fixtures,
  and the postgres-only-in-docker constraint.
---

## When to use

- Writing a new test for a feature → spawn `playwright-test-planner` first
- Converting a spec to runnable test → spawn `playwright-test-generator`
- Test failed → spawn `playwright-test-healer` with the failing test path + trace

## Stack startup

1. `cd e2e && ./scripts/start-stack.sh` — boots ONLY postgres in docker
2. `npm test` — Playwright `webServer[]` brings up 6 BE services + 2 FE apps; ~3 min cold start, ~30s warm (reuseExistingServer:true)

## Key conventions

- Static seed users (`user@e2e.test`, `admin@e2e.test`) — DO NOT delete from `users`/`roles`
- Selectors: role+name first, `data-testid` for ambiguous
- Page Objects in `helpers/pages/<Feature>Page.ts`
- One `@smoke` tag per spec → `npm run test:smoke` filters
- Trace+video+screenshot on failure (configured in playwright.config.ts)

## Three-agent flow

[diagram]

## Commands

| Goal | Command |
|---|---|
| Full suite | `npm test` |
| Smoke only | `npm run test:smoke` |
| Watch a spec | `npm run test:watch -- tests/user/auth.spec.ts` |
| Show last trace | `npm run trace` |
| Update snapshots | `npm test -- --update-snapshots` |
| UI mode (debugger) | `npm run test:ui` |
```

---

## 11. CI integration

**Don't** run E2E per-PR — too heavy (~10 min). Patterns:

| Trigger | Suite | Where |
|---|---|---|
| Nightly | Full | GitHub Actions cron |
| Manual `workflow_dispatch` | Full | Slack-triggered or button |
| Per-PR | `@smoke` only | GitHub Actions — ~2 min, blocking |

`.github/workflows/e2e.yml`:
- `services: postgres:16` (GitHub Actions service container — re-uses our `postgres_init.sql`)
- `./gradlew bootJar` for all BE services, run jars with `nohup`
- `npm run build && npm start` for both FE apps
- `npx playwright test`
- Upload `playwright-report/` + `test-results/` as artifacts on failure

---

## 12. Phased rollout

| Phase | Scope | Deliverables | Effort |
|---|---|---|---|
| **P1** | Scaffolding | `e2e/` skeleton, `playwright.config.ts` with webServer[], `start-stack.sh`, seed.spec.ts, auth.fixture, db.fixture, `BasePage`, README | 1-2 days |
| **P2** | Selectors + POM baseline | Add `data-testid` to ~15 critical components in both FE apps. Write `helpers/selectors.ts`. Write 4 page objects (`LoginPage`, `VocabularyListPage`, `FlashcardStudyPage`, `AdminUsersPage`) | 1 day |
| **P3** | Skills + Agents | `.claude/skills/playwright-e2e/SKILL.md` + 3 agent files. Verify each agent in isolation against one route | 0.5 day |
| **P4** | Planner pass | Run Planner against all 13 feature areas → produce all `specs/*.md` blueprints | 0.5-1 day (mostly agent runtime) |
| **P5** | Generator pass | Run Generator on each spec → produce `tests/*.spec.ts`. Hand-review each before commit | 1-2 days |
| **P6** | Stabilize + Healer | Run full suite; for each failure, spawn Healer; iterate until green or escalate true regressions | 1-2 days |
| **P7** | CI | `.github/workflows/e2e.yml` smoke + nightly | 0.5 day |
| **P8** | Docs | Update root README + CLAUDE.md to point at the new skill | 0.25 day |

**Total**: ~7-10 days realistic.

---

## 13. Risk register

| Risk | Mitigation |
|---|---|
| `gradle bootRun` cold start exceeds Playwright's 180s timeout | Pre-build with `./gradlew bootJar` and launch via `java -jar` in `webServer[]` — ~5x faster |
| Tests pollute DB across runs | `globalTeardown` truncates owned tables; static seed users protected |
| OAuth Google login can't be tested deterministically | Skip Google OAuth in E2E; cover email/password only |
| Speech recording needs mic permission | Use Chromium fake media stream flag: `--use-fake-device-for-media-stream` |
| Healer auto-fixes a real regression by mistake | Healer prompt forbids editing assertions — only selectors / waits. Anything else escalates |
| `data-testid` PR is large (touches both FE apps) | Land in 2 PRs: user-app testids + admin-app testids. Each ~150 lines |
| Flaky test from Eureka registration timing | `wait-for.sh` polls gateway's `/actuator/health` returning UP — gateway only reports UP once it has Eureka peers |
| Memory token (in `@/lib/tokenStore`) not in storageState | Verify refresh flow at P1; if it breaks, inject token via `page.evaluate(() => window.localStorage.setItem(...))` shim |

---

## 14. Acceptance criteria

- [ ] `cd e2e && ./scripts/start-stack.sh && npm test` runs green on a clean checkout in ≤ 12 min, **browser visible (headed mode)**
- [ ] OpenAI / Python NLP tests SKIP cleanly (không fail) khi env flag tắt; chạy đầy đủ khi `E2E_OPENAI=1 E2E_PYTHON_NLP=1`
- [ ] Mỗi spec touching AI/Python vẫn có ≥1 test render-only PASS không gate
- [ ] `npm run test:smoke` runs in ≤ 3 min
- [ ] HTML report at `e2e/playwright-report/index.html` shows all 13 feature areas
- [ ] Healer demonstration: revert a known data-testid in FE → Healer detects + proposes fix → re-run green
- [ ] CI: smoke job runs on each PR (headless, AI flags off); nightly full job runs at 03:00 ICT
- [ ] CLAUDE.md updated with E2E section + pointer to `playwright-e2e` skill

---

## 15. Concrete next step

After approval of this plan, **start with P1** (~1-2 days) which establishes the skeleton. P1 deliverables alone provide value (skeleton + seed + auth fixture + 1 smoke test) — if the project pauses there, the work isn't wasted.

I do NOT recommend doing P1-P8 in one session — each phase has a clear stopping point and benefits from human review of the output before the next phase starts.
