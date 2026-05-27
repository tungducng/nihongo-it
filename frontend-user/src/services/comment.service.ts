import api from '@/lib/api'
import type { Comment, CommentPage, CommentSort } from '@/types/comment.types'

const commentService = {
  listForVocab(
    vocabId: string,
    sort: CommentSort,
    page: number,
    size = 10,
  ): Promise<CommentPage> {
    return api
      .get<CommentPage>(`/api/v1/learning/vocabulary/${vocabId}/comments`, {
        params: { sort, page, size },
      })
      .then((r) => r.data)
  },

  listReplies(commentId: string, page: number, size = 5): Promise<CommentPage> {
    return api
      .get<CommentPage>(`/api/v1/learning/comments/${commentId}/replies`, {
        params: { page, size },
      })
      .then((r) => r.data)
  },

  create(vocabId: string, content: string, parentCommentId: string | null = null): Promise<Comment> {
    return api
      .post<Comment>(`/api/v1/learning/vocabulary/${vocabId}/comments`, {
        content,
        parentCommentId,
      })
      .then((r) => r.data)
  },

  update(commentId: string, content: string): Promise<Comment> {
    return api
      .put<Comment>(`/api/v1/learning/comments/${commentId}`, { content })
      .then((r) => r.data)
  },

  delete(commentId: string): Promise<void> {
    return api.delete(`/api/v1/learning/comments/${commentId}`).then(() => undefined)
  },

  like(commentId: string): Promise<{ likeCount: number; liked: boolean }> {
    return api
      .post<{ likeCount: number; liked: boolean }>(`/api/v1/learning/comments/${commentId}/like`)
      .then((r) => r.data)
  },

  unlike(commentId: string): Promise<{ likeCount: number; liked: boolean }> {
    return api
      .delete<{ likeCount: number; liked: boolean }>(`/api/v1/learning/comments/${commentId}/like`)
      .then((r) => r.data)
  },
}

export default commentService
