# Nihongo IT — UX & Engagement Improvement Plan

**Date:** 2026-05-26
**Status:** Draft — chờ approve trước khi land bất kỳ phase nào
**Audience:** Engineer-led product team (1-2 FE, 1 BE, optional 1 designer)
**Owner:** ndtung723

---

## 0. Bối cảnh

Sau 8 sprint feature work (Sprint P1-P8) và 1 sprint tech-debt (P9), app đã có **đầy đủ feature core**: vocabulary CRUD, flashcards với FSRS, conversation, pronunciation analysis (Python NLP), TTS, statistics. 50 E2E test green, build clean.

**Vấn đề thật:** App là một **collection of features**, không phải **product có cá tính**. User logged-in xong không biết bắt đầu từ đâu, UI thuần shadcn grayscale, không có brand identity, không có hook giữ chân ngoài raw functionality.

Plan này tập trung **3 trục đồng thời**:
1. **Design system + brand** — biến app từ shadcn-default thành "Nihongo IT"
2. **Engagement loop** — Today dashboard, streak smart, daily plan, smart reminder
3. **Active learning** — type-to-answer, AI roleplay, cloze trong IT context, voice conversation

Đo bằng metric thật (retention, session length, reviews/day), không phải feature count.

---

## 1. Non-goals

- ❌ Không build native mobile app (iOS/Android). PWA installable là enough cho phase này
- ❌ Không refactor backend microservice boundary
- ❌ Không thay Next.js / React / Tailwind / shadcn. Stack hiện tại OK
- ❌ Không add internationalization (multi-language UI). Vietnamese only như hiện tại
- ❌ Không touch admin app trong plan này — admin đã đủ ổn cho ops

---

## 2. Audit hiện tại (khảo sát code, 2026-05-26)

### 2.1 Routes user-app — 21 page

```
(public)/   → /, /furigana, /translation
(auth)/     → /login, /register, /forgot-password, /reset-password
(app)/      → /vocabulary (+ category, topic, [id], learning, saved)
            → /flashcards/study, /flashcards/stats
            → /conversation, /conversation/[id]/practice
            → /speech, /statistics
            → /profile, /account/change-password
```

**Gap:** Không có `(app)/page.tsx` → authenticated home dashboard không tồn tại.

### 2.2 Post-login UX dead-end

`frontend-user/src/app/(auth)/login/LoginForm.tsx:22`
```ts
const redirect = searchParams.get('redirect') || '/'
```

Login xong redirect tới `/` = `(public)/page.tsx` — marketing page với 2 button Login/Register. **User vừa login xong lại nhìn thấy nút Login.** Đây là pain point lớn nhất.

### 2.3 Design system = shadcn default

`frontend-user/src/app/globals.css:60-80`
```css
--primary: oklch(0.205 0 0);       /* dark gray */
--secondary: oklch(0.97 0 0);      /* light gray */
--chart-1: oklch(0.87 0 0);        /* gray */
--chart-2 ... chart-5: all gray
```

Không có brand color, không có Japanese font, charts tất cả grayscale.

### 2.4 Header chỉ text

`frontend-user/src/components/layout/Header.tsx:54`
```tsx
<Link href="/" className="text-primary text-lg font-semibold">
  Nihongo IT
</Link>
```

Plain text logo, 4 nav link, dropdown profile. Không có visual hierarchy, không streak indicator, không daily progress bar.

### 2.5 Stores chỉ có 3

`auth.store.ts`, `flashcards.store.ts`, `vocabulary.store.ts`. Thiếu:
- `user-progress.store` — streak, daily goal, points (đã có entity backend, FE chưa dùng)
- `notification.store` — bell icon đã có, dropdown render qua local fetch, không cache cross-page

### 2.6 Hooks usable cho engagement

✅ `useAppToast`, `useAsyncData`, `useAudioRecorder`, `useConfirm`, `useDebounce`, `usePagination`

Thiếu: `useStreak`, `useDailyPlan`, `useKeyboardShortcuts` (cần cho power user flashcard study).

### 2.7 Backend đã sẵn sàng tận dụng

`UserProgressEntity` đã có: `streakCount`, `lastStudyDate`, `points`, `dailyGoalMinutes` — FE chưa hiển thị/leverage.

`ai-service` có chat + TTS — đang **gated, không vào stack default** (P9.4). Cần unblock để làm AI roleplay.

`NotificationEntity` có `type=STUDY_REMINDER, REVIEW_DUE, SYSTEM_ANNOUNCEMENT` — bell hiện chỉ render list, chưa có call-to-action sâu.

---

## 3. Design System Overhaul

### 3.1 Brand identity

**Đề xuất tên hệ thống:** *"Wabi"* design system (từ 侘び — Japanese aesthetic: tinh tế, có khoảng trống, có chi tiết duy nhất).

**Color palette** — chọn 1 trong 2 hướng:

#### Option A: Indigo & Sakura (modern tech-Japanese)
```css
--primary:   oklch(0.55 0.18 264);   /* deep indigo #4F46E5-ish */
--accent:    oklch(0.78 0.13 15);    /* sakura pink */
--success:   oklch(0.72 0.16 145);   /* matcha green */
--warning:   oklch(0.78 0.16 75);    /* yamabuki yellow */
--destructive: oklch(0.60 0.22 25);  /* shu (vermilion) */
```
Modern, web-tech feel. Indigo gợi engineering, sakura accent tạo nét Nhật mà không cliché.

#### Option B: Sumi & Shu (traditional ink-and-vermilion)
```css
--primary:   oklch(0.20 0 0);         /* sumi (墨) - black ink */
--accent:    oklch(0.55 0.22 25);     /* shu (朱) - vermilion */
--success:   oklch(0.55 0.15 145);    /* matcha */
--warning:   oklch(0.75 0.16 75);     /* yamabuki */
```
Tối giản, sang trọng, gợi calligraphy. Risk: trông quá nghiêm túc cho engineer 25-30t.

**Recommendation: Option A.** Engineer-friendly, modern, vẫn có Japanese cue mà không stiff.

### 3.2 Typography

```css
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+JP:wght@400;500;600;700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

--font-sans: 'Inter', sans-serif;
--font-jp:   'Noto Sans JP', sans-serif;     /* dùng cho kanji/kana */
--font-mono: 'JetBrains Mono', monospace;    /* code blocks */
```

**Convention:** Wrap kanji/kana trong `<span class="font-jp">` — utility Tailwind. Hoặc set `:lang(ja) { font-family: var(--font-jp); }` global.

**Type scale (modular):**
- `text-display`: 48px / 56px line — chỉ dùng cho hero (1-2 chỗ)
- `text-title`: 28px / 36px — page heading
- `text-section`: 20px / 28px — card heading
- `text-body`: 16px / 24px — default
- `text-caption`: 13px / 20px — meta info
- `text-kanji-hero`: 96px — character display in study mode

### 3.3 Motion language

Hiện tại app static. Add 4 motion primitives:

| Primitive | Khi nào dùng | Spec |
|---|---|---|
| **Fade-up** | Card mount, route transition | 200ms ease-out, 12px translate |
| **Card flip** | Flashcard reveal | 400ms cubic-bezier(0.4, 0, 0.2, 1), 3D rotateY |
| **Pulse-glow** | Streak day complete, level up | 600ms 2x, primary ring 0→8px |
| **Stagger list** | Vocabulary/notification list | 50ms delay × index |

Implement: `framer-motion` hoặc Tailwind 4 `@starting-style` + view-transition API (Next.js 16 hỗ trợ).

### 3.4 Iconography

Hiện dùng `lucide-react` — giữ. Add custom set:
- Kanji-stroke icon (như Wanikani style) cho category icons
- Furigana indicator (small "あ" above kanji)
- Pronunciation wave SVG (cho speech feature)

---

## 4. Feature Roadmap

3 tier × 5 feature = 15 feature. Mỗi feature có **size estimate** (S/M/L), **dependency**, **success signal**.

### Tier 1 — Quick wins (target: 2 tuần)

| ID | Feature | Size | Depends | Success signal |
|---|---|---|---|---|
| **F1** | Today Dashboard (`(app)/page.tsx`) | M | UserProgress API, brand color | DAU +20% sau 2 tuần |
| **F2** | Streak freeze + grace | S | F1 | day-7 retention +15% |
| **F3** | Type-to-answer flashcard mode | M | flashcards store | Avg cards/session +30% |
| **F4** | Audio speed control + auto-replay | S | useAudioRecorder | Audio play count +50% |
| **F5** | Furigana toggle granularity | S | typography update | Setting opt-in rate ≥40% |

### Tier 2 — Medium bets (target: 4-6 tuần)

| ID | Feature | Size | Depends | Success signal |
|---|---|---|---|---|
| **F6** | AI Roleplay scenarios | L | ai-service P9.4 fix | Convs/user/week ≥3 |
| **F7** | Pronunciation drill scored | M | Python NLP, F6 vibe | Drill completion ≥60% |
| **F8** | Cloze trong IT context | M | seed real text corpus | Cards reviewed +25% |
| **F9** | Smart reminder time | S | UserProgress activity log | Reminder→app open CTR +40% |
| **F10** | Multi-facet vocab filter | S | vocabulary store | Search→study conversion +20% |

### Tier 3 — Big bets (target: 2-3 tháng)

| ID | Feature | Size | Depends | Success signal |
|---|---|---|---|---|
| **F11** | Team workspace (B2B) | L | new entity, billing | 1 paying team |
| **F12** | Voice conversation mode | L | F6, STT, TTS streaming | Sessions ≥5min |
| **F13** | Document → personal deck | M | SudachiPy, file upload | Decks/user ≥1 |
| **F14** | AI mnemonic generator | M | ai-service | Mnemonic save rate ≥30% |
| **F15** | Offline-first PWA | M | service worker | Install rate ≥10% |

---

## 5. Phased Rollout

### Phase D0 — Design system foundation (3 ngày)

**Goal:** Brand identity + typography ready. Không touch feature.

- D0.1 Adopt Option A palette → update `globals.css` cả 2 frontend (user + admin)
- D0.2 Load Noto Sans JP + Inter via next/font (not Google CDN — perf)
- D0.3 Define `font-jp` utility, `:lang(ja)` rule
- D0.4 Add motion primitives (CSS only, no framer-motion yet)
- D0.5 Replace Header text-logo with SVG logo (kanji 日 stylized + "Nihongo IT")
- D0.6 Update type scale, button radius (0.625rem → 0.75rem cho softer feel)
- D0.7 Update chart-1..5 colors → palette từ palette

**Deliverable:** Visual change visible across mọi page hiện có. Zero feature change. PR review: side-by-side screenshots before/after cho 5 page chính.

**Risk:** OKLCH color cần Tailwind 4 chroma support — confirm. Browser support ≥Chrome 111 (OK cho engineer audience).

### Phase D1 — Today Dashboard + Streak (4 ngày)

Foundation cho engagement. Tier 1 high-leverage.

- D1.1 BE: `GET /api/v1/learning/today-summary` returning `{ dueCount, newVocabSuggestion[], conversationOfDay, dailyGoalMinutes, streakCount, lastStudyDate }`
- D1.2 BE: streak freeze endpoint + claim logic (max 2/month free, hoặc paid)
- D1.3 FE: `(app)/page.tsx` — 4-card layout:
  - **Hôm nay** — due count + 1 CTA "Học ngay" → /flashcards/study
  - **Streak** — visualize 7 ngày gần nhất + freeze button nếu missed yesterday
  - **Từ mới gợi ý** — 5 vocab N+1 level
  - **Conversation snippet** — 1 dialogue card từ topic chưa hoàn thành
- D1.4 FE: Update `LoginForm.tsx:22` redirect default `/` → `/dashboard` (hoặc `(app)` root)
- D1.5 FE: Streak heatmap component (GitHub-style 30 ngày)

**Deliverable:** User login → thấy dashboard có data, không lạc lối.

### Phase D2 — Active recall (5 ngày)

- D2.1 Type-to-answer mode (F3) — toggle trong flashcard study
- D2.2 Audio speed control (F4) — slider 0.5×-2×, persist localStorage
- D2.3 Furigana 3-mode toggle (F5) — Profile settings

### Phase D3 — AI engagement (7-10 ngày)

Block bởi P9.4 (Spring AI upgrade). Làm parallel:

- D3.0 Unblock ai-service: Spring AI 1.1+ hoặc strip starter, direct RestClient
- D3.1 AI Roleplay (F6) — 5 scenario, prompt template, persona
- D3.2 Pronunciation drill scoring (F7) — mora-by-mora comparison
- D3.3 Smart reminder (F9) — activity log table + cron job tối ưu time

### Phase D4 — Polish (3 ngày)

- D4.1 Motion: framer-motion install, animate route transition, card flip enhance
- D4.2 Notification redesign — dropdown thành side panel với grouping (overdue, today, info)
- D4.3 Profile page redesign — show achievements, JLPT progress bar, learning curve

### Phase D5+ — Tier 3 (parking lot)

Sau D4, evaluate metric. Tier 3 chỉ làm nếu Tier 1+2 retention numbers tốt và có signal monetization.

---

## 6. UI Mockups — text-form

### 6.1 Today Dashboard layout

```
┌─────────────────────────────────────────────────────────────┐
│ 日 Nihongo IT          [🔥 7] [🔔 3]    Tùng ▾              │  ← Header với streak chip
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Chào Tùng, hôm nay học gì? 👋                              │  ← Type-title
│                                                             │
│  ┌─────────────────┐  ┌─────────────────────────────────┐  │
│  │ 🎯 Hôm nay      │  │ 🔥 Streak                       │  │
│  │ 23 thẻ cần ôn   │  │ 7 ngày liên tiếp                │  │
│  │ 5 từ mới        │  │ ████░░░ tuần này 4/7            │  │
│  │ [Bắt đầu →]     │  │ [Đóng băng 2/2 còn lại]         │  │
│  └─────────────────┘  └─────────────────────────────────┘  │
│                                                             │
│  ┌─────────────────────────┐  ┌─────────────────────────┐  │
│  │ 📚 Từ mới gợi ý         │  │ 💬 Hội thoại hôm nay    │  │
│  │ デプロイ — deployment   │  │ "Code Review"           │  │
│  │ リファクタ — refactor   │  │ N3 · 5 phút             │  │
│  │ + 3 từ khác             │  │ [Tham gia →]            │  │
│  └─────────────────────────┘  └─────────────────────────┘  │
│                                                             │
│  Hoạt động 30 ngày qua                                      │  ← Heatmap GitHub-style
│  ▢▢▢▣▢▣▣▢▣▣▣▣▢▣▣▣▣▣▣▢▣▣▣▣▣▣▢▣▣▣                              │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 Flashcard study với type-to-answer

```
┌──────────────────────────────────────────────────────────┐
│  ← Quay lại        12 / 23                  ⚙ Settings   │
├──────────────────────────────────────────────────────────┤
│                                                          │
│                                                          │
│                  デプロイ                                │  ← font-jp, kanji-hero (96px)
│                                                          │
│              ┌──────────────────────┐                    │
│              │  Gõ nghĩa tiếng Việt │                    │  ← type-to-answer mode
│              │  [_________________] │                    │
│              └──────────────────────┘                    │
│                                                          │
│              hoặc                                        │
│                                                          │
│              [👁 Xem nghĩa] [⏭ Bỏ qua]                   │
│                                                          │
│                                                          │
│   🔊 ▷  Speed: ──○──  1x      Auto-replay: ☑            │  ← audio speed control
└──────────────────────────────────────────────────────────┘
```

### 6.3 AI Roleplay scenario card

```
┌──────────────────────────────────────────────────────┐
│  🎭 Daily Standup                       N3 · 10 phút │
│                                                      │
│  Bạn báo cáo tiến độ với sếp Yamada-san về sprint    │
│  hiện tại. Sếp sẽ hỏi về blocker và timeline.        │
│                                                      │
│  Nhân vật: 山田部長 (Yamada-buchou)                  │
│  Bạn sẽ học: 報告, ブロッカー, 進捗, 締め切り        │
│                                                      │
│  [▶ Bắt đầu]                       [👁 Xem trước]    │
└──────────────────────────────────────────────────────┘
```

---

## 7. Success metrics

Track 4 metric chính, baseline = avg 7 ngày trước khi land D0:

| Metric | Baseline (est.) | Target sau D1-D4 | Cách đo |
|---|---|---|---|
| **DAU/WAU ratio** | ~20% (guess) | ≥35% | Auth login event |
| **Avg session length** | 4-6 phút (guess) | ≥10 phút | Time spent in `(app)/*` |
| **Reviews / user / week** | ? | ≥30 | FSRS review_logs |
| **Day-7 retention** | ? | ≥40% | Cohort: new register → return at day 7 |

**Action:** Phase D0 BE thêm `app_event` table + minimal tracking middleware. Không cần full analytics, chỉ 4 event: `login`, `study_session_start`, `study_session_end`, `card_reviewed`. Self-hosted, không gọi external.

---

## 8. Open questions — cần quyết trước khi land

| # | Question | Default proposal | Block phase |
|---|---|---|---|
| Q1 | Brand palette: Option A (Indigo+Sakura) hay Option B (Sumi+Shu)? | A | D0 |
| Q2 | Logo: ai design? hire 1-shot Fiverr hay tự SVG? | Tự SVG đơn giản | D0 |
| Q3 | AI roleplay dùng OpenAI hay model open-source? Chi phí monthly? | OpenAI gpt-4o-mini, cap $20/user/month, free 5 conv/week | D3 |
| Q4 | Streak freeze: free 2/month hay paywalled? | Free 2/month (acquisition), thêm là paid | D1 |
| Q5 | Team workspace (F11) — làm B2B trong scope plan này không? | Không, parking lot D5+ | — |
| Q6 | Analytics: thêm Mixpanel/PostHog hay self-host minimal? | Self-host minimal (`app_event` table) — privacy + zero vendor | D0 |

---

## 9. Risks

| Risk | Impact | Mitigation |
|---|---|---|
| OKLCH color không render đồng đều cross-browser | M | Test Safari 16+ early in D0. Fallback HSL nếu cần. |
| Noto Sans JP add 200KB+ bundle | M | Subset chỉ cover JLPT N5-N3 kanji + kana (~80KB). next/font auto-subset. |
| AI roleplay cost spike | H | Cap usage per user. Streaming response. Cache common prompts. |
| Type-to-answer fuzzy match phá grading | M | Tolerance config + A/B test: type-mode users vs reveal-mode users FSRS retention sau 4 tuần. |
| Smart reminder requires activity log = new table + writes on every action | M | Lazy aggregate: store last 30 day events, scheduled job tính optimal time tuần 1x. |
| Spring AI 1.0→1.1 upgrade phá ai-service | H | Strip Spring AI starter, direct OpenAI RestClient. Đơn giản hơn, ít magic. |

---

## 10. What stays the same (giảm scope creep)

- Microservice architecture
- 21 user routes (chỉ thêm `(app)/page.tsx` dashboard)
- 50 E2E test — sẽ thêm cho feature mới, không refactor existing
- shadcn/ui — chỉ retheme, không thay component lib
- Backend Kotlin/Spring Boot 4
- FSRS algorithm (chỉ visualize tốt hơn, không thay)

---

## 11. Concrete next step

**Step 1 — Decide Q1, Q4, Q6** (quick design discussion với owner)
**Step 2 — Spike D0 trong 1 ngày** (chỉ palette + font + 3 page screenshots before/after)
**Step 3 — Demo cho team. Nếu approve → commit D0 trong PR riêng, không gộp feature**
**Step 4 — Land D0 production → measure 1 tuần baseline metric**
**Step 5 — Bắt đầu D1 (Today Dashboard)**

Không recommend làm D0-D5 trong 1 stretch. Mỗi phase có giá trị độc lập.

---

## 12. Reference

- FSRS visualization patterns: https://github.com/open-spaced-repetition/fsrs4anki
- Japanese typography on web: https://www.smashingmagazine.com/2020/06/japanese-typography-web-design/
- Engagement loop framework: Nir Eyal "Hooked" — Trigger → Action → Variable Reward → Investment
- Existing project plan (P1-P9): `docs/plans/2026-05-21-playwright-e2e-plan.md`
- Audit findings (this doc §2): commit `c039f13` baseline
