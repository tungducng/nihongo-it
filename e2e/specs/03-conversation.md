# 03 — Conversation (user app)

> **Project**: `user`
> **External services**: `openai` (chat exchange gated)

## Pre-conditions

- `setup` project has run.

## Test cases

### TC-03-01: Conversation list renders (@smoke)

- **Route**: `/conversation`
- **Steps**: Navigate.
- **Assertions**: URL not `/login`; heading "Hội thoại" visible.

## Notes for the Generator

- File: `e2e/tests/user/conversation.spec.ts`. user-project. No gated test in this
  batch — chat exchange will be added when there are seeded scenarios.
