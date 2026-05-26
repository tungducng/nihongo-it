'use client'

import '@/lib/charts' // side-effect: register chart components
import { useEffect, useState } from 'react'
import Link from 'next/link'
import { Bar, Doughnut, Line } from 'react-chartjs-2'
import { ArrowLeft, Brain, CalendarClock, Flame, Layers } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Loader } from '@/components/common/Loader'
import flashcardService from '@/services/flashcard.service'
import { useAppToast } from '@/hooks/useAppToast'
import { extractApiError } from '@/types/common.types'

// Backend stats shape — narrowed locally because the service returns `unknown`.
interface StatsShape {
  summary?: {
    totalCards?: number
    dueCardsNow?: number
    currentStreak?: number
    overallRetentionRate?: number
  }
  dailyReviews?: Record<string, number>
  retentionRateByDay?: Record<string, number>
  cardsDueByDay?: Record<string, number>
  memoryStrengthDistribution?: {
    weak?: number
    medium?: number
    strong?: number
    new?: number
  }
  cardsByState?: Record<string, number>
}

const fmtPercent = (v?: number) => {
  if (typeof v !== 'number' || Number.isNaN(v)) return '—'
  return `${Math.round(v * 100)}%`
}

const sortedEntries = (obj?: Record<string, number>) =>
  obj ? Object.entries(obj).sort(([a], [b]) => a.localeCompare(b)) : []

export function StatsView() {
  const toast = useAppToast()
  const [stats, setStats] = useState<StatsShape | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    flashcardService
      .getStudyStatistics()
      .then((data) => {
        if (!cancelled) setStats(data as StatsShape)
      })
      .catch((err) => {
        if (!cancelled) toast.error(extractApiError(err, 'Không tải được thống kê'))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  if (loading) return <Loader label="Đang tải thống kê..." />
  if (!stats) {
    return (
      <div className="space-y-4 py-10 text-center">
        <p className="text-muted-foreground">Chưa có dữ liệu thống kê.</p>
        <Button asChild variant="outline">
          <Link href="/flashcards/study">Bắt đầu học</Link>
        </Button>
      </div>
    )
  }

  const summary = stats.summary ?? {}
  const streak = summary.currentStreak ?? 0
  const summaryCards = [
    {
      label: 'Tổng thẻ',
      value: summary.totalCards ?? 0,
      Icon: Layers,
      tone: 'text-[color:var(--ai-500)]',
    },
    {
      label: 'Đến hạn',
      value: summary.dueCardsNow ?? 0,
      Icon: CalendarClock,
      tone: 'text-[color:var(--jlpt-n3)]',
    },
    {
      label: 'Chuỗi ngày',
      value: streak,
      Icon: Flame,
      tone: 'text-[color:var(--shu-500)]',
    },
    {
      label: 'Ghi nhớ',
      value: fmtPercent(summary.overallRetentionRate),
      Icon: Brain,
      tone: 'text-[color:var(--jlpt-n5)]',
    },
  ]

  // Chart palette — pulls from --chart-1..5 (JLPT chromas) so colour meaning
  // is consistent with badges and rating buttons.
  const colorPrimary = 'oklch(0.48 0.135 260)' // --ai-500
  const colorPrimaryFill = 'oklch(0.48 0.135 260 / 0.18)'
  const colorWarn = 'oklch(0.74 0.16 80 / 0.7)' // --jlpt-n3
  const colorN1 = 'oklch(0.58 0.21 15 / 0.7)'
  const colorN3 = 'oklch(0.74 0.16 80 / 0.7)'
  const colorN5 = 'oklch(0.65 0.16 152 / 0.7)'
  const colorN4 = 'oklch(0.66 0.14 220 / 0.7)'

  // ---- Daily reviews (line chart) ----
  const dailyReviews = sortedEntries(stats.dailyReviews)
  const dailyData = {
    labels: dailyReviews.map(([d]) => d),
    datasets: [
      {
        label: 'Lượt ôn',
        data: dailyReviews.map(([, v]) => v),
        borderColor: colorPrimary,
        backgroundColor: colorPrimaryFill,
        fill: true,
        tension: 0.3,
      },
    ],
  }

  // ---- Cards due forecast (bar chart) ----
  const cardsDue = sortedEntries(stats.cardsDueByDay)
  const dueData = {
    labels: cardsDue.map(([d]) => d),
    datasets: [
      {
        label: 'Thẻ đến hạn',
        data: cardsDue.map(([, v]) => v),
        backgroundColor: colorWarn,
      },
    ],
  }

  // ---- Memory strength (doughnut) ----
  const mem = stats.memoryStrengthDistribution ?? {}
  const memData = {
    labels: ['Yếu', 'Trung bình', 'Mạnh', 'Mới'],
    datasets: [
      {
        data: [mem.weak ?? 0, mem.medium ?? 0, mem.strong ?? 0, mem.new ?? 0],
        backgroundColor: [colorN1, colorN3, colorN5, colorN4],
      },
    ],
  }

  const chartOpts = { maintainAspectRatio: false, responsive: true }

  return (
    <div className="space-y-6">
      <Button asChild variant="ghost" size="sm" className="-ml-2">
        <Link href="/flashcards/study">
          <ArrowLeft className="mr-1 size-4" />
          Quay lại học
        </Link>
      </Button>

      <h1 className="text-[24px] font-bold tracking-tight text-[color:var(--washi-900)]">
        Thống kê học tập
      </h1>

      {streak > 0 && (
        <div className="flex items-center gap-3 rounded-[12px] border border-[color:oklch(from_var(--shu-500)_l_c_h_/_30%)] bg-[color:var(--shu-50)] px-[18px] py-[14px]">
          <div className="bg-accent text-accent-foreground inline-flex h-10 w-10 items-center justify-center rounded-[10px]">
            <Flame className="size-5" />
          </div>
          <div>
            <div className="text-[16px] font-semibold text-[color:var(--shu-800)]">
              {streak} ngày học liên tiếp
            </div>
            <div className="text-[12px] text-[color:var(--shu-700)]">
              Tiếp tục thêm 1 thẻ hôm nay để giữ chuỗi.
            </div>
          </div>
        </div>
      )}

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {summaryCards.map(({ label, value, Icon, tone }) => (
          <Card key={label}>
            <CardContent className="flex items-start justify-between p-4">
              <div>
                <p className="eyebrow">{label}</p>
                <p className="mono mt-1.5 text-[26px] font-semibold tracking-tight text-[color:var(--washi-900)]">
                  {value}
                </p>
              </div>
              <Icon className={`size-5 ${tone}`} />
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Hoạt động ôn tập</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-64">
              {dailyReviews.length > 0 ? (
                <Line data={dailyData} options={chartOpts} />
              ) : (
                <p className="text-muted-foreground py-10 text-center text-sm">Chưa có dữ liệu</p>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Dự báo thẻ đến hạn</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-64">
              {cardsDue.length > 0 ? (
                <Bar data={dueData} options={chartOpts} />
              ) : (
                <p className="text-muted-foreground py-10 text-center text-sm">Chưa có dữ liệu</p>
              )}
            </div>
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-base">Phân bố độ ghi nhớ</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="mx-auto h-72 max-w-md">
              <Doughnut data={memData} options={chartOpts} />
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
