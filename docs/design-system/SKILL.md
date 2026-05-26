---
name: nihongo-it-design
description: Use this skill to generate well-branded interfaces and assets for Nihongo IT — a Vietnamese-language web app teaching technical Japanese to IT professionals. Contains the brand colour palette (indigo + vermillion + warm neutrals + JLPT level chromas), typography (Inter + Noto Sans/Serif JP + JetBrains Mono), spacing/shadow/motion tokens, content-tone rules (Vietnamese chrome, no emoji, sentence case), the 日 hanko brand mark, and React/HTML UI kits for the learner-facing and admin surfaces. Use it for production code styling, throwaway prototypes, marketing mocks, or any artifact that needs to look like Nihongo IT.
user-invocable: true
---

# Nihongo IT design

Read `README.md` for the complete brand language. The most-referenced files when designing:

| File | What it gives you |
| --- | --- |
| `colors_and_type.css` | Drop-in CSS variables — colours, type, spacing, radii, shadows, motion. Import into any HTML file. |
| `preview/*.html` | Individual specimen cards — open these to see the canonical example of each token in use. |
| `assets/logo.svg`, `assets/mark.svg`, `assets/mark-ai.svg` | Brand wordmark + 日 hanko marks (vermillion + indigo). |
| `ui_kits/user/` | High-fidelity learner-app UI kit — header, login, vocab grid, vocab detail with furigana, flashcards, conversations, statistics. Open `index.html` to play with the click-thru. |
| `ui_kits/admin/` | Admin UI kit — sidebar shell, dashboard, data tables for users and vocabulary. |

## Quick start — minimal HTML artifact

```html
<link rel="stylesheet" href="path/to/colors_and_type.css">
<style>
  body { background: var(--washi-50); color: var(--washi-800); font: 400 14px var(--font-sans); }
  h1   { font: 700 30px var(--font-sans); color: var(--washi-900); letter-spacing: -0.015em; }
  .jp  { font-family: var(--font-jp); }
</style>
<h1>Học tiếng Nhật chuyên ngành IT</h1>
<p>Cuộc họp = <span class="jp">会議</span> (かいぎ)</p>
```

## Non-negotiable rules

- **Tone & language.** UI chrome is Vietnamese. Japanese appears only as study content. Sentence case. No emoji. No exclamation marks. Errors are short and own the fault.
- **Colour.** `--ai-500` is primary. `--shu-500` is the accent — use sparingly (brand mark, notification dot, "new" / streak indicators). Neutrals are the warm `--washi` ramp, never cool greys. JLPT levels have fixed hues (N5 emerald → N1 rose); use the matching colour wherever a level appears.
- **Type.** Inter for Vietnamese/Latin. Noto Sans JP inline. Noto Serif JP for display-size Japanese (flashcard fronts, vocab headers). Furigana via `<ruby>` + `<rt>`.
- **Iconography.** Lucide only. Stroke-only. No emoji, no unicode arrows. Pair icons with labels in chrome.
- **Components.** Reach for the patterns shown in `ui_kits/user/` and `ui_kits/admin/` rather than inventing new ones. The shadcn primitive shapes (32-h buttons, 34-h inputs, 10px radius cards) are the system.

## When the user invokes this without context

Ask:
1. What are you making? (slide, marketing page, in-product screen, prototype, production styling)
2. Who's the audience — learners, admins, marketing visitors?
3. Do you need full pixel fidelity to the existing app, or a fresh visual riff on the brand?
4. Will any Japanese / kanji appear, and at what size? (Display-size kanji needs Noto Serif JP.)
5. Any constraints? (mobile / desktop, light only or dark too, must-fit-shadcn or fresh CSS.)

Then write HTML artifacts that link `colors_and_type.css` for tokens and lift component patterns from `ui_kits/`. For production code, hand the user CSS that maps the token block onto their `globals.css` shadcn slots.
