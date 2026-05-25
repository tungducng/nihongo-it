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

3 tier × 5 feature = **15 feature**. Mỗi feature: **vì sao** (problem nó giải quyết), **ý tưởng cụ thể** (implementation đề xuất), **size** (S=1d, M=2-3d, L=5-7d), **dependency**, **success signal** (đo bằng gì).

---

### Tier 1 — Quick wins (target: 2 tuần)

#### **F1. "Hôm nay" Dashboard** · M · depends: UserProgress API + brand color

**Vì sao.** Giảm decision fatigue — đa số bỏ học vì không biết bắt đầu từ đâu. Login xong hiện đang lạc về landing page (xem §2.2).

**Ý tưởng cụ thể.** Trang home mới `(app)/page.tsx`:
- Card "Hôm nay": due flashcards (N thẻ), CTA duy nhất "Bắt đầu →" chạy chuỗi flow tự động (study → vocab mới → conversation snippet)
- 5 từ mới gợi ý từ vocab N+1 level
- 1 conversation snippet ngắn
- Daily goal progress (15 phút default, configurable)
- Streak chip 7 ngày + heatmap 30 ngày GitHub-style

**Success signal.** DAU +20% sau 2 tuần. Avg time-to-first-action sau login ≤5s.

---

#### **F2. Streak freeze + grace period** · S · depends: F1

**Vì sao.** Streak là retention driver mạnh nhất, nhưng "mất 1 ngày = mất hết" làm user demoralize và quit. Duolingo có freeze cũng vì lý do này.

**Ý tưởng cụ thể.**
- Cho phép 2 freeze/tháng (auto-claim khi miss day, không cần user click)
- Cuối tuần streak giữ nguyên nếu user opt-in setting "Weekend rest"
- Visualize: streak chip có icon ❄ khi freeze active, đếm freeze còn lại
- Notification khi freeze sắp hết tháng

**Success signal.** Day-7 retention +15%. Average streak length tăng từ ~3 ngày → 7+ ngày.

---

#### **F3. Type-to-answer flashcard mode** · M · depends: flashcards store

**Vì sao.** Active recall (gõ ra) hiệu quả gấp ~2× so với passive recall (xem-rồi-rate) theo nghiên cứu của Karpicke & Roediger 2008.

**Ý tưởng cụ thể.**
- Toggle "Mode" trong study session: **Reveal** (hiện tại) | **Type** (mới)
- Type mode: user gõ nghĩa tiếng Việt HOẶC kanji/kana
- Fuzzy match tolerant:
  - Hiragana ↔ Katakana (デプロイ = でぷろい)
  - Gõ romaji được nhận (depuroi)
  - Tolerance synonym tiếng Việt (triển khai = deploy = cài đặt sản phẩm)
- FSRS rating tự động: correct = `Good`, wrong = `Again`, gần đúng = `Hard`
- Settings: chọn type kanji-only / meaning-only / both

**Success signal.** Avg cards/session +30%. Retention rate đo qua FSRS log sau 4 tuần (compare type-mode users vs reveal-mode users).

---

#### **F4. Audio speed control + auto-replay** · S · depends: useAudioRecorder

**Vì sao.** TTS Japanese tốc độ chuẩn quá nhanh cho beginner. Beginner phải tua đi tua lại nhiều lần — friction cao.

**Ý tưởng cụ thể.**
- Slider speed: 0.5× / 0.75× / 1× / 1.25× / 1.5×
- Auto-replay 2 lần khi flip card (configurable 1-5 lần)
- Persist preference vào localStorage + UserProgress backend
- Shortcut keyboard: `R` để replay, `[` `]` để giảm/tăng speed

**Success signal.** Audio play count/session +50%. Slider/replay được dùng ≥60% session beginner.

---

#### **F5. Furigana toggle granularity** · S · depends: typography update (D0)

**Vì sao.** All-or-nothing furigana làm khó học kanji — advanced user thấy thừa, beginner thấy thiếu.

**Ý tưởng cụ thể.**
- 3 mode global trong Profile settings:
  - **Off** — không bao giờ hiện furigana
  - **Smart** — chỉ hiện trên kanji ≥ JLPT-N của user (vd user N4 → hiện trên N3+ kanji)
  - **Always** — hiện trên mọi kanji
- Trong study session: tap-and-hold (mobile) hoặc hover (desktop) hiện furigana tạm thời, không đổi global setting
- Backend lưu reading per-kanji để render chính xác (đã có Python NLP integration)

**Success signal.** Setting opt-in rate ≥40% (user khác default). Average study completion rate +10%.

---

### Tier 2 — Medium bets (target: 4-6 tuần)

#### **F6. AI Roleplay scenarios** · L · depends: ai-service unblock (P9.4)

**Vì sao.** ai-service có sẵn nhưng đang dùng conservatively (chỉ chat đơn giản). AI roleplay là **differentiator lớn nhất** so với Duolingo/Anki — không ai làm tốt cho Japanese-IT niche.

**Ý tưởng cụ thể.** 5 scenario built-in, mỗi cái 5-10 phút:
- **Daily Standup** với sếp Yamada-buchou (báo cáo tiến độ, blocker, plan today)
- **Code Review** với senior Tanaka-san (giải thích logic, defend choice, accept feedback)
- **Sprint Planning** với PM Sato-san (estimate, prioritize, push back unrealistic deadline)
- **Báo cáo lỗi sản phẩm** qua email cho khách hàng (apology + root cause + fix plan)
- **Phỏng vấn xin việc** mock với HR Tanabe-san (giới thiệu, kinh nghiệm, hỏi văn hóa)

Mỗi scenario:
- Nhân vật có persona (tuổi, position, tính cách, expectations)
- AI generate phản hồi theo persona, không phải robot
- User trả lời bằng voice (STT → text) HOẶC type
- End-of-session feedback: từ vựng sai/thiếu, ngữ pháp cần ôn, suggestion phrase chuyên nghiệp hơn

**Success signal.** Conversations/user/week ≥3. Session completion ≥70%. Vocab từ scenario được added vào flashcard ≥5/user/week.

---

#### **F7. Pronunciation drill có chấm điểm** · M · depends: Python NLP, vibe từ F6

**Vì sao.** Python NLP service (SudachiPy + pronunciation analysis) đã có nhưng chưa tận dụng — chỉ dùng cho furigana hiện tại.

**Ý tưởng cụ thể.**
- Drill mode: lấy 1 câu IT (vd "デプロイが失敗しました — Triển khai thất bại")
- TTS đọc câu → user record giọng → service so sánh:
  - **Mora-by-mora**: chuẩn so với recording, highlight mora sai
  - **Pitch accent**:平板型 vs 頭高型 — visualize bằng waveform
  - **Length** (long vs short vowel, sokuon): điểm 0-100
- Lưu top 10 lỗi cá nhân vào `pronunciation_drill_log`
- Weekly "Drill of the Week" — top 5 câu user sai nhiều nhất tuần

**Success signal.** Drill completion ≥60%. Avg score tăng theo tuần (track per-user trend). Top-10 errors reduce ≥30% sau 4 tuần drill.

---

#### **F8. Cloze trong IT context thật** · M · depends: seed real text corpus

**Vì sao.** Học từ rời rạc không dùng được. Cloze trong real sentence tăng recall ~1.5× và bridge gap "biết từ" → "dùng được từ".

**Ý tưởng cụ thể.**
- Crawl/curate 100-200 đoạn JP từ:
  - Tech blog Qiita, Zenn (CC-BY license check)
  - Stack Overflow JP QA
  - Real error log (Sentry, Datadog) — anonymized
  - Japanese OSS commit message
- AI gen cloze: che 1-2 từ ở level N+1 của user
- Mode: user fill in blank (type) hoặc multiple choice (4 lựa chọn)
- Reading exercise + cloze = 1 module mới `/reading` (route mới)

**Success signal.** Cards reviewed/week +25%. New route `/reading` có ≥30% DAU touch.

---

#### **F9. Smart reminder time** · S · depends: UserProgress activity log

**Vì sao.** Notification 8pm cứng không match nhịp sinh hoạt — engineer làm overtime, commute time variable.

**Ý tưởng cụ thể.**
- Track 14 ngày activity: timestamp mỗi `study_session_end` event
- Sang tuần 3, scheduled job tính:
  - Hour có completion rate cao nhất (vd 22:15)
  - Day-of-week pattern (weekday vs weekend)
- Auto-điều chỉnh `reminder_time` trong UserEntity
- UI: Profile show analytics "Bạn học hiệu quả nhất lúc 22:15, mỗi session 12 phút trung bình"
- User vẫn override được manual

**Success signal.** Reminder → app open CTR +40%. Sessions starting within 30min of reminder +60%.

---

#### **F10. JLPT/IT-domain multi-facet filter** · S · depends: vocabulary store + topic tag schema

**Vì sao.** 5000+ từ trong DB nhưng search/filter còn yếu — chỉ filter theo topic. Engineer muốn focus, vd "chỉ học term DevOps level N3".

**Ý tưởng cụ thể.**
- Multi-select facet chip trên `/vocabulary`:
  - **JLPT level** (N5-N1, multi)
  - **IT domain** (backend/frontend/devops/PM/design/QA/business)
  - **Mastery** (chưa học / đang học / đã thuộc theo FSRS state)
- Saved filter: user save combo "DevOps N3 chưa học" → quick-access từ sidebar
- URL stateful (share filter qua link)

**Success signal.** Search → study conversion +20%. Saved filters used ≥3/user.

---

### Tier 3 — Big bets (target: 2-3 tháng)

#### **F11. Cohort / Team Workspace** · L · depends: new entity + billing

**Vì sao.** Sale B2B vào cty outsource Nhật. ARPU cao hơn ~10× so với individual user. Có sẵn admin app — extend cho team admin role.

**Ý tưởng cụ thể.**
- Admin tạo workspace cty (custom domain optional)
- Invite member (email or bulk CSV)
- Share custom deck: "Bộ từ dự án X", "Glossary nội bộ"
- Leaderboard nội bộ tuần (opt-in privacy)
- Slack/Teams bot daily summary: "Team studied 145 reviews, avg streak 4.2 ngày"
- Billing: $5/user/month, min 10 user

**Success signal.** 1 paying team (10+ user) trong 60 ngày sau launch. Workspace retention day-30 ≥60%.

---

#### **F12. Voice Conversation Mode** · L · depends: F6, STT, TTS streaming

**Vì sao.** Speech recognition + TTS + AI = full immersive conversation. Closest thing đến luyện nói với native speaker.

**Ý tưởng cụ thể.**
- User nói → STT (OpenAI Whisper hoặc Azure Speech) → AI hiểu intent → AI generate reply tiếng Nhật → TTS đọc → loop
- Recording được save (privacy: opt-in) để review pronunciation sau session
- Mode "free conversation" (không scenario, AI làm conversation partner)
- Pricing: usage-based với OpenAI cost — free 10 phút/tuần, paid unlimited
- Visualize: waveform real-time, turn indicator (đến lượt bạn)

**Success signal.** Sessions ≥5 phút trung bình. Conversion free → paid ≥5% trong cohort dùng feature ≥3 lần.

---

#### **F13. Personal Vocabulary từ Document** · M · depends: SudachiPy + file upload

**Vì sao.** User thật sự có spec/email/PR description tiếng Nhật cần đọc → muốn extract vocab chưa biết để học trước khi đọc.

**Ý tưởng cụ thể.**
- Drop PDF/text/image (OCR) vào `/import`
- SudachiPy tokenize → cross-reference với vocab user đã thuộc (FSRS state ≥ Mature)
- Suggest cards mới chỉ cho từ chưa biết, prioritize theo frequency trong document
- Convert document → personalized deck trong 30s
- Optional: AI generate context sentence cho mỗi từ ("trong document của bạn, X xuất hiện trong câu Y")

**Success signal.** Decks/user ≥1 sau 30 ngày. Card retention từ document-derived deck > generic deck (đo qua FSRS log).

---

#### **F14. AI Mnemonic Generator** · M · depends: ai-service

**Vì sao.** Kanji khó nhớ. Mnemonic là technique được prove (Heisig method), nhưng generic mnemonic không match người dùng. AI có thể personalize.

**Ý tưởng cụ thể.**
- Gặp kanji khó nhớ (vd 構築 kōchiku), nút "🧠 Mnemonic" trên card
- AI gen dựa trên kanji user đã biết:
  > "構 (cấu trúc) + 築 (xây) = build (構築). Liên tưởng: bạn đã biết 構造 (kōzō, structure), giờ thay 造 bằng 築 (xây) = action build something."
- Visual mnemonic (optional): AI gen 1 ASCII art hoặc emoji story
- Lưu mnemonic vào card → hiện lại mỗi lần review
- Community: user upvote mnemonic hay (sau Tier 1+2 stable)

**Success signal.** Mnemonic save rate ≥30%. Cards có mnemonic retention rate > cards không có (FSRS log compare).

---

#### **F15. Offline-first PWA** · M · depends: service worker

**Vì sao.** Engineer hay đi tàu/máy bay, lúc đó là **prime study time** — bị tắt mạng = mất cơ hội học.

**Ý tưởng cụ thể.**
- Service worker cache deck đến local IndexedDB (last 30 days due + saved decks)
- Sync FSRS review_logs khi online lại (conflict resolution: latest-wins on review_timestamp)
- Install banner cho mobile/desktop (`beforeinstallprompt`)
- Lock-screen widget Android: streak counter + due count (Android Widget API)
- Offline indicator UI: chip "Offline — sẽ sync sau" khi mất mạng

**Success signal.** Install rate ≥10% DAU. Offline session ≥15% total sessions.

---

### Đề xuất ưu tiên (cá nhân — owner decide)

Nếu budget 1 sprint 2 tuần (D0 + D1 + D2):
1. **D0** Design system foundation (xem §5)
2. **F1** Today Dashboard (base cho mọi feature engagement)
3. **F3** Type-to-answer (quick win, retention impact rõ)
4. **F2** Streak freeze (1 ngày code, retention impact lớn)
5. **F4** Audio speed (1 ngày, quality-of-life)

Sau 2 tuần → measure baseline metrics. Quyết Tier 2 features dựa data thật, không guess.

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
