import axios from 'axios'
import authService from './auth.service'

// Types
export interface VocabularyItem {
  vocabId: string
  term: string
  meaning: string
  pronunciation?: string
  example?: string
  exampleMeaning?: string
  audioPath?: string
  jlptLevel: string
  topicId?: string
  topicName?: string
  createdAt?: string
  isSaved: boolean

  // For chat interface
  aiExplanation?: string
  aiExamples?: ExampleSentence[]
  chatHistory?: ChatMessage[]
}

export interface VocabularyFilter {
  keyword: string | null
  jlptLevel: string | null
  topicName: string | null
  page: number
  size: number
  sort?: string | null
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  lastPage: boolean
}

export interface ExampleSentence {
  japanese: string
  vietnamese: string
}

export interface ChatMessage {
  role: string
  content: string
}

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// Helper function to get auth header
function authHeader() {
  const token = authService.getToken();
  return token ? { 'Authorization': `Bearer ${token}` } : {};
}

class VocabularyService {
  // Get vocabulary with filters
  async getVocabulary(filter: VocabularyFilter): Promise<PagedResponse<VocabularyItem>> {
    try {
      const response = await axios.get(`${API_URL}/api/v1/vocabulary`, {
        headers: { ...authHeader() },
        params: {
          keyword: filter.keyword || undefined,
          jlptLevel: filter.jlptLevel || undefined,
          topicName: filter.topicName || undefined,
          page: filter.page,
          size: filter.size,
          sort: filter.sort || undefined,
        }
      })
      return response.data
    } catch (error) {
      console.error('Error fetching vocabulary:', error)
      throw error
    }
  }

  // Get vocabulary by ID
  async getVocabularyById(id: string): Promise<VocabularyItem> {
    try {
      const response = await axios.get(`${API_URL}/api/v1/vocabulary/${id}`, {
        headers: { ...authHeader() }
      })
      return response.data.data
    } catch (error) {
      console.error(`Error fetching vocabulary with ID ${id}:`, error)
      throw error
    }
  }

  // Get vocabulary by term
  async getVocabularyByTerm(term: string): Promise<VocabularyItem> {
    try {
      const response = await axios.get(`${API_URL}/api/v1/vocabulary/term/${term}`, {
        headers: { ...authHeader() }
      })
      return response.data.data
    } catch (error) {
      console.error(`Error fetching vocabulary with term ${term}:`, error)
      throw error
    }
  }

  // Save vocabulary to user's notebook
  async saveVocabulary(id: string): Promise<VocabularyItem> {
    try {
      const response = await axios.post(`${API_URL}/api/v1/vocabulary/${id}/save`, {}, {
        headers: { ...authHeader() }
      })
      return response.data
    } catch (error) {
      console.error(`Error saving vocabulary ${id}:`, error)
      throw error
    }
  }

  // Remove vocabulary from user's notebook
  async removeSavedVocabulary(id: string): Promise<VocabularyItem> {
    try {
      const response = await axios.delete(`${API_URL}/api/v1/vocabulary/${id}/save`, {
        headers: { ...authHeader() }
      })
      return response.data
    } catch (error) {
      console.error(`Error removing saved vocabulary ${id}:`, error)
      throw error
    }
  }

  // Get saved vocabulary
  async getSavedVocabulary(filter: VocabularyFilter): Promise<PagedResponse<VocabularyItem>> {
    try {
      const response = await axios.get(`${API_URL}/api/v1/vocabulary/saved`, {
        headers: { ...authHeader() },
        params: {
          keyword: filter.keyword || undefined,
          page: filter.page,
          size: filter.size,
          sort: filter.sort || undefined,
        }
      })
      return response.data
    } catch (error) {
      console.error('Error fetching saved vocabulary:', error)
      throw error
    }
  }

  async getCategories(): Promise<any> {
    try {
      const response = await axios.get(`${API_URL}/api/v1/vocabulary/categories`, {
        headers: { ...authHeader() }
      })
      return response.data
    } catch (error) {
      console.error('Error fetching categories:', error)
      throw error
    }
  }

  async getJlptLevels(): Promise<any> {
    try {
      const response = await axios.get(`${API_URL}/api/v1/vocabulary/jlpt-levels`, {
        headers: { ...authHeader() }
      })
      return response.data
    } catch (error) {
      console.error('Error fetching JLPT levels:', error)
      throw error
    }
  }

  async getTopics(): Promise<any> {
    try {
      const response = await axios.get(`${API_URL}/api/v1/topics`, {
        headers: { ...authHeader() }
      })
      return response.data
    } catch (error) {
      console.error('Error fetching topics:', error)
      throw error
    }
  }

  async getTopicsByCategory(categoryId: string): Promise<any> {
    try {
      const response = await axios.get(`${API_URL}/api/v1/vocabulary/categories/${categoryId}/topics`, {
        headers: { ...authHeader() }
      })
      return response.data
    } catch (error) {
      console.error(`Error fetching topics for category ${categoryId}:`, error)
      throw error
    }
  }

  async createVocabulary(vocabulary: any): Promise<any> {
    try {
      const response = await axios.post(`${API_URL}/api/v1/vocabulary`, vocabulary, {
        headers: { ...authHeader() }
      })
      return response.data
    } catch (error) {
      console.error('Error creating vocabulary:', error)
      throw error
    }
  }

  async updateVocabulary(id: string, vocabulary: any): Promise<any> {
    try {
      const response = await axios.put(`${API_URL}/api/v1/vocabulary/${id}`, vocabulary, {
        headers: { ...authHeader() }
      })
      return response.data
    } catch (error) {
      console.error(`Error updating vocabulary:`, error)
      throw error
    }
  }
}

export default new VocabularyService()
