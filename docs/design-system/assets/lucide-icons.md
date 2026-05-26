# Iconography — Lucide

The codebase uses [`lucide-react`](https://lucide.dev/) exclusively. For HTML mocks and prototypes outside the React app, load via CDN:

```html
<script src="https://unpkg.com/lucide@latest"></script>
<i data-lucide="bookmark"></i>
<script>lucide.createIcons();</script>
```

## Icons used in the product

| Surface              | Icons                                                              |
| -------------------- | ------------------------------------------------------------------ |
| Header nav           | `bell`, `user`, `settings`, `lock`, `log-out`                      |
| Admin sidebar        | `layout-dashboard`, `users`, `folder-tree`, `tag`, `book-open`, `message-square`, `bar-chart-3` |
| Vocabulary           | `bookmark`, `bookmark-check`, `volume-2`, `loader-2`, `sparkles`, `search` |
| Flashcards           | `rotate-cw`, `refresh-cw`, `check-circle-2`, `bar-chart-3`, `brain`, `calendar-clock`, `flame`, `layers` |
| Conversation         | `message-square`, `mic`, `square`, `mic-2`, `chevron-left`, `chevron-right`, `arrow-left`, `check`, `x` |
| Tools                | `copy`, `file-text`, `languages`, `arrow-right-left`, `trash-2`    |
| Dashboard            | `users`, `book-open`, `folder-tree`, `tag`, `user-plus`, `activity`, `search`, `trending-up` |
| Comms                | `send`, `bell`, `check-check`, `trash-2`                           |

## Stroke and sizing

- Default `stroke-width="2"`, `viewBox="0 0 24 24"` — Lucide defaults, untouched.
- Render at `size-4` (16px) inside buttons and chips, `size-5` (20px) in standalone nav items, `size-7` (28px) in dashboard stat cards.
- Colour inherits from `currentColor`. Use `text-muted-foreground` for default chrome icons, `text-primary` for active nav, `text-destructive` for delete affordances.

## Bans

- No emoji.
- No unicode arrows or check marks as decoration.
- No filled icon variants — the only filled mark in the brand is the 日 hanko on `assets/mark.svg`.
- No hand-drawn SVGs.
