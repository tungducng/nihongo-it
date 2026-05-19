import type { JlptLevel, DateString, UUID } from './common.types'

export interface ConversationLine {
  lineId?: UUID
  speaker: string
  japaneseText: string
  vietnameseTranslation?: string
  notes?: string
  importantVocab?: string
  orderIndex: number
  // Client-only: stable React key for new (unsaved) lines.
  tempId?: string
}

export interface Conversation {
  conversationId?: UUID
  title: string
  description?: string
  jlptLevel?: JlptLevel
  unit?: number
  lines?: ConversationLine[]
  createdAt?: DateString
  updatedAt?: DateString
}

export interface CreateConversationLineRequest {
  speaker: string
  japaneseText: string
  vietnameseTranslation?: string
  notes?: string
  importantVocab?: string
  orderIndex: number
}

export interface CreateConversationRequest {
  title: string
  description?: string
  jlptLevel?: JlptLevel
  unit?: number
  lines: CreateConversationLineRequest[]
}

export interface UpdateConversationLineRequest extends CreateConversationLineRequest {
  lineId?: UUID
}

export interface UpdateConversationRequest {
  title?: string
  description?: string
  jlptLevel?: JlptLevel
  unit?: number
  lines?: UpdateConversationLineRequest[]
}
