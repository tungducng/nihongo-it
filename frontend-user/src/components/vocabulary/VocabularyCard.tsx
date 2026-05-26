'use client'

import Link from 'next/link'
import { Bookmark, BookmarkCheck } from 'lucide-react'
import { useState } from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import vocabularyService from '@/services/vocabulary.service'
import { useAppToast } from '@/hooks/useAppToast'
import { extractApiError } from '@/types/common.types'
import type { VocabularyItem } from '@/types/learning.types'

interface VocabularyCardProps {
  item: VocabularyItem
  onSavedChange?: (vocabId: string, isSaved: boolean) => void
}

export function VocabularyCard({ item, onSavedChange }: VocabularyCardProps) {
  const [isSaved, setIsSaved] = useState(item.isSaved)
  const [busy, setBusy] = useState(false)
  const toast = useAppToast()

  async function handleToggle(e: React.MouseEvent) {
    e.preventDefault()
    e.stopPropagation()
    if (busy) return
    setBusy(true)
    const next = !isSaved
    // Optimistic toggle
    setIsSaved(next)
    try {
      if (next) await vocabularyService.saveVocabulary(item.vocabId)
      else await vocabularyService.removeSavedVocabulary(item.vocabId)
      onSavedChange?.(item.vocabId, next)
    } catch (err) {
      setIsSaved(!next) // revert
      toast.error(extractApiError(err, 'Không cập nhật được trạng thái'))
    } finally {
      setBusy(false)
    }
  }

  const jlptClass =
    item.jlptLevel === 'N5'
      ? 'jlpt-n5'
      : item.jlptLevel === 'N4'
        ? 'jlpt-n4'
        : item.jlptLevel === 'N3'
          ? 'jlpt-n3'
          : item.jlptLevel === 'N2'
            ? 'jlpt-n2'
            : 'jlpt-n1'

  return (
    <Link href={`/vocabulary/${item.vocabId}`} className="block">
      <Card className="hover:border-primary/50 h-full transition-colors">
        <CardContent className="p-4">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0 flex-1">
              <h3 className="font-jp truncate text-[22px] font-semibold leading-tight text-[color:var(--washi-900)]">
                {item.term}
              </h3>
              {item.pronunciation && (
                <p className="font-jp text-muted-foreground mt-1 truncate text-[13px]">
                  {item.pronunciation}
                </p>
              )}
            </div>
            <Button
              size="icon"
              variant="ghost"
              onClick={handleToggle}
              disabled={busy}
              aria-label={isSaved ? 'Bỏ lưu' : 'Lưu từ vựng'}
              className="-mr-2 h-8 w-8 shrink-0"
            >
              {isSaved ? (
                <BookmarkCheck className="size-4 text-[color:var(--ai-500)]" />
              ) : (
                <Bookmark className="text-muted-foreground size-4" />
              )}
            </Button>
          </div>
          <p className="mt-2.5 line-clamp-2 min-h-[38px] text-[13px] leading-snug text-[color:var(--washi-800)]">
            {item.meaning}
          </p>
          <div className="mt-3 flex items-center gap-1.5">
            <span
              className={`${jlptClass} inline-flex h-[22px] items-center rounded-full px-2.5 text-[11px] font-semibold`}
            >
              {item.jlptLevel}
            </span>
            {item.topicName && (
              <Badge variant="outline" className="h-[22px] truncate px-2.5 text-[11px] font-semibold">
                {item.topicName}
              </Badge>
            )}
          </div>
        </CardContent>
      </Card>
    </Link>
  )
}
