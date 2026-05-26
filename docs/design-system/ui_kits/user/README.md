# User UI Kit — Nihongo IT

Click-through recreation of the learner-facing app (`frontend-user`). Opens on the login screen; "Đăng nhập" lands you in the vocabulary browser. From the header you can navigate to **Từ vựng**, **Hội thoại**, **Flashcards**, **Thống kê**. Clicking a vocab card opens its detail view.

## Files

```
ui-primitives.jsx       — Button, Input, Card, Badge, Avatar, Icon set (inline Lucide-style SVGs)
mock-data.jsx           — IT-Japanese vocabulary + conversation samples
Header.jsx              — Sticky top nav with avatar dropdown
LoginScreen.jsx         — Email + password form (any password ≥ 6 chars works)
VocabularyScreen.jsx    — Grid + filters (keyword, JLPT, topic)
VocabularyDetail.jsx    — Single word — furigana, examples, AI helper card
FlashcardScreen.jsx     — Flip-card SRS session with 4-way rating
ConversationScreen.jsx  — Practice list, JLPT filter, search
StatisticsScreen.jsx    — Streak banner, stat cards, JLPT distribution bars
kit.css                 — Cosmetic styles for the primitives above
index.html              — Mounts everything in one App
```

## What it covers

- Auth screen (login)
- Authenticated shell (sticky header with brand mark, nav, notification bell, avatar dropdown)
- Vocabulary browse + detail flow (with `<ruby>` furigana)
- Flashcard study with 3D flip + spaced-repetition rating
- Conversation list (filterable)
- Statistics view with streak and per-JLPT progress bars

## What it skips on purpose

- Real auth, real data fetch, real audio. Everything's mocked.
- Speech-analysis client (`SpeechAnalyzerClient.tsx`) — too much app-specific behaviour for a kit; the UI patterns it uses (mic button, feedback chips) are covered by primitives.
- The furigana tool and translation tool (`(public)/furigana`, `(public)/translation`) — light-weight forms that re-use the Input + Button + Card primitives.
- Pagination, change-password, reset-password flows — same form patterns.

## Keyboard

- **Space / Enter** flips the current flashcard.
- **1 / 2 / 3 / 4** rate the flipped flashcard (Quên / Khó / Tốt / Dễ).
