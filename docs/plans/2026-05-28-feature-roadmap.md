# Feature Roadmap — Nihongo IT

**Date:** 2026-05-28
**Status:** Living document — pick items off it, don't execute top-to-bottom
**Purpose:** Single backlog of candidate features, sized + sequenced, so the next
session can start without re-deriving options.

---

## 0. What's already shipped

| ID | Feature | Commit(s) |
|---|---|---|
| F1 | Today Dashboard (`/dashboard`) — due cards, streak strip, suggested vocab, conversation-of-day, 30-day heatmap; login redirect fixed | `bf28ed6`, `65e0e37` |
| S4 | Vocab comment threads — depth-1 replies, like, edit, soft-delete, sort newest/top, pagination | `97cd5ed`, `57dd931`, `be64dad` |
| S5 | Reply notification — cross-service push (learning → notification) with internal API key | `aacdaa1`, `c2697f7` |
| — | Design system "Wabi" (Ai-iro + Shu-iro + Washi) adopted across both apps | `f4d7e59`, `8c443aa`, `cc090f0`, `632b160` |
| — | Package rename `com.example` → `io.github.ndtung723.nihongoit` | `007371f` |

E2E baseline: **52 passed / 2 skipped** (gated AI). All CI green.

---

## 1. Sequencing logic

Three tracks run somewhat independently:

- **Engagement** — keep daily-active users coming back (streak, quests, reminders)
- **Learning depth** — more effective study modes (type-answer, dictation, cloze)
- **Moat** — features competitors can't trivially clone (AI tutor, IT-domain content)

Within a track, prefer **frontend-only** items first (no migration, faster to ship,
no schema risk), then items needing a BE endpoint, then cross-service / AI ones.

Each item below is tagged: **size** (S ≤ 1d, M = 2-3d, L = 5-7d), **deps**, **track**.

---

## 2. Tier 1 — Quick wins (frontend-heavy, plan-ready)

### F2 · Streak freeze + grace period · S · engagement
- **Problem:** missing one day wipes the whole streak → demoralises, users quit.
- **Build:**
  - BE: `user_progress` gains `freezes_available INT DEFAULT 2`, `freezes_reset_at`.
    Migration + a daily/lazy "refill 2 per month" rule. Endpoint
    `POST /users/me/streak/freeze` (claim) — but mostly auto-claim on miss.
  - FE: dashboard streak card shows ❄ count; a frozen day renders with the
    snowflake cell already wired in `DashboardClient.StreakStrip`.
- **Done when:** miss a day with a freeze available → streak survives, freeze count
  decrements; dashboard shows remaining freezes.
- **Success signal:** day-7 retention; avg streak length.

### F4 · Audio speed + auto-replay · S · learning depth
- **Problem:** TTS at 1× is too fast for beginners; they re-tap repeatedly.
- **Build:** FE-only. Speed slider 0.5×–1.5× on the flashcard + vocab-detail audio
  buttons; auto-replay N times on flip (configurable); persist to localStorage +
  optionally UserProgress. Keyboard `R` replay, `[`/`]` speed.
- **Done when:** speed persists across sessions; flip auto-plays.

### F5 · Furigana 3-mode toggle · S · learning depth + JP-specific
- **Problem:** all-or-nothing furigana — advanced users see noise, beginners see gaps.
- **Build:** Profile setting `furiganaMode: off | smart | always`. `smart` shows
  furigana only on kanji above the user's `currentLevel`. Render via `<ruby>` (CSS
  already in globals). Reading data comes from the existing Python NLP furigana
  endpoint; cache per vocab.
- **Done when:** setting changes furigana rendering on vocab detail + flashcards.

### J1 · Romaji toggle · S · JP-specific
- **Problem:** absolute beginners can't read kana yet.
- **Build:** global toggle (default off). When on, show romaji under kana/kanji on
  vocab cards + flashcard fronts. Use wanakana (kana→romaji) client-side.

### H5 · Dark mode · S · QoL
- **Problem:** no dark theme although `.dark` tokens already exist in globals.css.
- **Build:** theme switcher in Header (sun/moon), persist to localStorage, respect
  `prefers-color-scheme` on first load. Tokens are done — this is wiring + a
  `<ThemeProvider>` (or a tiny Zustand store + `documentElement.classList`).

### C1 · Daily phrase widget · S · engagement + content
- **Problem:** dashboard could teach something passively each visit.
- **Build:** one card on `/dashboard` showing a daily IT-Japanese phrase + audio +
  meaning. BE: `GET /learning/phrase-of-day` (deterministic by date hash over a
  curated table, or reuse a random vocab with an example sentence).

---

## 3. Tier 2 — Learning modes (new study mechanics)

### F3 · Type-to-answer flashcard mode · M · learning depth
- **Problem:** reveal-and-rate is passive; self-rating is gameable.
- **Build:** new study mode alongside reveal. User types the meaning/kana/romaji;
  fuzzy matcher (Hiragana↔Katakana, romaji via wanakana, VN synonym tolerance,
  edit-distance ≤1 = "close") → auto FSRS rating (exact=Good, close=Hard, wrong=Again).
  Toggle in study session header; persist preference.
- **Risk:** fuzzy matcher tuning. A/B retention vs reveal mode after 4 weeks.

### L1 · Match pairs · S · learning depth
- **Problem:** flashcards get monotonous.
- **Build:** mini-game — 8 JP terms + 8 VN meanings shuffled in two columns, tap to
  match. Pure FE, sources from due cards or a topic. Timer + accuracy score feeds XP
  (see G2). No BE change beyond reading existing vocab.

### L2 · Sentence builder · M · learning depth
- **Problem:** knowing words ≠ producing sentences.
- **Build:** show a JP sentence's VN translation + the JP word-chunks shuffled; user
  drags chunks into correct order. Needs a small seeded sentence corpus (reuse
  conversation lines, or curate ~50). `@dnd-kit` already in deps.

### L3 · Listening dictation · M · learning depth
- **Problem:** listening is the weakest skill for engineers self-studying from books.
- **Build:** play TTS of a word/sentence → user types what they hear (kana or kanji) →
  graded with the F3 fuzzy matcher. New route `/listening` or a mode inside study.

### F8 / L5 · Cloze in real IT context · M · learning depth + moat
- **Problem:** isolated words don't transfer to reading real docs.
- **Build:** curate 100–200 short JP passages (Qiita / Zenn / SO-JP, CC-checked).
  AI (ai-service) generates cloze blanks at the user's N+1 level. New `/reading` route.
- **Deps:** ai-service unblock (P9.4) + a corpus table + crawl/curation step.

---

## 4. Tier 3 — AI tutor (moat; needs ai-service)

> All of these depend on **P9.4 — Spring AI 1.0 → SB4 compat** (ai-service is
> currently gated out of the default stack). That unblock is the prerequisite epic.

### AI1 · AI chat freeform · M
- Ask anything about JP grammar/usage ("Sao 〜てしまう khác 〜ちゃう?"). The
  vocab-detail page already has an `AIChat` shell — generalise it into a standalone
  `/tutor` route with conversation memory.

### AI2 · AI roleplay scenarios · L
- 5 built-in scenarios (daily standup, code review, sprint planning, incident email,
  job interview), each with a persona. User replies by voice or text; end-of-session
  feedback on vocab/grammar. Biggest differentiator for the IT-Japanese niche.

### AI3 · AI mnemonic generator · M
- Per-kanji "🧠 Mnemonic" button → AI generates a personalised mnemonic referencing
  kanji the user already knows. Saved onto the card, shown on each review.

### AI4 · AI writing grader · M
- User writes a JP email/paragraph → AI feedback on grammar + tone (keigo level).

---

## 5. Tier 4 — Gamification (engineer-friendly, not Duolingo-loud)

### G2 · XP + Daily quests · M · engagement
- 3 rotating daily quests ("Học 20 thẻ", "Hoàn 1 hội thoại", "Đạt 80% chính xác") →
  XP. Builds directly on F1 dashboard. BE: `daily_quest` + `user_xp` tables, a
  reset-at-midnight job. The retention workhorse.

### G3 · Achievement badges · S · engagement
- "N3 Wordsmith" (100 N3 words mastered), "Night Owl", "Code Reviewer" (finished the
  code-review roleplay). Derived from existing FSRS + event data; mostly a query +
  a badges grid on the profile.

### G1 · Weekly league / leaderboard · L · engagement + social
- 30 random users per cohort, Bronze→Diamond, resets Sunday. Needs an XP source
  (G2 first), a cohort-assignment job, and a leaderboard endpoint. Heaviest
  gamification item — defer until G2 proves XP drives behaviour.

---

## 6. Tier 5 — Social & content (bigger bets)

| ID | Feature | Size | Note |
|---|---|---|---|
| S1 | Friend list + streak nudge | M | add friend, see their streak, send 👏 |
| S3 | Shared public decks + clone | M | Anki-style; user-created decks become assets |
| F13 | Document → personal deck | M | upload spec/email JP → SudachiPy extracts unknown vocab |
| C2 | Reading library (Qiita rút gọn, click-to-translate) | M | overlaps F8 corpus |
| F7 | Pronunciation drill scored | M | Python NLP mora-by-mora compare, top-10 personal errors |
| F11 | Team workspace (B2B) | L | company shares decks, internal leaderboard — monetisation path |
| F15 | Offline PWA | M | service-worker cache + IndexedDB sync for commute study |

---

## 7. Engineering debt / enablers (not user-facing, but unblock the above)

| ID | Item | Size | Unblocks |
|---|---|---|---|
| P9.4 | Spring AI 1.0 → SB4 compat; bring ai-service into default stack | M | AI1–AI4, F8 |
| EN1 | Self-host minimal `app_event` table + tracking middleware (login, study_start/end, card_reviewed) | S | every "success signal" metric; G1/G2 |
| EN2 | Notification preferences UI (`/account/notifications`) — let users mute COMMENT_REPLY etc. | S | S5 follow-up; required before more notification types |
| EN3 | Comment feature flag rollout note already in plan — verify `app.comments.enabled` wired | XS | S4 ops safety |

---

## 8. Recommended next 2-sprint slice

If continuing solo, highest impact-per-effort:

1. **F2 Streak freeze** (S) — finishes the engagement loop F1 started.
2. **H5 Dark mode** (S) — tokens already exist, pure wiring, visible polish.
3. **F3 Type-to-answer** (M) — the single biggest learning-quality upgrade.
4. **EN1 app_event tracking** (S) — without it, none of the "success signals" are measurable.
5. **G2 XP + Daily quests** (M) — turns the dashboard into a daily habit.

Then evaluate metrics from EN1 before committing to Tier 3/4 heavy items.

---

## 9. Conventions reminder (so any of these ships cleanly)

- BE: new entity → migration with `_at` **and** `_by` audit columns; explicit
  `@Column(nullable = false)` on non-null Kotlin primitives; run
  `./gradlew :<svc>:ktlintFormat` before commit.
- FE: `{name}.service.ts`, types in `*.types.ts`, `useAppToast`, `extractApiError`,
  `useConfirm`; run `npm run lint` before commit.
- Each feature: plan note (if non-trivial) → BE+migration → FE → E2E spec → screenshot
  → commit per layer. Keep the 52-test E2E baseline green.
- Format before every commit (memory: `feedback-format-before-commit`).
