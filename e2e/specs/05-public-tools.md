# 05 — Public tools (user app)

> **Project**: `user` (route is public — uses user-app baseURL, no auth required)
> **External services**: `openai` (translation submit gated)

## Pre-conditions

- `setup` project has run (just to share Playwright context conventions).
- Routes are listed in `frontend-user/proxy.ts` PUBLIC_PATHS so they bypass
  the cookie gate.

## Test cases

### TC-05-01: Furigana tool renders (@smoke)

- **Route**: `/furigana`
- **Assertions**: heading "日本語ふりがな生成器" visible.

### TC-05-02: Translation tool renders

- **Route**: `/translation`
- **Assertions**: heading "Công cụ dịch thuật" visible.
- **Gated**: actual translation submit requires `E2E_OPENAI=1` — not exercised
  in this spec.

## Notes for the Generator

- File: `e2e/tests/user/public-tools.spec.ts`. user-project (baseURL = 3000).
