import type { JlptLevel, DateString, UUID } from './common.types'

export interface Category {
  categoryId: UUID
  name: string
  meaning?: string
  displayOrder: number
  topicCount?: number
  isActive: boolean
  createdAt?: DateString
  updatedAt?: DateString
}

export interface CreateCategoryRequest {
  name: string
  meaning: string
  displayOrder?: number
  isActive?: boolean
}
export interface UpdateCategoryRequest {
  name?: string
  meaning?: string
  displayOrder?: number
  isActive?: boolean
}

export interface Topic {
  topicId: UUID
  name: string
  meaning?: string
  jlptLevel?: JlptLevel
  displayOrder: number
  categoryId: UUID
  categoryName?: string
  vocabularyCount?: number
  isActive: boolean
  createdAt?: DateString
  updatedAt?: DateString
}

export interface CreateTopicRequest {
  name: string
  meaning: string
  displayOrder?: number
  isActive?: boolean
  categoryId: UUID
}
export interface UpdateTopicRequest {
  name?: string
  meaning?: string
  displayOrder?: number
  isActive?: boolean
  categoryId?: UUID
}

export interface VocabularyItem {
  vocabId: UUID
  term: string
  meaning: string
  pronunciation?: string
  example?: string
  exampleMeaning?: string
  audioPath?: string
  jlptLevel: JlptLevel
  topicId?: UUID
  topicName?: string
  createdAt?: DateString
  isSaved: boolean
}

export interface VocabularyFilter {
  keyword: string | null
  jlptLevel: JlptLevel | null
  topicName: string | null
  page: number
  size: number
  sort?: string | null
}

export interface CreateVocabularyRequest {
  term: string
  meaning: string
  pronunciation?: string
  example?: string
  exampleMeaning?: string
  audioPath?: string
  topicName: string
  jlptLevel: JlptLevel
}
export interface UpdateVocabularyRequest {
  term: string
  meaning: string
  pronunciation?: string
  example?: string
  exampleMeaning?: string
  audioPath?: string
  topicName?: string
  jlptLevel?: JlptLevel
}
