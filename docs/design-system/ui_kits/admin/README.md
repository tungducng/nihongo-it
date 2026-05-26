# Admin UI Kit — Nihongo IT

Click-through recreation of the admin app (`frontend-admin`). Lands on the Dashboard; the sidebar lets you navigate to Users, Vocabulary, and placeholder screens for Categories / Topics / Conversations / Statistics (those reuse the same table + filter pattern).

## Files

```
ui-primitives.jsx          — shared Button/Input/Card/Badge/Icon set
AdminShell.jsx             — AdminSidebar + AdminTopbar
AdminDashboard.jsx         — Stat-card grids + recent activity feed
AdminUsersTable.jsx        — Searchable users table with role chips, JLPT, status
AdminVocabularyTable.jsx   — Vocab table with JLPT filter and row actions
kit.css                    — Cosmetic shared with user kit
admin.css                  — Sidebar shell, table styles, status pill
index.html                 — Mounts everything
```

## What it covers

- Full admin shell — 240px sidebar with active state on solid `--ai-500`, footer profile slot, and a crumb topbar.
- Dashboard with two stat grids ("Tổng quan" / "Hôm nay") and an activity feed.
- Two data tables (Users, Vocabulary) demonstrating: avatar+name cell, role/JLPT chips, status pills with a dot indicator, row hover, row actions (edit/delete), search + filter toolbar.

## What it skips

- Modals / drawers for create/edit (would use Card + form primitives from `ui-primitives.jsx`).
- Bulk-select checkbox column.
- Real pagination — the table renders all rows in the mock set.
- The Conversations, Categories, Topics, Statistics admin screens render a placeholder noting that they reuse the same patterns.
