# 01 — Vocabulary (user app)

> **Project**: `user`
> **External services**: `none`

## Pre-conditions

- `setup` project has run.
- Vocabulary table has at least the V2 seed data (Flyway migration loads default Japanese vocabulary).

## Test cases

### TC-01-01: Vocabulary list page is reachable (@smoke)

- **Route**: `/vocabulary`
- **Steps**:
  1. Navigate to `/vocabulary`.
- **Assertions**:
  - URL is NOT redirected to `/login` (within 15s).
  - Heading "Từ vựng" is visible (within 30s — protected route requires FE refresh roundtrip).

### TC-01-02: Saved vocabulary URL is reachable

- **Route**: `/vocabulary/saved`
- **Steps**:
  1. Navigate to `/vocabulary/saved`.
- **Assertions**:
  - URL is NOT `/login` (within 15s).
- **Known limitation**: Heading-visibility assertion is intentionally NOT included.
  The page issues an unauthenticated XHR before the FE refresh roundtrip
  completes, the BE responds 401, and the axios interceptor's full-page
  redirect race intermittently lands on `/login`. Tracking under the FE
  auth-bootstrap bug (see api.ts comment).

### TC-01-03: Learning-by-topic page renders

- **Route**: `/vocabulary/learning`
- **Steps**:
  1. Navigate to `/vocabulary/learning`.
- **Assertions**:
  - URL is NOT `/login`.
  - Heading "Học theo chủ đề" is visible.

### TC-01-04: Vocabulary URL remains accessible after refresh

- **Route**: `/vocabulary`
- **Steps**:
  1. Navigate to `/vocabulary`.
  2. Reload the page.
- **Assertions**:
  - After reload, URL is NOT `/login` (within 15s).
- **Known limitation**: Same as TC-01-02 — heading assertion omitted on
  the post-reload check because the second hydration races with stored
  cookies.

## Notes for the Generator

- File: `e2e/tests/user/vocabulary.spec.ts` (overwrites existing smoke).
- All cases run under the `user` project — uses `.auth/user.json` storageState.
- No new `data-testid` needed — all assertions use `getByRole('heading', { name, exact: true })`.
- 30s timeout on the @smoke heading visibility because the protected-route first-paint requires an `/auth/refresh-token` roundtrip via the FE's axios interceptor.
