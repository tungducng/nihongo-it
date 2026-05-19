import api from '@/lib/api'
import type { PagedResponse } from '@/types/common.types'
import type { NotificationItem } from '@/types/notification.types'

const notificationService = {
  async list(page = 0, size = 20): Promise<PagedResponse<NotificationItem>> {
    const res = await api.get<PagedResponse<NotificationItem>>('/api/v1/notify/notifications', {
      params: { page, size },
    })
    return res.data
  },

  async getUnreadCount(): Promise<number> {
    const res = await api.get<{ count: number }>('/api/v1/notify/notifications/unread-count')
    return res.data.count ?? 0
  },

  async markAsRead(id: string): Promise<void> {
    await api.put(`/api/v1/notify/notifications/${id}/read`)
  },

  async markAllAsRead(): Promise<number> {
    const res = await api.put<{ updated: number }>('/api/v1/notify/notifications/read-all')
    return res.data.updated ?? 0
  },

  async deleteNotification(id: string): Promise<void> {
    await api.delete(`/api/v1/notify/notifications/${id}`)
  },
}

export default notificationService
