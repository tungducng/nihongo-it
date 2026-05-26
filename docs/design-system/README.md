# Nihongo IT — Design System

> A design language for **Nihongo IT** (にほんごIT), a Japanese learning app for Vietnamese IT professionals. This system is a brand evolution of the existing shadcn-based codebase; it keeps the structural DNA (Tailwind v4, shadcn primitives, lucide-react icons) but introduces a tighter Japanese-learning visual identity.

## What is Nihongo IT?

A Vietnamese-language web app that teaches **technical Japanese** to engineers, QA, and IT staff working with Japanese clients or in Japanese companies. The product is split into two surfaces:

| Product            | Audience            | Path                | Key surfaces                                                                 |
| ------------------ | ------------------- | ------------------- | ---------------------------------------------------------------------------- |
| `frontend-user`    | Learners (Vietnamese) | `localhost:3000`    | Vocabulary browser, flashcards (SRS), conversation practice with speech analysis, furigana tool, translation tool, JLPT stats |
| `frontend-admin`   | Content editors     | `localhost:3001`    | User management, vocabulary/topic/category CRUD, conversation editor, dashboard |

**Stack** (both apps): Next.js 16 (App Router), React 19, Tailwind v4, shadcn/ui, Radix primitives, lucide-react, Zustand, react-hook-form + zod, axios, sonner toasts, chart.js. The codebase's product copy is in **Vietnamese**; UI labels say "Đăng nhập", "Từ vựng", "Hội thoại", etc.

### Sources consulted

- `frontend-user/` — Next.js user app, mounted via File System Access
- `frontend-admin/` — Next.js admin app, mounted via File System Access
- Key files referenced while authoring this system:
  - `frontend-user/src/app/globals.css` — current shadcn token set
  - `frontend-user/src/app/page.tsx` — marketing/home copy
  - `frontend-user/src/components/{ui,vocabulary,flashcard,conversation,layout}/` — existing components
  - `frontend-user/src/app/(app)/**`, `(auth)/**`, `(public)/**` — feature screens
  - `frontend-admin/src/app/(admin)/**`, `frontend-admin/src/components/**`

No Figma or design files were provided — the design system is reverse-engineered from code and then evolved.

---

## Index

```
.
├── README.md                       — you are here
├── SKILL.md                        — Claude / Claude Code skill manifest
├── colors_and_type.css             — drop-in CSS tokens (colours, type, spacing, motion, radii)
├── assets/                         — brand marks, illustrations, generic photography placeholders
│   ├── logo.svg                    — primary wordmark
│   ├── mark.svg                    — 日 hanko mark (vermillion)
│   ├── mark-ai.svg                 — 日 hanko mark (indigo)
│   └── lucide-icons.md             — icon usage notes
├── preview/                        — Design-System-tab cards (small specimens, ~700×N)
│   ├── colors-*.html, type-*.html, spacing-*.html
│   └── component-*.html
├── ui_kits/
│   ├── user/                       — learner-facing UI kit (cards, flashcards, vocab grid, header…)
│   │   ├── index.html              — interactive click-through
│   │   ├── Header.jsx, Sidebar.jsx, VocabularyCard.jsx, FlashcardReview.jsx, …
│   │   └── README.md
│   └── admin/                      — admin UI kit (sidebar shell, data tables, dashboard)
│       ├── index.html
│       └── README.md
└── fonts/                          — empty; project uses Google Fonts CDN (see §Type)
```

---

## Content fundamentals

The product speaks **Vietnamese** to the learner and presents Japanese as the object of study. Tone is **direct, encouraging, slightly formal** — closer to a study planner than a gamified language app. There is no mascot voice. No emoji.

### Voice & tone

- **Vietnamese is the chrome; Japanese is the content.** All UI labels, errors, empty states are Vietnamese. Japanese only appears as study material (vocab terms, conversation lines, kanji headers).
- **Imperative + sentence case.** Buttons say `Đăng nhập`, `Lưu`, `Bấm để lật` — verb-first, no trailing punctuation, capitalize only the first word.
- **Address the user with the implicit "you".** Copy says `Học tiếng Nhật chuyên ngành IT — từ vựng, hội thoại, flashcards…` not "your vocabulary" or "we will teach you".
- **Em-dashes connect promises.** Marketing copy uses ` — ` (with spaces) to chain ideas, not bullets in a sentence.
- **JLPT levels are spoken plainly.** `N1`, `N2`… `N5` appear as bare uppercased badges; never "Level 5" or "Beginner". The level is the noun.
- **Errors are short and own the fault.** `Không tải được danh sách từ vựng.` `Không cập nhật được trạng thái.` — verb negated, no apology, no "please try again".
- **Loading is a sentence, not a spinner alone.** `Đang tải...`, `Đang khôi phục phiên làm việc...`, `Đang đăng nhập...` — present-continuous `Đang` + verb.

### Casing

- Sentence case for **everything** including page titles (`Từ vựng`, not `TỪ VỰNG`).
- Uppercase **only** for: JLPT level codes (N1–N5), category eyebrows (`TỔNG QUAN`, `HÔM NAY`), and the brand wordmark.
- The brand is **Nihongo IT** — title case, single space, no period.

### Numbers & units

- Counts go inline with their noun: `12 bài hội thoại`, `120 từ vựng`. No "items", no parens.
- Dates render via `toLocaleString('vi-VN')`.
- Tabular figures (`font-variant-numeric: tabular-nums`) for any column of numbers — stats, counters, dates.

### Copywriting examples (from code)

| Surface              | Copy                                                                                  |
| -------------------- | ------------------------------------------------------------------------------------- |
| Home hero            | `Học tiếng Nhật chuyên ngành IT — từ vựng, hội thoại, flashcards với spaced repetition và phân tích phát âm bằng AI.` |
| Empty state          | `Không tìm thấy hội thoại phù hợp.`                                                   |
| Vocab placeholder    | `Khám phá từ vựng IT theo cấp độ JLPT`                                                |
| Flashcard hint       | `Bấm để lật`                                                                          |
| Login submit busy    | `Đang đăng nhập...`                                                                   |
| Toast success        | `Đăng nhập thành công`                                                                |
| Toast error          | `Đăng nhập thất bại`                                                                  |
| Auth gate            | `Đang khôi phục phiên làm việc...`                                                    |
| Admin section header | `TỔNG QUAN`, `HÔM NAY`                                                                |

---

## Visual foundations

### Colour

The codebase ships **stock shadcn neutrals** (pure grayscale, monochrome primary). The new system replaces that with a two-colour brand:

- **Primary — `--ai-500`** `oklch(0.48 0.135 260)` ≈ `#3a4ea0`. _Ai-iro_ (藍色) — the traditional Japanese indigo found in workwear dye, woodblock prints, and noren curtains. Used for: primary buttons, active nav, links, kanji headings, focus rings, the admin sidebar mark.
- **Accent — `--shu-500`** `oklch(0.65 0.185 35)` ≈ `#d24a26`. _Shu-iro_ (朱色) — vermillion, the colour of hanko seals, torii gates, and red ink corrections on a study sheet. Used **sparingly**: the brand mark background, notification dots, "new" badges, the active flashcard streak indicator. Never a default button colour.
- **Neutrals — washi paper scale.** A warm-leaning neutral ramp (`--washi-50` → `--washi-900`, hue 70–80°). Replaces the cool clinical grays in the current codebase so the UI reads like a study notebook.
- **JLPT level chromas.** Five fixed hues mapped to N5 (emerald, easiest) → N4 (sky) → N3 (amber) → N2 (orange) → N1 (rose, hardest). The same colour appears on a vocab badge, the JLPT filter dropdown, the stats chart for that level, and any progress ring. Consistency here is the rule.
- **States** — success (emerald), warning (amber), danger (rose), info (= ai-500).

See `colors_and_type.css` for the full token set.

### Type

The codebase loads `Inter` for Vietnamese/Latin. This system keeps Inter and adds:

- **Inter 400/500/600/700** — body, UI labels, headings (Vietnamese & Latin)
- **Noto Sans JP 400/500/600/700** — inline Japanese in body text (`:lang(ja)`, `.jp`)
- **Noto Serif JP 500/700/900** — Japanese display text on flashcard fronts, vocabulary detail headers (`.jp-display`)
- **JetBrains Mono 400/500** — code snippets, romaji transliterations, tabular numbers

All four are pulled via Google Fonts CDN in `colors_and_type.css`. No locally-hosted font files yet — see _Caveats_ below.

**Scale.** A standard 12 / 14 / 16 / 18 / 20 / 24 / 30 / 36 / 48 px scale (--text-xs through --text-5xl). Japanese display headlines get their own larger track: 40 / 64 / 96 px.

**Furigana** — `<ruby>会議<rt>かいぎ</rt></ruby>`. The `rt` element renders at 0.5em in `--washi-500`. Critical for the vocabulary detail screen.

### Spacing & layout

- **4px base scale.** Tokens `--space-1` (4) … `--space-20` (80).
- **Container.** `container mx-auto px-4` (1280px max) — inherited from existing code.
- **Header.** `h-14` (56px) sticky, bg-background, single bottom border. Logo left, nav center, avatar right.
- **Admin sidebar.** Fixed `w-60` (240px), `bg-card` with right border. Vertical nav list, lucide icons at `size-4`.
- **Cards.** `p-4` interior, `gap-3` between cards in grids, `gap-6` between page sections.
- **Form rows.** `space-y-1.5` label↔input, `space-y-4` field↔field.
- **Touch targets.** Buttons height: `h-8` default, `h-9` lg, `h-7` sm. Icon buttons square. Never below `h-6` for tappable elements.

### Backgrounds

- **No gradients.** The system is solid-colour-first. The hero is `bg-muted` (warm off-white), not a purple gradient.
- **No background images on UI.** Imagery is for content (avatars, conversation illustrations); chrome stays flat.
- **No textures or hand-drawn motifs.** The hanko-style 日 mark is the only "Japanese-ness" signal in the chrome; everything else is restrained.
- **Sidebar tint.** Admin sidebar uses `--sidebar` (`oklch(0.985 0.006 260)`) — a 0.6%-chroma indigo wash, almost imperceptible but it separates the sidebar from the canvas without a heavy border.

### Borders, corners, shadows

- **Radius.** Base `--radius: 10px`. Inputs and small buttons use `--radius-md` (8px). Cards use `--radius-lg` (10px). Modals and feature cards step up to `--radius-xl/2xl`. JLPT/level chips and pills use `--radius-pill`.
- **Borders.** A single hairline `1px solid var(--border)` (washi-300). Never doubled, never coloured except for focus/danger states.
- **Shadows.** Five-tier ramp from `--shadow-xs` to `--shadow-xl`, all soft and black-translucent (no coloured shadows). Cards rest at `--shadow-sm`; popovers/menus at `--shadow-lg`; modals at `--shadow-xl`.
- **Focus ring.** `--shadow-focus` — a 3px `ai-500/25%` outer ring. Always visible on `:focus-visible` for keyboard users.

### Motion

- **Easing.** Primary easing is `--ease-out` `cubic-bezier(0.16, 1, 0.3, 1)` — quick start, soft land. Used for menus, dropdowns, tooltips, sheet enters.
- **Durations.** `fast 120ms` (hover/press colour swaps), `base 180ms` (most transitions), `slow 320ms` (sheets, modals), `flip 500ms` (flashcard rotateY).
- **Hover state.** Subtle background darken (`hover:bg-muted` for ghost, `hover:bg-primary/80` for solid). No scale, no lift.
- **Press state.** `active:translate-y-px` (1px nudge) — present in the codebase's button.tsx, kept. No shrink, no spring.
- **No bounce, no parallax, no scroll-triggered animations.** The product is for studying; motion stays out of the way.

### Transparency & blur

- Used sparingly for **overlays only**: modal backdrop (`bg-black/40`), dropdown shadow halos. Body chrome is opaque.
- Sticky header is opaque (`bg-background`), not blurred — the canvas underneath is rarely visually-rich enough to need a frosted effect.

### Imagery vibe

- The codebase has no photography. If/when added, prefer **muted, warm, naturally-lit** stock — desk scenes, books, kanji notebooks. Avoid neon, avoid anime/manga illustration, avoid stock "happy team meeting" photos. Black-and-white or low-saturation works well alongside the vermillion accent.

---

## Iconography

The codebase uses **`lucide-react`** exclusively — already a dependency. Stroke icons, 2px stroke weight, 24×24 viewBox, rendered at `size-4` (16px) in buttons and `size-7` (28px) in stat cards.

**Icons in use** (extracted from imports): `ArrowLeft`, `ArrowRightLeft`, `BarChart3`, `Bell`, `Bookmark`, `BookmarkCheck`, `BookOpen`, `Brain`, `CalendarClock`, `Check`, `CheckCircle2`, `CheckCheck`, `ChevronLeft`, `ChevronRight`, `Copy`, `FileText`, `Flame`, `FolderTree`, `Languages`, `Layers`, `LayoutDashboard`, `Loader2`, `Lock`, `LogOut`, `MessageSquare`, `Mic`, `Mic2`, `RefreshCw`, `RotateCw`, `Search`, `Send`, `Settings`, `Sparkles`, `Square`, `Tag`, `Trash2`, `TrendingUp`, `User`, `UserPlus`, `Users`, `Volume2`, `X`.

**Usage rules**
- **One icon family only — Lucide.** No mixing with Heroicons, Feather, or hand-drawn SVGs.
- **Stroke icons in chrome, never filled.** The only fill exception is the brand 日 mark.
- **Pair an icon with its label** in nav and CTAs. Standalone icon buttons must have `aria-label`.
- **No emoji.** Anywhere. Not in toasts, not in copy, not in empty states.
- **No unicode glyphs as icons** (no →, ✓, ★). Use the Lucide equivalent.
- **No icon backgrounds.** Icons sit on canvas in `text-muted-foreground` or the appropriate semantic colour (`text-primary` for active, `text-destructive` for delete). No coloured circle behind them.

In static HTML mocks (preview cards, UI kit `index.html`), load Lucide via CDN:

```html
<script src="https://unpkg.com/lucide@latest"></script>
<i data-lucide="bookmark"></i>
<script>lucide.createIcons();</script>
```

### The 日 mark

The brand mark is the kanji 日 ("ni" / "sun" / "day") set in a rounded vermillion square — a stylized hanko seal. Files in `assets/`:

- `assets/logo.svg` — full horizontal wordmark
- `assets/mark.svg` — 日 in vermillion square (favicon / app icon)
- `assets/mark-ai.svg` — 日 in indigo square (alternate)

These are the only brand-illustrated assets. No hand-drawn cherry blossoms, no torii silhouettes, no Mt. Fuji.

---

## Caveats

- **No fonts hosted locally.** The system pulls Inter / Noto Sans JP / Noto Serif JP / JetBrains Mono from Google Fonts CDN. For production, self-host the subsets you need (Inter Latin + Vietnamese, Noto Sans/Serif JP regular subset).
- **The current codebase is monochrome.** This design system is a **proposal** — adopting it requires replacing the token block at the top of `frontend-user/src/app/globals.css` and `frontend-admin/src/app/globals.css`. The shadcn component classNames don't need to change.
- **No real photography or illustrations** were provided. The 日 hanko mark is the only brand asset created here.
- **No marketing site, mobile app, or docs site** appears in the codebases; only the two web apps. If those exist, they're not represented yet.

