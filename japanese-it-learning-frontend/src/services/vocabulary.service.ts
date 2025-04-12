import axios from 'axios'

// Types
export interface VocabularyItem {
  vocabId: string
  hiragana: string
  kanji: string | null
  katakana: string | null
  meaning: string
  exampleSentence: string | null
  audioPath: string | null
  audioUrl?: string | null
  exampleAudioPath?: string | null
  category: string | null
  jlptLevel: string
  contentType: string
  createdAt: string
  createdBy: string
  updatedAt?: boolean  // Changed from isSaved to match API response
  isSaved?: boolean    // Keep this for backward compatibility
}

export interface VocabularyFilter {
  keyword?: string | null
  jlptLevel?: string | null
  category?: string | null
  contentType?: string | null
  page: number
  size: number
}

export interface PagedResponse {
  content: VocabularyItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  lastPage: boolean
}

const API_URL = '/api/v1/vocabulary'

class VocabularyService {
  // Get vocabulary with filters
  async getVocabulary(filter: VocabularyFilter): Promise<PagedResponse> {
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
  async getSavedVocabulary(filter: VocabularyFilter): Promise<PagedResponse> {
    try {
      const response = await axios.get(`${API_URL}/saved`, {
        params: filter,
        headers: { 'Accept': 'application/json' }
      })

      // Transform the response data
      const data = response.data;
      if (data && Array.isArray(data.content)) {
        data.content = data.content.map((item: VocabularyItem) => ({
          ...item,
          isSaved: true,
          audioPath: item.audioPath || item.audioUrl || null // Use audioUrl as fallback
        }));
      }

      return data;
    } catch (error) {
      console.error('Error in getSavedVocabulary:', error);
      throw error;
    }
  }
}

export default new VocabularyService()
