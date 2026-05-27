export interface CommentUser {
  userId: string
  fullName: string
  initials: string
}

export interface Comment {
  commentId: string
  vocabId: string
  parentCommentId: string | null
  user: CommentUser
  content: string
  likeCount: number
  replyCount: number
  liked: boolean
  edited: boolean
  deleted: boolean
  canEdit: boolean
  createdAt: string
  updatedAt: string
}

export interface CommentPage {
  content: Comment[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  totalComments?: number
}

export type CommentSort = 'newest' | 'top'
