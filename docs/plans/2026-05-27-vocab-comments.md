# Plan — S4 Vocabulary Comment Thread

**Date:** 2026-05-27
**Status:** Approved — execute now
**Scope:** Facebook-style nested comments on `/vocabulary/[id]` detail page only

---

## 0. Goal

Cho phép learner thảo luận dưới mỗi vocabulary entry. Câu hỏi điển hình:
- "デプロイ vs リリース khác gì?"
- "Kanji 構築 nhớ thế nào cho dễ?"
- "Trong dự án mình dùng từ này hợp ngữ cảnh không?"

Mục tiêu phụ:
- Tạo community moat — vocab có discussion là asset, không ai clone được dễ
- Surface "ai cũng nhầm chỗ này" → admin biết gap nội dung

---

## 1. Non-goals

- ❌ Comment ở list page `/vocabulary` — chỉ ở detail
- ❌ Mention `@user` (v1)
- ❌ Emoji reactions ngoài 1 nút "Hữu ích" (like)
- ❌ Image/attachment trong comment
- ❌ Markdown rich text — plain text (preserve newlines)
- ❌ Real-time push (WebSocket) — page reload / explicit refresh
- ❌ Comment moderation workflow (v1 — anyone can post, admin can hard-delete)
- ❌ Nested depth > 2 levels (parent + 1 reply layer)
- ❌ Notifications khi có reply (defer to F9 smart-reminder work)

---

## 2. UX spec

### 2.1 Layout (Vocabulary detail page, dưới phần AI explanation)

```
┌─────────────────────────────────────────────────────┐
│  💬 Thảo luận     12 bình luận     [Mới nhất ▾]    │  ← header eyebrow + count + sort
├─────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────┐    │
│  │ Viết bình luận của bạn...                   │    │  ← composer (textarea, autosize)
│  │                                             │    │
│  │                              [Gửi]          │    │
│  └─────────────────────────────────────────────┘    │
│                                                     │
│  ─── COMMENT 1 (top-level) ──────────────────────   │
│  [A] Anna Nguyen · 2 giờ trước                      │
│      デプロイ vs リリース — em thấy 2 từ này hay   │
│      bị nhầm trong các bài viết tiếng Nhật...      │
│      ♥ 5  ·  Trả lời  ·  Đăng                       │  ← actions
│      ▸ 3 câu trả lời                                │  ← reply expander (collapsed)
│                                                     │
│  ─── COMMENT 2 (top-level) ──────────────────────   │
│  [B] Bao Pham · 1 ngày trước                        │
│      Trong dự án Agile, mình thường dùng リリース  │
│      cho version build, デプロイ cho action push…  │
│      ♥ 12  ·  Trả lời                               │
│      ▾ 2 câu trả lời                                │  ← expanded
│          [C] Cuong Le · 1 ngày trước                │
│              Bro cho hỏi staging có gọi là...       │
│              ♥ 1  ·  Trả lời                        │
│          [B] Bao Pham · 1 ngày trước                │
│              Staging thường là 検証環境 (kenshou…   │
│              ♥ 3  ·  Trả lời                        │
│  ─────────────────────────────────────────────────  │
│                                                     │
│  [Xem thêm 8 bình luận]                             │  ← pagination, 10/page
└─────────────────────────────────────────────────────┘
```

### 2.2 Interactions

| Action | Behaviour |
|---|---|
| Type comment → Gửi | POST, optimistic insert vào đầu list (newest-first) hoặc đúng vị trí (popular sort) |
| Click ♥ | Optimistic toggle, increment/decrement count, revert on error |
| Click "Trả lời" | Expand inline reply composer dưới comment đó |
| Click "▸ N câu trả lời" | Fetch replies (lazy), expand inline. Click lại → collapse |
| Click "Đăng" trên own comment | Show inline edit textarea (within 5min of post) |
| Click "Xoá" trên own comment | Confirm dialog → soft delete. Show "Bình luận đã xoá" placeholder if it has replies |
| Click "Xem thêm N bình luận" | Fetch page n+1, append to list |
| Sort change | Re-fetch from page 0 with new sort |

### 2.3 States

- **Empty**: "Chưa có bình luận. Bạn là người đầu tiên!"
- **Loading**: Skeleton 3 rows
- **Submitting**: Disable button + "Đang gửi…"
- **Error**: Toast "Không gửi được bình luận", keep textarea content
- **Unauthenticated** (shouldn't happen on (app)/ route): redirect to login
- **Deleted comment with replies**: "[Bình luận đã xoá]" placeholder, replies still visible
- **Deleted comment without replies**: removed entirely from list

### 2.4 Reply depth rule

- Top-level comment (depth 0)
- Reply to top-level (depth 1) — indented, smaller avatar
- "Reply" on a depth-1 comment → opens composer that posts AS depth 1 (same parent), prefixed with `@username` text mention (plain string, no link). Avoid infinite depth.

---

## 3. Data model

### 3.1 Tables

`learning_service.vocab_comments`
```sql
CREATE TABLE vocab_comments (
    comment_id        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    vocab_id          UUID         NOT NULL REFERENCES vocabulary(vocab_id) ON DELETE CASCADE,
    parent_comment_id UUID         REFERENCES vocab_comments(comment_id) ON DELETE CASCADE,
    user_id           UUID         NOT NULL,   -- cross-service ref, no FK
    content           TEXT         NOT NULL,
    like_count        INTEGER      NOT NULL DEFAULT 0,   -- denormalized
    reply_count       INTEGER      NOT NULL DEFAULT 0,   -- denormalized, only on top-level rows
    deleted_at        TIMESTAMP,                          -- soft delete
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    created_by        VARCHAR(64),
    updated_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_by        VARCHAR(64),
    CONSTRAINT chk_content_length CHECK (char_length(content) BETWEEN 1 AND 2000),
    CONSTRAINT chk_reply_depth     CHECK (parent_comment_id IS NULL OR parent_comment_id <> comment_id)
);

-- top-level listing by newest, scoped per vocab
CREATE INDEX idx_comments_vocab_toplevel_newest
    ON vocab_comments (vocab_id, created_at DESC)
    WHERE parent_comment_id IS NULL AND deleted_at IS NULL;

-- top-level listing by popularity, scoped per vocab
CREATE INDEX idx_comments_vocab_toplevel_top
    ON vocab_comments (vocab_id, like_count DESC, created_at DESC)
    WHERE parent_comment_id IS NULL AND deleted_at IS NULL;

-- replies lookup
CREATE INDEX idx_comments_parent
    ON vocab_comments (parent_comment_id, created_at ASC)
    WHERE parent_comment_id IS NOT NULL AND deleted_at IS NULL;
```

`learning_service.vocab_comment_likes`
```sql
CREATE TABLE vocab_comment_likes (
    comment_id UUID      NOT NULL REFERENCES vocab_comments(comment_id) ON DELETE CASCADE,
    user_id    UUID      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (comment_id, user_id)
);

-- "did user X like comment Y" lookup
CREATE INDEX idx_comment_likes_user ON vocab_comment_likes (user_id, comment_id);
```

### 3.2 Why denormalised counts

`reply_count` and `like_count` are kept up-to-date on every write (insert/delete reply, insert/delete like). This trades a small write cost for:
- O(1) "X câu trả lời" badge — no `COUNT(*) GROUP BY parent_comment_id`
- O(1) sort by popularity — index covers `(vocab_id, like_count DESC)`
- O(1) total count for the eyebrow — sum over the page, or one cached query

Race condition: 2 likes arriving at the same instant → use `UPDATE … SET like_count = like_count + 1` (atomic). Confirmed safe at our load.

### 3.3 Soft delete

`deleted_at IS NOT NULL` means "deleted". Reasoning:
- Preserve thread coherence (if a parent is removed, replies still visible)
- Audit & abuse trail
- Reply badge accuracy depends on excluding deleted

Hard delete: admin-only path (`DELETE … WHERE comment_id = ? AND user has ROLE_ADMIN`).

---

## 4. API

All endpoints under `learning-service`, gated by JWT (gateway already validates). User id is read from `X-User-Id` header via `UserAuthUtil`.

| Method | Path | Auth | Returns |
|---|---|---|---|
| `GET` | `/api/v1/learning/vocabulary/{vocabId}/comments?sort=newest\|top&page=0&size=10` | USER | `{ content, page, totalElements, totalComments }` |
| `GET` | `/api/v1/learning/comments/{commentId}/replies?page=0&size=5` | USER | `{ content, page, totalElements }` |
| `POST` | `/api/v1/learning/vocabulary/{vocabId}/comments` | USER | `CommentDto` (new) |
| `PUT` | `/api/v1/learning/comments/{commentId}` | USER (own) | `CommentDto` |
| `DELETE` | `/api/v1/learning/comments/{commentId}` | USER (own) or ADMIN | `204` |
| `POST` | `/api/v1/learning/comments/{commentId}/like` | USER | `{ likeCount, liked: true }` |
| `DELETE` | `/api/v1/learning/comments/{commentId}/like` | USER | `{ likeCount, liked: false }` |

### 4.1 Request shapes

```ts
// POST /vocabulary/{vocabId}/comments
interface CreateCommentRequest {
  content: string                    // 1-2000 chars, trimmed
  parentCommentId?: string | null    // null = top-level
}

// PUT /comments/{commentId}
interface UpdateCommentRequest {
  content: string                    // edit window not enforced server-side v1
}
```

### 4.2 Response shape

```ts
interface CommentDto {
  commentId: string
  vocabId: string
  parentCommentId: string | null
  user: {
    userId: string
    fullName: string
    initials: string     // computed server-side
    // profilePicture?: string — future
  }
  content: string
  likeCount: number
  replyCount: number     // 0 if this is a reply (depth > 0)
  liked: boolean         // did the CURRENT user like this?
  edited: boolean        // updatedAt != createdAt
  deleted: boolean       // true if soft-deleted (content replaced with "[đã xoá]")
  createdAt: string      // ISO
  updatedAt: string
}

interface CommentPage {
  content: CommentDto[]
  page: number
  size: number
  totalElements: number
  totalComments: number  // total top-level + replies for this vocab (only on the vocab endpoint)
}
```

### 4.3 Validation

- `content`: trim, reject empty after trim, max 2000 chars (DB constraint enforces upper bound; controller enforces lower)
- `parentCommentId`: if provided, must exist, must belong to same `vocabId`, must itself be top-level (no depth > 1)
- Rate limit: 5 posts / 60s per user via existing `RateLimitFilter` on POST endpoint

### 4.4 N+1 concern

Listing 10 comments must NOT fire 10 separate "did current user like" queries.

Strategy: when serving the list, single batch query `SELECT comment_id FROM vocab_comment_likes WHERE user_id = ? AND comment_id IN (?, …)` → build a Set → annotate each DTO. Same for replies.

User lookup: every comment carries a `userId`. We need the user's name. Options:
- (A) FeignClient to user-service per request — slow + chatty
- (B) Cache user names in learning-service in a small Caffeine cache keyed by userId — TTL 10 min
- (C) Denormalise: store `user_full_name` in the comment row at creation time

Pick **(B)** — caffeine is already on the classpath, simplest, handles name changes gracefully (with TTL).

---

## 5. Backend implementation

### 5.1 Files (learning-service)

```
io/github/ndtung723/nihongoit/learningservice/
├── entity/
│   ├── VocabCommentEntity.kt
│   └── VocabCommentLikeEntity.kt
├── repository/
│   ├── VocabCommentRepository.kt
│   └── VocabCommentLikeRepository.kt
├── dto/
│   ├── CommentDto.kt
│   ├── CommentPageDto.kt
│   ├── CreateCommentRequest.kt
│   └── UpdateCommentRequest.kt
├── service/
│   ├── VocabCommentService.kt
│   └── UserNameCacheService.kt   ← Feign + Caffeine
├── controller/
│   └── VocabCommentController.kt
└── feign/
    └── UserNameFeignClient.kt
```

### 5.2 Migration

`V9__vocab_comments.sql` — DDL from §3.1.

### 5.3 Key service methods

```kotlin
@Transactional(readOnly = true)
fun listForVocab(vocabId: UUID, sort: Sort, page: Pageable, currentUserId: UUID): CommentPage

@Transactional(readOnly = true)
fun listReplies(parentId: UUID, page: Pageable, currentUserId: UUID): Page<CommentDto>

@Transactional
fun create(vocabId: UUID, req: CreateCommentRequest, currentUserId: UUID): CommentDto
//   - if parentCommentId is set: validate parent.vocabId == vocabId && parent.parentCommentId == null
//   - insert, then UPDATE parent SET reply_count = reply_count + 1 WHERE comment_id = parent
//   - flush, fetch back with computed fields

@Transactional
fun softDelete(commentId: UUID, currentUserId: UUID, isAdmin: Boolean)
//   - SELECT for update
//   - require user_id == currentUserId OR isAdmin
//   - set deleted_at = now(), keep content (audit), but DTO output masks content
//   - decrement parent.reply_count if this was a reply

@Transactional
fun like(commentId: UUID, currentUserId: UUID): LikeResult
//   - INSERT INTO vocab_comment_likes ... ON CONFLICT DO NOTHING
//   - if inserted: UPDATE comment SET like_count = like_count + 1
//   - return new count + liked=true
```

### 5.4 Unit tests (≥6 cases)

1. `create top-level comment increments totalElements` — happy path
2. `create reply increments parent.reply_count` — verify the denormalisation
3. `cannot reply to a reply (depth limit)` — should 400 with code `COMMENT_DEPTH_EXCEEDED`
4. `like is idempotent` — calling twice doesn't increment twice
5. `soft delete preserves replies` — replies still listable, parent shows "[đã xoá]"
6. `list paginates correctly across 25 comments, page size 10` — last page has 5

---

## 6. Frontend implementation

### 6.1 Files (frontend-user)

```
src/
├── types/comment.types.ts
├── services/comment.service.ts
└── components/vocabulary/comments/
    ├── CommentSection.tsx       ← orchestrator on detail page
    ├── CommentList.tsx          ← top-level list + "load more"
    ├── CommentItem.tsx          ← single comment + actions
    ├── CommentReplies.tsx       ← lazy-loaded reply thread
    ├── CommentComposer.tsx      ← textarea + submit, used for both new + reply
    └── CommentSortPicker.tsx    ← shadcn Select wrapping sort options
```

### 6.2 State / data flow

`CommentSection` owns:
- `sort: 'newest' | 'top'`
- `pages: CommentDto[][]` (one array per loaded page)
- `totalComments: number`
- expanded reply state: `Map<commentId, RepliesState>` where `RepliesState = { loaded: boolean, expanded: boolean, replies: CommentDto[], page: number }`

Pagination: append-only. Sort change wipes the list and re-fetches from page 0.

Optimistic mutations:
- **Post**: insert DTO at top of pages[0] before server confirms. On 4xx/5xx: pop it back out + toast.
- **Like**: increment `likeCount` + flip `liked` immediately. Revert on error.
- **Delete**: optimistic remove (if no replies) OR mark deleted (if replies). Revert on error.

### 6.3 Perf budget

| Scenario | Target |
|---|---|
| Initial render of section | < 200 ms after data lands |
| Click "Xem thêm 8 bình luận" | network ≤ 300 ms, render ≤ 50 ms |
| Click ♥ | UI updates < 16 ms (optimistic) |
| Re-render after sort change | < 100 ms |

**Tactics:**
- `React.memo` on `CommentItem` so a like on one item doesn't re-render siblings
- Stable keys (`commentId`)
- Avoid recreating `onClick` handlers — move out via `useCallback`
- Use the existing `useDebounce` for textarea (if we later add autosave; not v1)
- Skeleton placeholder (3 rows of `bg-muted h-16`) during first load — no layout jump

### 6.4 Empty state copy

- No comments: **"Chưa có bình luận. Bạn là người đầu tiên!"**
- Has comments but no replies on a comment: just hide the "X câu trả lời" expander
- Optimistic failed: toast **"Không gửi được bình luận"** (per design system rule — own the fault, no apology)

### 6.5 Visual tokens

Reuse design system:
- Avatar: `bg-primary text-primary-foreground` initials, 32px top-level / 24px reply
- Time stamp: `text-muted-foreground text-[12px]` via `Intl.RelativeTimeFormat('vi')`
- Like button: ghost variant; active state = `text-[color:var(--shu-500)]` + filled heart
- "X câu trả lời" expander: small chevron + count, `text-[color:var(--ai-500)]` link style
- Hover state on comment: subtle `hover:bg-[color:var(--washi-100)]` block (matches the "list item" treatment elsewhere)

---

## 7. Testing

### 7.1 BE unit tests
Listed in §5.4.

### 7.2 FE component tests
Skip for v1 (we don't have RTL set up yet outside auth.store tests). Defer to follow-up.

### 7.3 E2E (Playwright)

`e2e/tests/user/vocab-comments.spec.ts`:
1. **TC-S4-01 post a top-level comment and see it appear** @smoke
   - Seed a vocab via api-admin
   - Login as user
   - Goto /vocabulary/{id}
   - Find CommentComposer, fill, submit
   - Expect new comment visible at top of list
   - Expect total count incremented
2. **TC-S4-02 like + unlike toggles the counter and persists across reload**
   - Post a comment (or use one from the seed)
   - Click ♥ → count goes from 0 → 1, button shows liked state
   - Reload page → still 1 + liked
   - Click again → 0 + unliked
3. **TC-S4-03 reply expander lazy-loads replies**
   - Vocab with 1 top-level + 2 replies (set up via API)
   - Goto detail page
   - "▸ 2 câu trả lời" visible, replies NOT in DOM
   - Click expander → both replies render
   - `page.evaluate(() => document.querySelectorAll('[data-comment-reply]').length)` returns 2
4. **TC-S4-04 sort by popularity reshuffles list**
   - 3 comments with different like counts
   - Default sort = newest → see them in created order
   - Switch to "Nổi bật" → see them in like-count order

### 7.4 Performance assertion

In TC-S4-03, also measure: after click, network request count should be exactly 1 (the replies GET) — no rogue refetches.

```ts
const requests: string[] = []
page.on('request', (r) => requests.push(r.url()))
await page.getByRole('button', { name: /câu trả lời/ }).click()
await page.waitForResponse(/\/comments\/.*\/replies/)
expect(requests.filter((u) => u.includes('/comments/'))).toHaveLength(1)
```

---

## 8. Performance & ops notes

| Concern | Mitigation |
|---|---|
| Hot vocab with 500+ comments | Pagination caps at 50 page-loads (5000 visible). Beyond that, archive. |
| Spam/abuse | Rate-limit (5/60s) at gateway. Admin hard-delete. v2 = report button |
| N+1 user name fetch | Caffeine cache w/ 10min TTL (§4.4 option B) |
| Like burst | `like_count` UPDATE is atomic. Race-safe |
| Soft-deleted clutter | Indexes have `WHERE deleted_at IS NULL` partial predicate — soft-deleted rows don't bloat the scan |
| Comment edit history | NOT stored v1 — only `updatedAt`. Add `comment_revisions` table if requested |

---

## 9. Rollout

Single feature flag `app.comments.enabled` (default `true` in dev, `true` in prod after smoke). If anything goes wrong, flip to `false` → API returns 404, UI hides the section.

---

## 10. Estimate

- BE (entity / migration / service / controller / tests): **1.5 day**
- FE (5 components / service / state / optimistic UI): **2 days**
- E2E (4 specs): **0.5 day**
- Polish + buffer: **0.5 day**

**Total: ~4.5 days.** This session targets BE + FE happy path + 1 smoke E2E; the other 3 specs are follow-ups.

---

## 11. Commit plan

1. `feat(be): S4 — vocab comments entity, migration, service, controller, unit tests`
2. `feat(fe): S4 — vocab comments UI on detail page (composer, list, replies, sort)`
3. `test(e2e): S4 — vocab-comments smoke (TC-S4-01 post + appear)`
4. `chore(e2e): S4 — extended specs (like toggle, lazy replies, sort)` — follow-up
