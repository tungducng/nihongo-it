import api from '@/lib/api'
import type {
  AdminStatisticsOverview,
  UserStatisticsDetail,
  UserStatisticsListResponse,
} from '@/types/statistics.types'

interface UsersListParams {
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
  search?: string
}

const statisticsService = {
  async getOverview(): Promise<AdminStatisticsOverview | null> {
    try {
      const res = await api.get<AdminStatisticsOverview>(
        '/api/v1/learning/admin/statistics/overview',
      )
      return res.data
    } catch {
      return null
    }
  },

  async getUsers(params: UsersListParams = {}): Promise<UserStatisticsListResponse> {
    const { page = 0, size = 10, sortBy = 'email', sortDir = 'asc', search } = params
    const qp: Record<string, string | number> = { page, size, sortBy, sortDir }
    if (search?.trim()) qp.search = search.trim()
    const res = await api.get<UserStatisticsListResponse>(
      '/api/v1/learning/admin/statistics/users',
      { params: qp },
    )
    return res.data
  },

  async getUserDetail(userId: string): Promise<UserStatisticsDetail | null> {
    try {
      const res = await api.get<UserStatisticsDetail>(
        `/api/v1/learning/admin/statistics/users/${userId}`,
      )
      return res.data
    } catch {
      return null
    }
  },
}

export default statisticsService
