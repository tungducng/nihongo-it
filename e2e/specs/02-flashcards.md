# 02 — Flashcards (user app)

> **Project**: `user`
> **External services**: `none`

## Pre-conditions

- `setup` project has run.

## Test cases

### TC-02-01: Flashcard stats page renders (@smoke)

- **Route**: `/flashcards/stats`
- **Steps**: Navigate.
- **Assertions**: URL not `/login`; heading "Thống kê học tập" visible.

### TC-02-02: Flashcard study page renders

- **Route**: `/flashcards/study`
- **Steps**: Navigate.
- **Assertions**: URL not `/login`; heading "Học thẻ ghi nhớ" visible (or empty-state if no due cards).

## Notes for the Generator

- File: `e2e/tests/user/flashcards.spec.ts`. user-project.
