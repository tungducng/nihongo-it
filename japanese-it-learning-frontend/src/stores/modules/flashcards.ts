import { defineStore } from 'pinia'
import axios from 'axios'
import type { AxiosError } from 'axios'

interface Flashcard {
  id: number | string
  // Add other flashcard properties based on your data model
  front?: string
  back?: string
  notes?: string
  // Add any other properties your flashcards have
}

interface Review {
  card: Flashcard | null
  rating: number
  timestamp: Date
}

interface FlashcardsState {
  dueCards: Flashcard[]
  currentCard: Flashcard | null
  reviewHistory: Review[]
  loading: boolean
  error: string | null
}

export const useFlashcardsStore = defineStore('flashcards', {
  state: (): FlashcardsState => ({
    dueCards: [],
    currentCard: null,
    reviewHistory: [],
    loading: false,
    error: null,
  }),

  getters: {
    hasDueCards: (state) => state.dueCards.length > 0,
    dueCardsCount: (state) => state.dueCards.length,
  },

  actions: {
    async fetchDueCards() {
      this.loading = true
      try {
        const response = await axios.get('/api/flashcards/due')
        this.dueCards = response.data
        if (response.data.length > 0) {
          this.currentCard = response.data[0]
        } else {
          this.currentCard = null
        }
        this.error = null
      } catch (error) {
        const axiosError = error as AxiosError
        this.error = axiosError.message
      } finally {
        this.loading = false
      }
    },

    async reviewCard({ cardId, rating }: { cardId: number | string; rating: number }) {
      this.loading = true
      try {
        await axios.post(`/api/flashcards/${cardId}/review`, { rating })

        // Log review in history
        const reviewedCard = this.currentCard
        this.reviewHistory.push({
          card: reviewedCard,
          rating,
          timestamp: new Date(),
        })

        // Remove card from due list
        this.dueCards = this.dueCards.filter((card) => card.id !== cardId)

        // Set next card as current
        if (this.dueCards.length > 0) {
          this.currentCard = this.dueCards[0]
        } else {
          this.currentCard = null
        }

        this.error = null
      } catch (error) {
        const axiosError = error as AxiosError
        this.error = axiosError.message
      } finally {
        this.loading = false
      }
    },

    async createFlashcard(flashcardData: Partial<Flashcard>) {
      this.loading = true
      try {
        await axios.post('/api/flashcards', flashcardData)
        this.error = null
      } catch (error) {
        const axiosError = error as AxiosError
        this.error = axiosError.message
      } finally {
        this.loading = false
      }
    },
  },
})
