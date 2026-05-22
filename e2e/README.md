# Nihongo IT — E2E Playwright suite

End-to-end tests covering the user app (`localhost:3000`), the admin app
(`localhost:3001`), and the backend stack reachable via API Gateway
(`localhost:8080`).

See `docs/plans/2026-05-21-playwright-e2e-plan.md` for the full plan.

## What's in scope

- **Real** backend — no mocks. Tests boot the full Spring Boot stack +
  Postgres and exercise the actual REST endpoints.
- **Browser visible by default** (headed mode). CI flips to headless via
  the `CI` env var.
- **3-agent assist** (planned for P3+): a Planner agent walks the UI and
  writes Markdown blueprints to `specs/`, a Generator agent turns those
  into `.spec.ts` files in `tests/`, and a Healer agent auto-fixes
  selector/timing drift when tests fail.

## What's out of scope

- Flows that call **OpenAI** (`/conversation` chat, `/translation` AI)
  or **Python NLP** (`/speech`) are **gated** behind env flags:
  - `E2E_OPENAI=1` enables OpenAI-dependent tests.
  - `E2E_PYTHON_NLP=1` enables Python-dependent tests.
  Default = OFF, so a vanilla `npm test` never fails on missing keys.

## Quickstart

```bash
# one-time
cp .env.example .env
npm install
npx playwright install chromium

# boot the full stack (postgres docker + 6 BE services + 2 FE apps)
# — ordered Eureka-first, then services in parallel, then frontends
./scripts/start-stack.ps1               # default
./scripts/start-stack.ps1 -SkipFE       # if you only need the BE for API tests

# run the suite — stack must already be UP
npm test

# stop everything when done
./scripts/stop-stack.ps1
```

First run is slow (~3 min cold start for 6 BE services + 2 FE apps).
Subsequent runs reuse the stack — leave it running and just re-run `npm test`.

**Why the stack is started outside Playwright** (not via `webServer[]`):
Playwright launches webServers in parallel with no ordering. Services that
register with Eureka can fail bean creation if Eureka isn't up yet. The
PowerShell script enforces *Eureka first → wait UP → then everything else*.

## Useful commands

| What | How |
|---|---|
| Full suite | `npm test` |
| Smoke only | `npm run test:smoke` |
| User app project only | `npm run test:user` |
| Admin app project only | `npm run test:admin` |
| UI mode (best for debugging) | `npm run test:ui` |
| Headed (already default locally) | `npm run test:headed` |
| Step-through debugger | `npm run test:debug` |
| Open last HTML report | `npm run report` |
| View a trace | `npm run trace test-results/<...>/trace.zip` |
| Enable OpenAI-gated tests | `E2E_OPENAI=1 npm test` |
| Enable Python-gated tests | `E2E_PYTHON_NLP=1 npm test` |

## Directory map

```
e2e/
├── specs/        # Markdown blueprints (Planner output — populated in P3)
├── tests/        # Generated .spec.ts files
│   ├── seed.spec.ts        # idempotent: creates the 2 test users + storageState
│   ├── user/               # user-app project
│   └── admin/              # admin-app project
├── fixtures/     # Playwright fixtures (auth + api + db)
├── helpers/
│   ├── pages/    # Page Object Model
│   ├── selectors.ts        # data-testid catalog (centralized)
│   ├── feature-flags.ts    # E2E_OPENAI, E2E_PYTHON_NLP gating
│   ├── api.ts              # axios wrapper for direct REST calls
│   └── db.ts               # pg client + cleanup utilities
├── scripts/      # start-stack / stop-stack
└── .auth/        # storageState files (gitignored)
```

## Test users

`seed.spec.ts` creates these two accounts idempotently on every run:

| Role  | Email             | Password    |
|-------|-------------------|-------------|
| user  | `user@e2e.test`   | `User#2026` |
| admin | `admin@e2e.test`  | `Admin#2026`|

After signup, the seed step calls SQL directly to:
- mark `is_email_verified = true`
- (admin only) promote `role_id` to `ROLE_ADMIN`

The seed users are NEVER truncated. Test-owned data (flashcards, review
logs, saved vocabulary, etc.) is truncated on demand via
`helpers/db.ts → truncateLearningOwned()`.

## Conventions

- **Selectors**: prefer `getByRole(...)` / `getByLabel(...)`. Fall back to
  `data-testid` only when the target has no good semantic anchor. Centralize
  every testid in `helpers/selectors.ts`.
- **POMs**: one file per page in `helpers/pages/`. Extend `BasePage`.
- **Smoke tag**: mark one happy-path test per spec with `@smoke`.
- **Gated tests**: anything that calls OpenAI / Python must wrap the
  assertion in `test.skip(!E2E_FEATURES.openai, '...')`. Every spec keeps
  at least one render-only test ungated.

## Troubleshooting

- **"connection refused on 5433"** — Postgres not running. Run
  `./scripts/start-stack.ps1`.
- **A `webServer` times out** — increase the per-server `timeout` in
  `playwright.config.ts`, or pre-build with `./gradlew bootJar` and switch
  the launch command to `java -jar ...` for ~5x faster startup.
- **`storageState` file missing** — the `setup` project failed. Run
  `npx playwright test --project=setup` alone to inspect the seed error.
- **OpenAI / Python feature test fails despite `E2E_*=0`** — the gating
  is missing. File the test for Healer or add the skip manually.
