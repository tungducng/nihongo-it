import api from '@/lib/api'
import type {
  DashboardStats,
  UserCreateRequest,
  UserDetailInfo,
  UserInfo,
  UserListResponse,
  UserUpdateRequest,
} from '@/types/user.types'

const adminService = {
  async getDashboardStats(): Promise<DashboardStats> {
    const res = await api.get<DashboardStats>('/api/v1/learning/admin/dashboard/stats')
    return res.data
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

  // Backend currently returns plain UserDto. Extra fields on UserDetailInfo
  // (streakCount/points/flashcardCount) are undefined until the BE endpoint
  // is extended with an AdminUserDetailDto.
  async getUserById(userId: string): Promise<UserDetailInfo> {
    const res = await api.get<UserDetailInfo>(`/api/v1/user/admin/users/${userId}`)
    return res.data
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
