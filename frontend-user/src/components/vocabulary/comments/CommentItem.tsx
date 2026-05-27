'use client'

import { memo, useState } from 'react'
import { Heart, MessageCircle, MoreHorizontal, Pencil, Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import type { Comment } from '@/types/comment.types'
import { formatRelative } from './relative-time'
import { CommentComposer } from './CommentComposer'

interface CommentItemProps {
  comment: Comment
  /** Reply (depth 1) renders smaller avatar + tighter layout */
  isReply?: boolean
  onLike: (commentId: string, currentlyLiked: boolean) => void
  onReply?: (parentId: string, content: string) => Promise<void>
  onEdit?: (commentId: string, content: string) => Promise<void>
  onDelete?: (commentId: string) => void
  onExpandReplies?: (commentId: string) => void
  repliesExpanded?: boolean
  repliesLoading?: boolean
}

function CommentItemImpl({
  comment,
  isReply = false,
  onLike,
  onReply,
  onEdit,
  onDelete,
  onExpandReplies,
  repliesExpanded,
  repliesLoading,
}: CommentItemProps) {
  const [showReplyComposer, setShowReplyComposer] = useState(false)
  const [editing, setEditing] = useState(false)

  const avatarSize = isReply ? 'h-7 w-7 text-[11px]' : 'h-8 w-8 text-[13px]'

  return (
    <article className={isReply ? 'pl-2' : ''} data-comment-id={comment.commentId}>
      <div className="flex items-start gap-2.5">
        <span
          className={`bg-primary text-primary-foreground inline-flex shrink-0 items-center justify-center rounded-full font-semibold ${avatarSize}`}
          aria-hidden
        >
          {comment.user.initials}
        </span>

        <div className="min-w-0 flex-1">
          {/* Header line: name + time + edited + actions menu */}
          <div className="flex items-center gap-2">
            <p className="text-[13px] font-semibold text-[color:var(--washi-900)]">
              {comment.user.fullName}
            </p>
            <p className="text-muted-foreground text-[12px]">{formatRelative(comment.createdAt)}</p>
            {comment.edited && (
              <p className="text-muted-foreground text-[11px] italic">đã sửa</p>
            )}
            {comment.canEdit && !comment.deleted && (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="icon" className="ml-auto h-6 w-6">
                    <MoreHorizontal className="size-3.5" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem onClick={() => setEditing(true)}>
                    <Pencil className="mr-2 size-3.5" />
                    Sửa
                  </DropdownMenuItem>
                  {onDelete && (
                    <DropdownMenuItem
                      onClick={() => onDelete(comment.commentId)}
                      className="text-destructive"
                    >
                      <Trash2 className="mr-2 size-3.5" />
                      Xoá
                    </DropdownMenuItem>
                  )}
                </DropdownMenuContent>
              </DropdownMenu>
            )}
          </div>

          {/* Body */}
          {editing && onEdit ? (
            <div className="mt-1.5">
              <CommentComposer
                initialContent={comment.content}
                submitLabel="Cập nhật"
                onSubmit={async (c) => {
                  await onEdit(comment.commentId, c)
                  setEditing(false)
                }}
                onCancel={() => setEditing(false)}
                autoFocus
                compact
              />
            </div>
          ) : (
            <p
              className={`mt-1 whitespace-pre-wrap text-[14px] leading-[1.5] ${
                comment.deleted ? 'text-muted-foreground italic' : 'text-[color:var(--washi-800)]'
              }`}
            >
              {comment.content}
            </p>
          )}

          {/* Actions */}
          {!editing && !comment.deleted && (
            <div className="mt-1.5 flex items-center gap-3">
              <button
                type="button"
                onClick={() => onLike(comment.commentId, comment.liked)}
                className={`inline-flex items-center gap-1 text-[12px] font-semibold transition-colors ${
                  comment.liked
                    ? 'text-[color:var(--shu-500)]'
                    : 'text-muted-foreground hover:text-[color:var(--shu-500)]'
                }`}
                aria-pressed={comment.liked}
                aria-label={comment.liked ? 'Bỏ thích' : 'Thích'}
              >
                <Heart className={`size-3.5 ${comment.liked ? 'fill-current' : ''}`} />
                {comment.likeCount > 0 && <span>{comment.likeCount}</span>}
              </button>
              {!isReply && onReply && (
                <button
                  type="button"
                  onClick={() => setShowReplyComposer((v) => !v)}
                  className="text-muted-foreground hover:text-foreground inline-flex items-center gap-1 text-[12px] font-semibold transition-colors"
                >
                  <MessageCircle className="size-3.5" />
                  Trả lời
                </button>
              )}
            </div>
          )}

          {/* Reply composer (inline) */}
          {showReplyComposer && onReply && (
            <div className="mt-3">
              <CommentComposer
                placeholder={`Trả lời ${comment.user.fullName}...`}
                submitLabel="Trả lời"
                onSubmit={async (c) => {
                  await onReply(comment.commentId, c)
                  setShowReplyComposer(false)
                }}
                onCancel={() => setShowReplyComposer(false)}
                autoFocus
                compact
              />
            </div>
          )}

          {/* Replies expander (only on top-level with replies) */}
          {!isReply && comment.replyCount > 0 && onExpandReplies && (
            <button
              type="button"
              onClick={() => onExpandReplies(comment.commentId)}
              className="mt-2 inline-flex items-center gap-1 text-[12px] font-semibold text-[color:var(--ai-500)] hover:underline"
              data-comment-replies-toggle={comment.commentId}
            >
              {repliesLoading ? '...' : repliesExpanded ? '▾' : '▸'} {comment.replyCount} câu trả lời
            </button>
          )}
        </div>
      </div>
    </article>
  )
}

export const CommentItem = memo(CommentItemImpl)
