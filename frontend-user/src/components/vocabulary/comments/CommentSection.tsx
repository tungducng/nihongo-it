'use client'

import { useCallback, useEffect, useState } from 'react'
import { MessageSquare } from 'lucide-react'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Button } from '@/components/ui/button'
import { useAppToast } from '@/hooks/useAppToast'
import { useConfirm } from '@/hooks/useConfirm'
import { extractApiError } from '@/types/common.types'
import commentService from '@/services/comment.service'
import type { Comment, CommentSort } from '@/types/comment.types'
import { CommentComposer } from './CommentComposer'
import { CommentItem } from './CommentItem'

interface CommentSectionProps {
  vocabId: string
}

interface RepliesState {
  loaded: boolean
  loading: boolean
  expanded: boolean
  replies: Comment[]
}

const PAGE_SIZE = 10

export function CommentSection({ vocabId }: CommentSectionProps) {
  const toast = useAppToast()
  const confirm = useConfirm()

  const [sort, setSort] = useState<CommentSort>('newest')
  const [comments, setComments] = useState<Comment[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalComments, setTotalComments] = useState(0)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [replies, setReplies] = useState<Record<string, RepliesState>>({})

  const loadPage = useCallback(
    async (nextSort: CommentSort, nextPage: number, append: boolean) => {
      if (append) setLoadingMore(true)
      else setLoading(true)
      try {
        const data = await commentService.listForVocab(vocabId, nextSort, nextPage, PAGE_SIZE)
        setComments((prev) => (append ? [...prev, ...data.content] : data.content))
        setPage(nextPage)
        setTotalPages(data.totalPages)
        setTotalComments(data.totalComments ?? data.totalElements)
      } catch (err) {
        toast.error(extractApiError(err, 'Không tải được bình luận'))
      } finally {
        setLoading(false)
        setLoadingMore(false)
      }
    },
    // toast is a fresh object each render (useAppToast returns a literal); excluding
    // it intentionally — otherwise this callback's identity changes every render and
    // the useEffect below re-fires + resets `loading` indefinitely.
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [vocabId],
  )

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void loadPage(sort, 0, false)
  }, [sort, loadPage])

  const handleSortChange = (next: string) => {
    if (next === 'newest' || next === 'top') {
      setReplies({}) // invalidate expanded threads
      setSort(next)
    }
  }

  const handleCreate = async (content: string) => {
    try {
      const created = await commentService.create(vocabId, content, null)
      setComments((prev) => [created, ...prev])
      setTotalComments((n) => n + 1)
      toast.success('Đã đăng bình luận')
    } catch (err) {
      toast.error(extractApiError(err, 'Không gửi được bình luận'))
      throw err
    }
  }

  const handleReply = async (parentId: string, content: string) => {
    try {
      const created = await commentService.create(vocabId, content, parentId)
      setComments((prev) =>
        prev.map((c) => (c.commentId === parentId ? { ...c, replyCount: c.replyCount + 1 } : c)),
      )
      setReplies((prev) => {
        const current = prev[parentId] ?? { loaded: true, loading: false, expanded: true, replies: [] }
        return {
          ...prev,
          [parentId]: {
            ...current,
            loaded: true,
            expanded: true,
            replies: [...current.replies, created],
          },
        }
      })
      setTotalComments((n) => n + 1)
      toast.success('Đã trả lời')
    } catch (err) {
      toast.error(extractApiError(err, 'Không gửi được trả lời'))
      throw err
    }
  }

  const handleLike = useCallback(
    async (commentId: string, currentlyLiked: boolean) => {
      // Optimistic toggle
      const apply = (delta: number, liked: boolean) =>
        (c: Comment): Comment =>
          c.commentId === commentId
            ? { ...c, likeCount: Math.max(0, c.likeCount + delta), liked }
            : c
      setComments((prev) => prev.map(apply(currentlyLiked ? -1 : 1, !currentlyLiked)))
      setReplies((prev) => {
        const next: typeof prev = {}
        for (const [k, v] of Object.entries(prev)) {
          next[k] = { ...v, replies: v.replies.map(apply(currentlyLiked ? -1 : 1, !currentlyLiked)) }
        }
        return next
      })

      try {
        const result = currentlyLiked
          ? await commentService.unlike(commentId)
          : await commentService.like(commentId)
        // Sync with server (handles concurrent likes)
        const sync = (c: Comment): Comment =>
          c.commentId === commentId ? { ...c, likeCount: result.likeCount, liked: result.liked } : c
        setComments((prev) => prev.map(sync))
        setReplies((prev) => {
          const next: typeof prev = {}
          for (const [k, v] of Object.entries(prev)) {
            next[k] = { ...v, replies: v.replies.map(sync) }
          }
          return next
        })
      } catch (err) {
        // Revert optimistic
        setComments((prev) => prev.map(apply(currentlyLiked ? 1 : -1, currentlyLiked)))
        setReplies((prev) => {
          const next: typeof prev = {}
          for (const [k, v] of Object.entries(prev)) {
            next[k] = { ...v, replies: v.replies.map(apply(currentlyLiked ? 1 : -1, currentlyLiked)) }
          }
          return next
        })
        toast.error(extractApiError(err, 'Không cập nhật được'))
      }
    },
    [toast],
  )

  const handleEdit = async (commentId: string, content: string) => {
    try {
      const updated = await commentService.update(commentId, content)
      setComments((prev) => prev.map((c) => (c.commentId === commentId ? updated : c)))
      setReplies((prev) => {
        const next: typeof prev = {}
        for (const [k, v] of Object.entries(prev)) {
          next[k] = { ...v, replies: v.replies.map((c) => (c.commentId === commentId ? updated : c)) }
        }
        return next
      })
      toast.success('Đã cập nhật')
    } catch (err) {
      toast.error(extractApiError(err, 'Không cập nhật được'))
      throw err
    }
  }

  const handleDelete = async (commentId: string) => {
    const ok = await confirm({
      title: 'Xoá bình luận?',
      message: 'Hành động này không thể hoàn tác.',
      confirmText: 'Xoá',
      variant: 'destructive',
    })
    if (!ok) return
    try {
      await commentService.delete(commentId)
      // Mark deleted in place — soft delete keeps the row visible if it has replies
      const markDeleted = (c: Comment): Comment =>
        c.commentId === commentId
          ? { ...c, deleted: true, content: '[Bình luận đã xoá]', canEdit: false }
          : c
      setComments((prev) =>
        prev.map(markDeleted).filter((c) => !(c.commentId === commentId && c.replyCount === 0)),
      )
      setReplies((prev) => {
        const next: typeof prev = {}
        for (const [k, v] of Object.entries(prev)) {
          next[k] = {
            ...v,
            replies: v.replies.filter((c) => c.commentId !== commentId),
          }
        }
        return next
      })
      setTotalComments((n) => Math.max(0, n - 1))
      toast.success('Đã xoá')
    } catch (err) {
      toast.error(extractApiError(err, 'Không xoá được'))
    }
  }

  const handleExpandReplies = async (commentId: string) => {
    const current = replies[commentId]
    if (current?.expanded) {
      setReplies((prev) => ({ ...prev, [commentId]: { ...current, expanded: false } }))
      return
    }
    if (current?.loaded) {
      setReplies((prev) => ({ ...prev, [commentId]: { ...current, expanded: true } }))
      return
    }
    setReplies((prev) => ({
      ...prev,
      [commentId]: { loaded: false, loading: true, expanded: false, replies: [] },
    }))
    try {
      const data = await commentService.listReplies(commentId, 0, 50)
      setReplies((prev) => ({
        ...prev,
        [commentId]: { loaded: true, loading: false, expanded: true, replies: data.content },
      }))
    } catch (err) {
      setReplies((prev) => ({
        ...prev,
        [commentId]: { loaded: false, loading: false, expanded: false, replies: [] },
      }))
      toast.error(extractApiError(err, 'Không tải được câu trả lời'))
    }
  }

  return (
    <section className="space-y-4">
      <header className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <MessageSquare className="size-4 text-[color:var(--ai-500)]" />
          <h2 className="text-[15px] font-semibold text-[color:var(--washi-900)]">Thảo luận</h2>
          <span className="text-muted-foreground text-[13px]">
            {totalComments} bình luận
          </span>
        </div>
        <Select value={sort} onValueChange={handleSortChange}>
          <SelectTrigger className="h-8 w-[140px] text-[12px]" aria-label="Sắp xếp">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="newest">Mới nhất</SelectItem>
            <SelectItem value="top">Nổi bật</SelectItem>
          </SelectContent>
        </Select>
      </header>

      <CommentComposer onSubmit={handleCreate} />

      {loading ? (
        <div className="space-y-3">
          {[0, 1, 2].map((i) => (
            <div key={i} className="bg-muted h-16 animate-pulse rounded-md" />
          ))}
        </div>
      ) : comments.length === 0 ? (
        <p className="text-muted-foreground py-6 text-center text-[13px]">
          Chưa có bình luận. Bạn là người đầu tiên!
        </p>
      ) : (
        <ul className="space-y-4 border-t border-[color:var(--washi-200)] pt-4">
          {comments.map((c) => {
            const replyState = replies[c.commentId]
            return (
              <li key={c.commentId} className="space-y-3" data-comment-toplevel>
                <CommentItem
                  comment={c}
                  onLike={handleLike}
                  onReply={handleReply}
                  onEdit={handleEdit}
                  onDelete={handleDelete}
                  onExpandReplies={handleExpandReplies}
                  repliesExpanded={replyState?.expanded ?? false}
                  repliesLoading={replyState?.loading ?? false}
                />
                {replyState?.expanded && (
                  <ul className="ml-10 space-y-3 border-l border-[color:var(--washi-200)] pl-3.5">
                    {replyState.replies.map((r) => (
                      <li key={r.commentId} data-comment-reply>
                        <CommentItem
                          comment={r}
                          isReply
                          onLike={handleLike}
                          onEdit={handleEdit}
                          onDelete={handleDelete}
                        />
                      </li>
                    ))}
                  </ul>
                )}
              </li>
            )
          })}
        </ul>
      )}

      {page + 1 < totalPages && (
        <div className="flex justify-center">
          <Button
            variant="outline"
            size="sm"
            disabled={loadingMore}
            onClick={() => void loadPage(sort, page + 1, true)}
          >
            {loadingMore ? 'Đang tải...' : `Xem thêm ${totalComments - comments.length} bình luận`}
          </Button>
        </div>
      )}
    </section>
  )
}
