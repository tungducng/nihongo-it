import api from '@/lib/api'
import type { PagedResponse } from '@/types/common.types'
import type {
  Conversation,
  CreateConversationRequest,
  UpdateConversationRequest,
} from '@/types/conversation.types'

interface ListParams {
  page?: number
  size?: number
  search?: string
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

const conversationService = {
  list: (params: ListParams = {}): Promise<PagedResponse<Conversation>> => {
    const { page = 0, size = 10, search, sortBy = 'title', sortDir = 'asc' } = params
    const qp: Record<string, string | number> = { page, size, sortBy, sortDir }
    if (search?.trim()) qp.search = search.trim()
    return api.get('/api/v1/learning/admin/conversations', { params: qp }).then((r) => r.data)
  },

  getByJlptLevel: (
    level: string,
    page = 0,
    size = 10,
  ): Promise<PagedResponse<Conversation>> =>
    api
      .get(`/api/v1/learning/admin/conversations/jlpt/${level}`, { params: { page, size } })
      .then((r) => r.data),

  getById: (id: string): Promise<Conversation> =>
    api.get(`/api/v1/learning/admin/conversations/${id}`).then((r) => r.data),

  create: (data: CreateConversationRequest): Promise<Conversation> =>
    api.post('/api/v1/learning/admin/conversations', data).then((r) => r.data),

  update: (id: string, data: UpdateConversationRequest): Promise<Conversation> =>
    api.put(`/api/v1/learning/admin/conversations/${id}`, data).then((r) => r.data),

  delete: (id: string): Promise<void> =>
    api.delete(`/api/v1/learning/admin/conversations/${id}`).then(() => undefined),
}

export default conversationService
