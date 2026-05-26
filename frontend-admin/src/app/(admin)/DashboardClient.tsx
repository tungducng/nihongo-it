'use client'

import { useEffect, useState } from 'react'
import {
  Activity,
  BookOpen,
  FolderTree,
  Search,
  Tag,
  TrendingUp,
  UserPlus,
  Users as UsersIcon,
} from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Loader } from '@/components/common/Loader'
import adminService from '@/services/admin.service'
import { useAppToast } from '@/hooks/useAppToast'
import { extractApiError } from '@/types/common.types'
import type { DashboardStats } from '@/types/user.types'

const SUMMARY_CARDS = [
  { key: 'userCount', label: 'Tổng người dùng', Icon: UsersIcon, tone: 'text-[color:var(--ai-500)]' },
  { key: 'vocabularyCount', label: 'Từ vựng', Icon: BookOpen, tone: 'text-[color:var(--jlpt-n5)]' },
  { key: 'categoryCount', label: 'Danh mục', Icon: FolderTree, tone: 'text-[color:var(--jlpt-n3)]' },
  { key: 'topicCount', label: 'Chủ đề', Icon: Tag, tone: 'text-[color:var(--jlpt-n1)]' },
] as const

const TODAY_CARDS = [
  { key: 'newUsers', label: 'Tài khoản mới', Icon: UserPlus, tone: 'text-[color:var(--ai-500)]' },
  { key: 'activeUsers', label: 'Đang hoạt động', Icon: Activity, tone: 'text-[color:var(--jlpt-n5)]' },
  { key: 'searchesToday', label: 'Lượt tra cứu', Icon: Search, tone: 'text-[color:var(--jlpt-n3)]' },
  {
    key: 'flashcardsStudiedToday',
    label: 'Lượt học flashcard',
    Icon: TrendingUp,
    tone: 'text-[color:var(--jlpt-n1)]',
  },
] as const

function StatCard({
  label,
  value,
  Icon,
  tone,
}: {
  label: string
  value: number | string
  Icon: typeof UsersIcon
  tone: string
}) {
  return (
    <Card>
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
  )
}

function SectionHead({ children }: { children: React.ReactNode }) {
  return <p className="eyebrow mb-3">{children}</p>
}

export function DashboardClient() {
  const toast = useAppToast()
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    adminService
      .getDashboardStats()
      .then((data) => {
        if (!cancelled) setStats(data)
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

  if (loading) return <Loader label="Đang tải dashboard..." />

  if (!stats) {
    return <p className="text-muted-foreground py-10 text-center">Chưa có dữ liệu thống kê.</p>
  }

  return (
    <div className="space-y-7">
      <div>
        <h1 className="text-[24px] font-bold tracking-tight text-[color:var(--washi-900)]">
          Dashboard
        </h1>
        <p className="text-muted-foreground mt-1 text-[13px]">Tổng quan hệ thống Nihongo IT</p>
      </div>

      <section>
        <SectionHead>Tổng quan</SectionHead>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {SUMMARY_CARDS.map(({ key, label, Icon, tone }) => (
            <StatCard
              key={key}
              label={label}
              value={stats[key] ?? 0}
              Icon={Icon}
              tone={tone}
            />
          ))}
        </div>
      </section>

      <section>
        <SectionHead>Hôm nay</SectionHead>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {TODAY_CARDS.map(({ key, label, Icon, tone }) => (
            <StatCard
              key={key}
              label={label}
              value={stats[key] ?? 0}
              Icon={Icon}
              tone={tone}
            />
          ))}
        </div>
      </section>

      {stats.recentActivities && stats.recentActivities.length > 0 && (
        <section>
          <SectionHead>Hoạt động gần đây</SectionHead>
          <Card>
            <CardContent className="p-0">
              {stats.recentActivities.slice(0, 10).map((a, i) => (
                <div
                  key={i}
                  className="flex items-center justify-between border-b border-[color:var(--washi-200)] px-4 py-3 text-[13px] last:border-0"
                >
                  <div className="min-w-0 flex-1">
                    <p className="truncate font-medium text-[color:var(--washi-900)]">{a.user}</p>
                    <p className="text-muted-foreground mt-0.5 truncate text-[12px]">{a.action}</p>
                  </div>
                  <span className="text-muted-foreground mono ml-2 shrink-0 text-[12px]">
                    {new Date(a.timestamp).toLocaleString('vi-VN')}
                  </span>
                </div>
              ))}
            </CardContent>
          </Card>
        </section>
      )}
    </div>
  )
}
