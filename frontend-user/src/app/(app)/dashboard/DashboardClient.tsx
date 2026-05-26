'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { ArrowRight, Flame, MessageSquare } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Loader } from '@/components/common/Loader'
import flashcardService from '@/services/flashcard.service'
import vocabularyService from '@/services/vocabulary.service'
import conversationService from '@/services/conversation.service'
import userProgressService, { type UserProgress } from '@/services/user-progress.service'
import { useAuthStore } from '@/stores/auth.store'
import { useAppToast } from '@/hooks/useAppToast'
import { extractApiError } from '@/types/common.types'
import type { StudyStatistics, VocabularyItem } from '@/types/learning.types'
import type { Conversation } from '@/types/conversation.types'

const VI_WEEKDAYS = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'] as const

function formatDate(d: Date) {
  // "Thứ Ba · 26 tháng 5"
  const weekday = ['Chủ nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'][d.getDay()]
  return `${weekday} · ${d.getDate()} tháng ${d.getMonth() + 1}`
}

function StreakStrip({ progress }: { progress: UserProgress | null }) {
  // Build last 7 days [Mon..Sun] with status: studied / missed / today
  const today = new Date()
  const todayIdx = (today.getDay() + 6) % 7 // 0=Mon..6=Sun
  const lastStudy = progress?.lastStudyDate ? new Date(progress.lastStudyDate) : null
  const lastStudyDay = lastStudy ? new Date(lastStudy.getFullYear(), lastStudy.getMonth(), lastStudy.getDate()) : null
  const todayDay = new Date(today.getFullYear(), today.getMonth(), today.getDate())

  const cells = Array.from({ length: 7 }, (_, i) => {
    // i = 0..6 (Mon..Sun); compute date that distance from today
    const d = new Date(todayDay)
    d.setDate(d.getDate() + (i - todayIdx))
    const isToday = i === todayIdx
    const isFuture = d > todayDay
    const wasStudied = lastStudyDay !== null && d <= lastStudyDay && d > new Date(lastStudyDay.getFullYear(), lastStudyDay.getMonth(), lastStudyDay.getDate() - (progress?.streakCount ?? 0))
    const label = VI_WEEKDAYS[(i + 1) % 7] // map Mon..Sun → T2..CN
    if (isToday) {
      return (
        <div key={i} className="flex flex-col items-center gap-1">
          <span className="text-[10px] font-medium uppercase text-[color:var(--ai-600)]">{label}</span>
          <div className="relative grid h-9 w-full place-items-center rounded-lg border-2 border-dashed border-[color:var(--ai-400)] bg-white text-xs font-bold text-[color:var(--ai-500)]">
            ?
          </div>
        </div>
      )
    }
    if (isFuture) {
      return (
        <div key={i} className="flex flex-col items-center gap-1">
          <span className="text-muted-foreground text-[10px] font-medium uppercase">{label}</span>
          <div className="grid h-9 w-full place-items-center rounded-lg bg-[color:var(--washi-100)]" />
        </div>
      )
    }
    return (
      <div key={i} className="flex flex-col items-center gap-1">
        <span className="text-muted-foreground text-[10px] font-medium uppercase">{label}</span>
        <div
          className={`grid h-9 w-full place-items-center rounded-lg text-xs font-bold ${
            wasStudied
              ? 'bg-[color:var(--ai-500)] text-white'
              : 'bg-[color:var(--washi-200)] text-[color:var(--washi-500)]'
          }`}
        >
          {wasStudied ? '✓' : '·'}
        </div>
      </div>
    )
  })

  return <div className="grid grid-cols-7 gap-1.5">{cells}</div>
}

function Heatmap30({ stats }: { stats: StudyStatistics | null }) {
  const today = new Date()
  const todayDay = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  // Last 30 days (oldest first)
  const cells = Array.from({ length: 30 }, (_, i) => {
    const d = new Date(todayDay)
    d.setDate(d.getDate() - (29 - i))
    const key = d.toISOString().slice(0, 10)
    const count = stats?.dailyReviews?.[key] ?? 0
    let intensity: 0 | 1 | 2 | 3 | 4 = 0
    if (count > 0 && count <= 5) intensity = 1
    else if (count <= 15) intensity = 2
    else if (count <= 30) intensity = 3
    else if (count > 30) intensity = 4
    const isToday = i === 29
    const cls = [
      'bg-[color:var(--washi-200)]',
      'bg-[color:oklch(from_var(--ai-500)_l_c_h_/_25%)]',
      'bg-[color:oklch(from_var(--ai-500)_l_c_h_/_45%)]',
      'bg-[color:var(--ai-500)]',
      'bg-[color:var(--ai-700)]',
    ][intensity]
    return (
      <div
        key={i}
        title={`${key}: ${count} thẻ`}
        className={`aspect-square rounded-sm ${cls} ${isToday ? 'ring-2 ring-[color:var(--ai-500)] ring-offset-1' : ''}`}
      />
    )
  })
  return (
    <div className="grid grid-cols-[repeat(30,1fr)] gap-1.5">{cells}</div>
  )
}

interface DashboardData {
  dueCount: number
  totalCards: number
  todayMinutes: number
  goalMinutes: number
  progress: UserProgress | null
  suggested: VocabularyItem[]
  conversation: Conversation | null
  stats: StudyStatistics | null
}

export function DashboardClient() {
  const toast = useAppToast()
  const user = useAuthStore((s) => s.user)
  const [loading, setLoading] = useState(true)
  const [data, setData] = useState<DashboardData | null>(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      try {
        // Fetch everything in parallel — each call has its own catch so a single
        // failing endpoint doesn't blank the entire dashboard.
        const [stats, progress, vocab, convPage] = await Promise.all([
          flashcardService.getStudyStatistics().catch(() => null),
          userProgressService.getMyProgress().catch(() => null),
          vocabularyService.getVocabulary({ page: 0, size: 5, keyword: null, jlptLevel: null, topicName: null }).catch(() => null),
          conversationService.getConversations(0, 1, '').catch(() => null),
        ])

        if (cancelled) return

        const todayMinutes = 0 // backend doesn't expose this yet; placeholder
        setData({
          dueCount: stats?.summary?.dueCardsNow ?? 0,
          totalCards: stats?.summary?.totalCards ?? 0,
          todayMinutes,
          goalMinutes: progress?.dailyGoalMinutes ?? 15,
          progress,
          suggested: vocab?.content ?? [],
          conversation: convPage?.content?.[0] ?? null,
          stats,
        })
      } catch (err) {
        toast.error(extractApiError(err, 'Không tải được dữ liệu hôm nay'))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    void load()
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  if (loading) return <Loader label="Đang tải hôm nay..." />
  if (!data) return null

  const firstName = user?.fullName?.split(' ').slice(-1)[0] ?? user?.fullName ?? 'bạn'
  const goalProgress = data.goalMinutes > 0 ? Math.min(100, (data.todayMinutes / data.goalMinutes) * 100) : 0
  const streakCount = data.progress?.streakCount ?? 0

  return (
    <div className="space-y-6">
      {/* Greeting */}
      <div className="flex items-end justify-between">
        <div>
          <p className="eyebrow mb-1">{formatDate(new Date())}</p>
          <h1 className="text-[28px] font-bold tracking-tight text-[color:var(--washi-900)]">
            Chào {firstName}, hôm nay học gì?
          </h1>
        </div>
        <div className="hidden text-right md:block">
          <p className="eyebrow">Mục tiêu hôm nay</p>
          <p className="mono mt-1 text-[22px] font-bold text-[color:var(--washi-900)]">
            <span className="text-[color:var(--ai-500)]">{data.todayMinutes}</span>
            <span className="text-[color:var(--washi-400)]">/{data.goalMinutes}</span>
            <span className="text-muted-foreground ml-1 text-[13px] font-medium">phút</span>
          </p>
        </div>
      </div>

      {/* Daily goal progress */}
      <div className="h-1.5 overflow-hidden rounded-full bg-[color:var(--washi-200)]">
        <div
          className="h-full rounded-full bg-[color:var(--ai-500)] transition-all"
          style={{ width: `${goalProgress}%` }}
        />
      </div>

      {/* Hero row */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1.5fr_1fr]">
        {/* Hero — Hôm nay */}
        <Card className="overflow-hidden">
          <CardContent className="p-7">
            <div className="mb-3 flex items-center gap-2">
              <Badge className="bg-[color:var(--ai-100)] text-[color:var(--ai-700)]">Hôm nay</Badge>
              {data.dueCount > 0 && (
                <Badge variant="outline">{data.dueCount} thẻ đang chờ</Badge>
              )}
            </div>
            {data.dueCount > 0 ? (
              <>
                <h2 className="text-[32px] font-bold tracking-tight text-[color:var(--washi-900)]">
                  <span className="mono">{data.dueCount}</span> thẻ +{' '}
                  <span className="mono">{data.suggested.length}</span>{' '}
                  <span className="text-[color:var(--washi-400)]">từ mới</span>
                </h2>
                <p className="text-muted-foreground mt-2 max-w-md text-[14px]">
                  Học hôm nay để tiếp tục chuỗi {streakCount + 1} ngày liên tiếp.
                </p>
              </>
            ) : (
              <>
                <h2 className="text-[24px] font-bold tracking-tight text-[color:var(--washi-900)]">
                  Hôm nay không có thẻ đến hạn.
                </h2>
                <p className="text-muted-foreground mt-2 max-w-md text-[14px]">
                  Khám phá từ vựng mới hoặc luyện một bài hội thoại để duy trì chuỗi học.
                </p>
              </>
            )}
            <div className="mt-5 flex flex-wrap items-center gap-2">
              <Button asChild>
                <Link href={data.dueCount > 0 ? '/flashcards/study' : '/vocabulary'}>
                  {data.dueCount > 0 ? 'Bắt đầu học' : 'Khám phá từ vựng'}
                  <ArrowRight className="ml-1 size-4" />
                </Link>
              </Button>
              {data.dueCount > 0 && (
                <Button asChild variant="outline">
                  <Link href="/flashcards/stats">Xem thống kê</Link>
                </Button>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Streak */}
        <Card>
          <CardContent className="p-6">
            <div className="mb-4 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Flame className="size-5 text-[color:var(--shu-500)]" />
                <h3 className="text-[15px] font-semibold text-[color:var(--washi-900)]">Streak</h3>
              </div>
              <Badge className="bg-[color:var(--shu-50)] text-[color:var(--shu-700)]">
                <span className="mono">{streakCount}</span> ngày
              </Badge>
            </div>
            <StreakStrip progress={data.progress} />
            <div className="mt-4 border-t border-[color:var(--washi-200)] pt-3">
              <p className="text-muted-foreground text-[12px]">
                {streakCount > 0
                  ? `Học hôm nay để giữ chuỗi ${streakCount} ngày.`
                  : 'Học hôm nay để bắt đầu chuỗi mới.'}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Suggested vocab + Conversation */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        {/* Suggested vocab */}
        <Card>
          <CardContent className="p-6">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-[15px] font-semibold text-[color:var(--washi-900)]">
                Từ mới gợi ý
              </h3>
              <Link
                href="/vocabulary"
                className="text-[12px] font-semibold text-[color:var(--ai-500)] hover:underline"
              >
                Xem tất cả →
              </Link>
            </div>
            {data.suggested.length > 0 ? (
              <ul className="space-y-2.5">
                {data.suggested.map((v) => (
                  <li key={v.vocabId}>
                    <Link
                      href={`/vocabulary/${v.vocabId}`}
                      className="hover:border-[color:var(--ai-300)] hover:bg-[color:var(--ai-50)] flex items-center justify-between rounded-lg border border-transparent p-2.5 transition-colors"
                    >
                      <div className="min-w-0">
                        <p className="font-jp truncate text-[16px] font-semibold text-[color:var(--washi-900)]">
                          {v.term}
                          {v.pronunciation && (
                            <span className="font-jp text-muted-foreground ml-2 text-[12px] font-normal">
                              {v.pronunciation}
                            </span>
                          )}
                        </p>
                        <p className="text-muted-foreground truncate text-[12px]">{v.meaning}</p>
                      </div>
                      <span
                        className={`jlpt-${v.jlptLevel.toLowerCase()} inline-flex h-[20px] shrink-0 items-center rounded-full px-2 text-[10px] font-semibold`}
                      >
                        {v.jlptLevel}
                      </span>
                    </Link>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-muted-foreground py-4 text-center text-[13px]">
                Chưa có từ vựng nào.
              </p>
            )}
          </CardContent>
        </Card>

        {/* Conversation of day */}
        <Card>
          <CardContent className="p-6">
            <div className="mb-3 flex items-center gap-2">
              <MessageSquare className="size-4 text-[color:var(--ai-500)]" />
              <h3 className="text-[15px] font-semibold text-[color:var(--washi-900)]">
                Hội thoại hôm nay
              </h3>
            </div>
            {data.conversation ? (
              <>
                <p className="font-jp mb-1 text-[18px] font-semibold text-[color:var(--washi-900)]">
                  {data.conversation.title}
                </p>
                {data.conversation.description && (
                  <p className="text-muted-foreground line-clamp-2 text-[13px]">
                    {data.conversation.description}
                  </p>
                )}
                <div className="mt-3 flex items-center gap-2">
                  {data.conversation.jlptLevel && (
                    <span
                      className={`jlpt-${data.conversation.jlptLevel.toLowerCase()} inline-flex h-[22px] items-center rounded-full px-2.5 text-[11px] font-semibold`}
                    >
                      {data.conversation.jlptLevel}
                    </span>
                  )}
                  {typeof data.conversation.unit === 'number' && (
                    <Badge variant="outline" className="h-[22px] px-2.5 text-[11px] font-semibold">
                      Bài {data.conversation.unit}
                    </Badge>
                  )}
                </div>
                <Button asChild className="mt-5 w-full">
                  <Link href={`/conversation/${data.conversation.conversationId}/practice`}>
                    Tham gia hội thoại
                    <ArrowRight className="ml-1 size-4" />
                  </Link>
                </Button>
              </>
            ) : (
              <p className="text-muted-foreground py-4 text-center text-[13px]">
                Chưa có hội thoại nào.
              </p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Heatmap */}
      <Card>
        <CardContent className="p-6">
          <div className="mb-4 flex items-center justify-between">
            <div>
              <h3 className="text-[15px] font-semibold text-[color:var(--washi-900)]">
                Hoạt động 30 ngày qua
              </h3>
              <p className="text-muted-foreground mt-0.5 text-[12px]">
                Mỗi ô là một ngày — màu càng đậm, ôn càng nhiều thẻ.
              </p>
            </div>
            <div className="text-muted-foreground hidden items-center gap-1.5 text-[10px] md:flex">
              <span>Ít</span>
              <span className="size-3 rounded-sm bg-[color:var(--washi-200)]" />
              <span className="size-3 rounded-sm bg-[color:oklch(from_var(--ai-500)_l_c_h_/_25%)]" />
              <span className="size-3 rounded-sm bg-[color:oklch(from_var(--ai-500)_l_c_h_/_45%)]" />
              <span className="size-3 rounded-sm bg-[color:var(--ai-500)]" />
              <span className="size-3 rounded-sm bg-[color:var(--ai-700)]" />
              <span>Nhiều</span>
            </div>
          </div>
          <Heatmap30 stats={data.stats} />
        </CardContent>
      </Card>
    </div>
  )
}
