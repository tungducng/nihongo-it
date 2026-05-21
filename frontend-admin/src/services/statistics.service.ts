import api from '@/lib/api'
import type {
  AdminStatisticsOverview,
  UserStatistics,
  UserStatisticsDetail,
  UserStatisticsListResponse,
} from '@/types/statistics.types'
import type { UserListResponse } from '@/types/user.types'

interface UsersListParams {
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
  search?: string
}

interface UserStatsCount {
  count: number
}

interface LearningOverview {
  totalFlashcards: number
  averageRetentionRate: number
}

interface LearningUserStats {
  summary?: UserStatistics['summary']
  cardsByState?: Record<string, number>
  cardsByJlptLevel?: Record<string, number>
  dailyReviews?: Record<string, number>
  retentionRateByDay?: Record<string, number>
  memoryStrengthDistribution?: { weak?: number; medium?: number; strong?: number }
  cardsDueByDay?: Record<string, number>
  reviewHistory?: UserStatisticsDetail['reviewHistory']
  lastActive?: string
  progress?: number
  streakCount?: number
  points?: number
}

const isoDaysAgo = (days: number) => {
  const d = new Date()
  d.setDate(d.getDate() - days)
  d.setHours(0, 0, 0, 0)
  return d.toISOString().replace('Z', '')
}

const safe = async <T>(p: Promise<T>, fallback: T): Promise<T> => {
  try {
    return await p
  } catch {
    return fallback
  }
}

const statisticsService = {
  async getOverview(): Promise<AdminStatisticsOverview | null> {
    const since30 = isoDaysAgo(30)

    const [learning, totalUsers, activeUsers, usersByLevel, usersByJlptGoal] = await Promise.all([
      safe(
        api
          .get<LearningOverview>('/api/v1/learning/admin/statistics/overview')
          .then((r) => r.data),
        null as LearningOverview | null,
      ),
      safe(
        api
          .get<UserStatsCount>('/api/v1/user/admin/users/stats/count')
          .then((r) => r.data.count),
        0,
      ),
      safe(
        api
          .get<UserStatsCount>('/api/v1/user/admin/users/stats/active-since', {
            params: { since: since30 },
          })
          .then((r) => r.data.count),
        0,
      ),
      safe(
        api
          .get<Record<string, number>>('/api/v1/user/admin/users/stats/by-level')
          .then((r) => r.data),
        {} as Record<string, number>,
      ),
      safe(
        api
          .get<Record<string, number>>('/api/v1/user/admin/users/stats/by-jlpt-goal')
          .then((r) => r.data),
        {} as Record<string, number>,
      ),
    ])

    if (!learning && totalUsers === 0) return null

    const totalFlashcards = learning?.totalFlashcards ?? 0
    return {
      totalUsers,
      activeUsers,
      totalFlashcards,
      averageCardsPerUser: totalUsers > 0 ? totalFlashcards / totalUsers : 0,
      averageRetentionRate: learning?.averageRetentionRate ?? 0,
      usersByLevel,
      usersByJlptGoal,
      // Backend does not rank users; the dashboard hides these sections when empty.
      topPerformingUsers: [],
      mostActiveUsers: [],
    }
  },

  // Combines user list (user-service) with per-user learning stats (learning-service)
  // via N+1 fan-out. Acceptable for paginated views (size <= 100).
  async getUsers(params: UsersListParams = {}): Promise<UserStatisticsListResponse> {
    const { page = 0, size = 10, sortBy = 'email', sortDir = 'asc', search } = params
    const qp: Record<string, string | number> = { page, size, sortBy, sortDir }
    if (search?.trim()) qp.search = search.trim()

    const userList = await api
      .get<UserListResponse>('/api/v1/user/admin/users', { params: qp })
      .then((r) => r.data)

    const enriched = await Promise.all(
      userList.users.map(async (u): Promise<UserStatistics> => {
        const stats = await safe(
          api
            .get<LearningUserStats>(`/api/v1/learning/admin/statistics/users/${u.userId}`)
            .then((r) => r.data),
          null as LearningUserStats | null,
        )
        return {
          userId: u.userId,
          userName: u.fullName,
          email: u.email,
          summary: stats?.summary,
          cardsByState: stats?.cardsByState,
          currentStreak: stats?.streakCount,
          retentionRate: stats?.summary?.overallRetentionRate,
        }
      }),
    )

    return {
      users: enriched,
      totalItems: userList.totalItems,
      totalPages: userList.totalPages,
      currentPage: userList.currentPage,
    }
  },

  async getUserDetail(userId: string): Promise<UserStatisticsDetail | null> {
    const [base, stats] = await Promise.all([
      safe(
        api
          .get<{
            userId: string
            email: string
            fullName: string
            currentLevel?: string
            jlptGoal?: string
            createdAt?: string
            lastLogin?: string
          }>(`/api/v1/user/admin/users/${userId}`)
          .then((r) => r.data),
        null as {
          userId: string
          email: string
          fullName: string
          currentLevel?: string
          jlptGoal?: string
          createdAt?: string
          lastLogin?: string
        } | null,
      ),
      safe(
        api
          .get<LearningUserStats>(`/api/v1/learning/admin/statistics/users/${userId}`)
          .then((r) => r.data),
        null as LearningUserStats | null,
      ),
    ])

    if (!base && !stats) return null

    return {
      userId: base?.userId ?? userId,
      userName: base?.fullName ?? '',
      email: base?.email ?? '',
      summary: stats?.summary,
      cardsByState: stats?.cardsByState,
      cardsByJlptLevel: stats?.cardsByJlptLevel,
      dailyReviews: stats?.dailyReviews,
      retentionRateByDay: stats?.retentionRateByDay,
      memoryStrengthDistribution: stats?.memoryStrengthDistribution,
      cardsDueByDay: stats?.cardsDueByDay,
      reviewHistory: stats?.reviewHistory,
      lastActive: stats?.lastActive,
      progress: stats?.progress,
      currentStreak: stats?.streakCount,
      retentionRate: stats?.summary?.overallRetentionRate,
      profileInfo: base
        ? {
            currentLevel: base.currentLevel,
            jlptGoal: base.jlptGoal,
            createdAt: base.createdAt,
            lastLogin: base.lastLogin,
          }
        : undefined,
    }
  },
}

export default statisticsService
