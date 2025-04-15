import axios from 'axios'

// Types
export interface VocabularyItem {
  vocabId: string
  hiragana: string | null
  kanji: string | null
  katakana: string | null
  meaning: string
  exampleSentence: string | null
  exampleSentenceTranslation: string | null
  audioPath: string | null
  audioUrl?: string | null
  exampleAudioPath?: string | null
  category: string | null
  jlptLevel: string
  createdAt: string | null
  createdBy: string | null
  updatedAt?: boolean  // Changed from isSaved to match API response
  isSaved?: boolean    // Keep this for backward compatibility

  // AI-related properties
  aiExplanation?: string
  aiExamples?: ExampleSentence[]
  chatHistory?: ChatMessage[]
}

export interface VocabularyFilter {
  jlptLevel?: string | null
  category?: string | null
  keyword?: string | null
  page?: number
  size?: number
  sort?: string
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
  note?: string
}

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

const API_URL = '/api/v1/vocabulary'

class VocabularyService {
  // Get vocabulary with filters
  async getVocabulary(filter: VocabularyFilter): Promise<PagedResponse<VocabularyItem>> {
    try {
      const response = await axios.get(API_URL, {
        params: filter,
        headers: {
          'Accept': 'application/json'
        }
      })

      // Transform the response data to match our expected format
      const data = response.data;
      if (data && Array.isArray(data.content)) {
        // Add isSaved property if it doesn't exist and handle audioUrl
        data.content = data.content.map((item: VocabularyItem) => ({
          ...item,
          isSaved: item.isSaved || false,
          audioPath: item.audioPath || item.audioUrl || null // Use audioUrl as fallback
        }));
      }

      console.log('API Response:', data);
      return data;
    } catch (error) {
      console.error('Error in getVocabulary:', error);
      throw error;
    }
  }

  // Get vocabulary by ID
  async getVocabularyById(id: string): Promise<VocabularyItem> {
    try {
      const response = await axios.get(`${API_URL}/${id}`, {
        headers: { 'Accept': 'application/json' }
      })

      // Handle the data nested in the response
      const vocabItem = response.data.data || response.data;
      return {
        ...vocabItem,
        isSaved: vocabItem.isSaved || false,
        audioPath: vocabItem.audioPath || vocabItem.audioUrl || null // Use audioUrl as fallback
      };
    } catch (error) {
      console.error('Error in getVocabularyById:', error);
      throw error;
    }
  }

  // Save vocabulary to user's notebook
  async saveVocabulary(id: string): Promise<VocabularyItem> {
    try {
      const response = await axios.post(`${API_URL}/${id}/save`, null, {
        headers: { 'Accept': 'application/json' }
      })
      return {
        ...response.data,
        isSaved: true
      };
    } catch (error) {
      console.error('Error in saveVocabulary:', error);
      throw error;
    }
  }

  // Remove vocabulary from user's notebook
  async removeSavedVocabulary(id: string): Promise<VocabularyItem> {
    try {
      const response = await axios.delete(`${API_URL}/${id}/save`, {
        headers: { 'Accept': 'application/json' }
      })
      return {
        ...response.data,
        isSaved: false
      };
    } catch (error) {
      console.error('Error in removeSavedVocabulary:', error);
      throw error;
    }
  }

  // Get saved vocabulary
  async getSavedVocabulary(filter: VocabularyFilter | URLSearchParams): Promise<PagedResponse<VocabularyItem>> {
    let url = '/api/v1/vocabulary/saved'

    try {
      if (filter instanceof URLSearchParams) {
        url += `?${filter.toString()}`
        const response = await axios.get(url)
        return response.data
      } else {
        // Build query params the old way
        const params = new URLSearchParams()
        if (filter.page !== undefined) params.append('page', filter.page.toString())
        if (filter.size !== undefined) params.append('size', filter.size.toString())
        if (filter.keyword) params.append('keyword', filter.keyword)
        if (filter.sort) params.append('sort', filter.sort)

        url += `?${params.toString()}`
        const response = await axios.get(url)
        return response.data
      }
    } catch (error) {
      console.error('Error fetching saved vocabulary:', error)
      throw error
    }
  }
}

export default new VocabularyService()
