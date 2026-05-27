'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'

interface CommentComposerProps {
  placeholder?: string
  submitLabel?: string
  initialContent?: string
  onSubmit: (content: string) => Promise<void>
  onCancel?: () => void
  autoFocus?: boolean
  compact?: boolean
}

export function CommentComposer({
  placeholder = 'Viết bình luận của bạn...',
  submitLabel = 'Gửi',
  initialContent = '',
  onSubmit,
  onCancel,
  autoFocus = false,
  compact = false,
}: CommentComposerProps) {
  const [content, setContent] = useState(initialContent)
  const [busy, setBusy] = useState(false)

  async function handleSubmit() {
    const trimmed = content.trim()
    if (!trimmed || busy) return
    setBusy(true)
    try {
      await onSubmit(trimmed)
      setContent('')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="space-y-2">
      <Textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder={placeholder}
        rows={compact ? 2 : 3}
        autoFocus={autoFocus}
        disabled={busy}
        maxLength={2000}
        className="resize-none"
      />
      <div className="flex items-center justify-between">
        <p className="text-muted-foreground text-[11px]">{content.length} / 2000</p>
        <div className="flex items-center gap-2">
          {onCancel && (
            <Button type="button" variant="ghost" size="sm" onClick={onCancel} disabled={busy}>
              Huỷ
            </Button>
          )}
          <Button
            type="button"
            size="sm"
            onClick={handleSubmit}
            disabled={busy || content.trim().length === 0}
          >
            {busy ? 'Đang gửi...' : submitLabel}
          </Button>
        </div>
      </div>
    </div>
  )
}
