import api from '@/lib/api'
import type {
  DashboardStats,
  UserCreateRequest,
  UserDetailInfo,
  UserInfo,
  UserListResponse,
  UserUpdateRequest,
} from '@/types/user.types'

interface RecentActivity {
  user: string
  action: string
  timestamp: string
}

interface UserStatsCount {
  count: number
}

interface LearningDashboardStats {
  vocabularyCount: number
  categoryCount: number
  topicCount: number
  flashcardsCreatedToday: number
  flashcardsStudiedToday: number
  searchesToday: number
}

interface UserProgressDto {
  userId: string
  streakCount: number
  points: number
  dailyGoalMinutes: number
  lastStudyDate?: string | null
}

interface LearningUserStats {
  summary?: {
    totalCards?: number
  }
  cardsByState?: Record<string, number>
}

const startOfTodayIso = () => {
  const d = new Date()
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

const adminService = {
  async getDashboardStats(): Promise<DashboardStats> {
    const since = startOfTodayIso()

    const [learning, userCount, newUsers, activeUsers, recentActivities] = await Promise.all([
      safe(
        api
          .get<LearningDashboardStats>('/api/v1/learning/admin/dashboard/stats')
          .then((r) => r.data),
        {
          vocabularyCount: 0,
          categoryCount: 0,
          topicCount: 0,
          flashcardsCreatedToday: 0,
          flashcardsStudiedToday: 0,
          searchesToday: 0,
        } as LearningDashboardStats,
      ),
      safe(
        api
          .get<UserStatsCount>('/api/v1/user/admin/users/stats/count')
          .then((r) => r.data.count),
        0,
      ),
      safe(
        api
          .get<UserStatsCount>('/api/v1/user/admin/users/stats/new-since', {
            params: { since },
          })
          .then((r) => r.data.count),
        0,
      ),
      safe(
        api
          .get<UserStatsCount>('/api/v1/user/admin/users/stats/active-since', {
            params: { since },
          })
          .then((r) => r.data.count),
        0,
      ),
      safe(
        api
          .get<RecentActivity[]>('/api/v1/user/admin/users/stats/recent-activities', {
            params: { limit: 10 },
          })
          .then((r) => r.data),
        [] as RecentActivity[],
      ),
    ])

    return {
      userCount,
      vocabularyCount: learning.vocabularyCount,
      categoryCount: learning.categoryCount,
      topicCount: learning.topicCount,
      newUsers,
      activeUsers,
      searchesToday: learning.searchesToday,
      flashcardsCreatedToday: learning.flashcardsCreatedToday,
      flashcardsStudiedToday: learning.flashcardsStudiedToday,
      recentActivities,
    }
  },

  async getUsers(
    page = 0,
    size = 10,
    search?: string,
    sortBy: string = 'email',
    sortDir: 'asc' | 'desc' = 'asc',
  ): Promise<UserListResponse> {
    const params: Record<string, string | number> = { page, size, sortBy, sortDir }
    if (search?.trim()) params.search = search.trim()
    const res = await api.get<UserListResponse>('/api/v1/user/admin/users', { params })
    return res.data
  },

  // Merges canonical user identity (user-service) with learning-domain progress
  // + flashcard breakdown (learning-service). Each call is fault-tolerant so a
  // partial failure still surfaces the user record.
  async getUserById(userId: string): Promise<UserDetailInfo> {
    const [base, progress, learningStats] = await Promise.all([
      api.get<UserDetailInfo>(`/api/v1/user/admin/users/${userId}`).then((r) => r.data),
      safe(
        api
          .get<UserProgressDto>(`/api/v1/learning/users/${userId}/progress`)
          .then((r) => r.data),
        null as UserProgressDto | null,
      ),
      safe(
        api
          .get<LearningUserStats>(`/api/v1/learning/admin/statistics/users/${userId}`)
          .then((r) => r.data),
        null as LearningUserStats | null,
      ),
    ])

    const cardsByState = learningStats?.cardsByState ?? {}

    return {
      ...base,
      streakCount: progress?.streakCount,
      points: progress?.points,
      flashcardCount: learningStats?.summary?.totalCards,
      newCards: cardsByState.new,
      learningCards: cardsByState.learning,
      masteredCards: (cardsByState.review ?? 0) + (cardsByState.graduated ?? 0),
    }
  },

  async createUser(data: UserCreateRequest): Promise<UserInfo> {
    const res = await api.post<UserInfo>('/api/v1/user/admin/users', data)
    return res.data
  },

  async updateUser(userId: string, data: UserUpdateRequest): Promise<UserInfo> {
    const res = await api.put<UserInfo>(`/api/v1/user/admin/users/${userId}`, data)
    return res.data
  },

  async deactivateUser(userId: string): Promise<void> {
    await api.delete(`/api/v1/user/admin/users/${userId}`)
  },

  async activateUser(userId: string): Promise<void> {
    await api.put(`/api/v1/user/admin/users/${userId}/activate`)
  },

  async changeUserRole(userId: string, roleId: number): Promise<void> {
    await api.put(`/api/v1/user/admin/users/${userId}/change-role`, null, {
      params: { roleId },
    })
  },
}

export default adminService
