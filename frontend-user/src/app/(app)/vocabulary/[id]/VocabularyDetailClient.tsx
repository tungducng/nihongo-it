'use client'

import { useCallback, useEffect, useState } from 'react'
import Link from 'next/link'
import { ArrowLeft, Bookmark, BookmarkCheck, Sparkles } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Loader } from '@/components/common/Loader'
import { AudioButton } from '@/components/vocabulary/AudioButton'
import { AIChat } from '@/components/vocabulary/AIChat'
import { VocabularyCard } from '@/components/vocabulary/VocabularyCard'
import { useVocabularyStore } from '@/stores/vocabulary.store'
import aiService from '@/services/ai.service'
import { useAppToast } from '@/hooks/useAppToast'
import { extractApiError } from '@/types/common.types'

interface Props {
  vocabId: string
}

export function VocabularyDetailClient({ vocabId }: Props) {
  const toast = useAppToast()
  const vocab = useVocabularyStore((s) => s.currentVocabulary)
  const related = useVocabularyStore((s) => s.relatedVocabulary)
  const loading = useVocabularyStore((s) => s.loading)
  const fetchVocabularyById = useVocabularyStore((s) => s.fetchVocabularyById)
  const toggleFavorite = useVocabularyStore((s) => s.toggleFavorite)
  const reset = useVocabularyStore((s) => s.reset)

  const [aiExplanation, setAiExplanation] = useState<string | null>(null)
  const [explanationLoading, setExplanationLoading] = useState(false)

  useEffect(() => {
    void fetchVocabularyById(vocabId).catch((err) => {
      toast.error(err instanceof Error ? err.message : 'Lỗi tải dữ liệu')
    })
    return () => reset()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [vocabId])

  const loadAiExplanation = useCallback(async () => {
    if (!vocab || explanationLoading) return
    setExplanationLoading(true)
    try {
      const res = await aiService.explainVocabulary(
        vocab.term,
        vocab.pronunciation,
        vocab.meaning,
        vocab.topicName,
        vocab.example,
      )
      setAiExplanation(res.explanation)
    } catch (err) {
      toast.error(extractApiError(err, 'Không tải được giải thích AI'))
    } finally {
      setExplanationLoading(false)
    }
  }, [vocab, explanationLoading, toast])

  async function handleToggleFavorite() {
    try {
      await toggleFavorite()
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Không cập nhật được')
    }
  }

  if (loading && !vocab) return <Loader label="Đang tải..." />
  if (!vocab) {
    return (
      <div className="space-y-4 py-10 text-center">
        <p className="text-muted-foreground">Không tìm thấy từ vựng.</p>
        <Button asChild variant="outline">
          <Link href="/vocabulary">← Quay về danh sách</Link>
        </Button>
      </div>
    )
  }

  const jlptClass =
    vocab.jlptLevel === 'N5'
      ? 'jlpt-n5'
      : vocab.jlptLevel === 'N4'
        ? 'jlpt-n4'
        : vocab.jlptLevel === 'N3'
          ? 'jlpt-n3'
          : vocab.jlptLevel === 'N2'
            ? 'jlpt-n2'
            : 'jlpt-n1'

  return (
    <div className="space-y-6">
      <Button asChild variant="ghost" size="sm" className="-ml-2">
        <Link href="/vocabulary">
          <ArrowLeft className="mr-1 size-4" />
          Danh sách từ vựng
        </Link>
      </Button>

      <Card className="px-9 py-8">
        <CardContent className="p-0">
          <div className="flex items-start justify-between gap-4">
            <div className="min-w-0 flex-1">
              {vocab.pronunciation ? (
                <p className="font-jp-serif font-medium text-[56px] leading-[1.05] text-[color:var(--washi-900)]">
                  <ruby>
                    {vocab.term}
                    <rt>{vocab.pronunciation}</rt>
                  </ruby>
                </p>
              ) : (
                <p className="font-jp-serif font-medium text-[56px] leading-[1.05] text-[color:var(--washi-900)]">
                  {vocab.term}
                </p>
              )}
              <div className="mt-3.5 flex flex-wrap items-center gap-2">
                <span
                  className={`${jlptClass} inline-flex h-[22px] items-center rounded-full px-2.5 text-[11px] font-semibold`}
                >
                  {vocab.jlptLevel}
                </span>
                {vocab.topicName && (
                  <Badge variant="outline" className="h-[22px] px-2.5 text-[11px] font-semibold">
                    {vocab.topicName}
                  </Badge>
                )}
                <AudioButton text={vocab.term} contentType="vocabulary" />
              </div>
            </div>
            <Button
              variant={vocab.isSaved ? 'default' : 'outline'}
              onClick={handleToggleFavorite}
              aria-label={vocab.isSaved ? 'Bỏ lưu' : 'Lưu từ'}
            >
              {vocab.isSaved ? (
                <>
                  <BookmarkCheck className="mr-2 size-4" />
                  Đã lưu
                </>
              ) : (
                <>
                  <Bookmark className="mr-2 size-4" />
                  Lưu
                </>
              )}
            </Button>
          </div>

          <hr className="border-border my-7 border-t" />

          <div className="grid gap-6">
            <section>
              <p className="eyebrow mb-2.5">Nghĩa</p>
              <p className="text-[15px] leading-[1.6] text-[color:var(--washi-800)]">
                {vocab.meaning}
              </p>
            </section>

            {vocab.example && (
              <section>
                <p className="eyebrow mb-2.5">Ví dụ</p>
                <div className="border-border border-t">
                  <div className="border-border flex items-start gap-3 border-b py-3">
                    <div className="flex-1">
                      <p className="font-jp text-[16px] text-[color:var(--washi-800)]">
                        {vocab.example}
                      </p>
                      {vocab.exampleMeaning && (
                        <p className="text-muted-foreground mt-1 text-[13px]">
                          {vocab.exampleMeaning}
                        </p>
                      )}
                    </div>
                    <AudioButton text={vocab.example} contentType="example" size="sm" />
                  </div>
                </div>
              </section>
            )}

            <section>
              <div className="mb-2.5 flex items-center justify-between">
                <p className="eyebrow flex items-center gap-1.5">
                  Trợ lý AI
                  <Sparkles className="size-3 text-[color:var(--shu-500)]" />
                </p>
                {!aiExplanation && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={loadAiExplanation}
                    disabled={explanationLoading}
                  >
                    {explanationLoading ? 'Đang tạo...' : 'Tạo giải thích'}
                  </Button>
                )}
              </div>
              {aiExplanation ? (
                <Card className="border-[color:oklch(from_var(--ai-500)_l_c_h_/_30%)] bg-[color:var(--ai-50)] p-4">
                  <p className="whitespace-pre-wrap text-[13.5px] leading-[1.55] text-[color:var(--washi-800)]">
                    {aiExplanation}
                  </p>
                </Card>
              ) : (
                <p className="text-muted-foreground text-[13px]">
                  Nhấn &ldquo;Tạo giải thích&rdquo; để AI giải thích chi tiết về từ này.
                </p>
              )}
            </section>
          </div>
        </CardContent>
      </Card>

      <AIChat vocabWord={vocab.term} />

      {related.length > 0 && (
        <section className="space-y-3">
          <p className="eyebrow">Từ liên quan</p>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            {related.map((item) => (
              <VocabularyCard key={item.vocabId} item={item} />
            ))}
          </div>
        </section>
      )}
    </div>
  )
}
