import api from '@/lib/api'
import type { FlashcardDTO, FlashcardStats, StudyStatistics } from '@/types/learning.types'

const flashcardService = {
  async getFlashcardsByVocabulary(vocabId: string): Promise<FlashcardDTO[]> {
    const res = await api.get<FlashcardDTO[]>(`/api/v1/learning/flashcards/vocabulary/${vocabId}`)
    return res.data
  },

  async reviewFlashcard(flashcardId: string, rating: number): Promise<FlashcardDTO> {
    const res = await api.post<FlashcardDTO>(
      `/api/v1/learning/flashcards/${flashcardId}/review`,
      { rating },
    )
    return res.data
  },

  async createFlashcardFromVocabulary(vocabId: string): Promise<FlashcardDTO> {
    const res = await api.post<FlashcardDTO>(`/api/v1/learning/flashcards/vocabulary/${vocabId}`, {})
    return res.data
  },

  async getDueCards(): Promise<FlashcardDTO[]> {
    const res = await api.get<FlashcardDTO[]>('/api/v1/learning/flashcards/due')
    return res.data
  },

  async getStudyStatistics(): Promise<StudyStatistics> {
    const res = await api.get<StudyStatistics>('/api/v1/learning/flashcards/statistics')
    return res.data
  },

  async getStudySummary(): Promise<FlashcardStats | null> {
    try {
      const raw = await this.getStudyStatistics()
      const s = raw.summary
      if (!s) return null
      return {
        totalCards: s.totalCards,
        dueCardsNow: s.dueCardsNow,
        currentStreak: s.currentStreak,
        reviewsLast30Days: s.reviewsLast30Days,
        overallRetentionRate: s.overallRetentionRate,
      }
    } catch {
      return null
    }
  },
}

export default flashcardService
