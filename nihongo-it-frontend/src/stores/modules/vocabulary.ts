import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import vocabularyService from '@/services/vocabulary.service'
import type { VocabularyItem, VocabularyFilter, PagedResponse } from '@/services/vocabulary.service'
import { useToast } from 'vue-toast-notification'

export const useVocabularyStore = defineStore('vocabulary', () => {
  // State
  const currentVocabulary = ref<VocabularyItem | null>(null)
  const relatedVocabulary = ref<VocabularyItem[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const processedExample = ref('')

  // Getters
  const hasVocabulary = computed(() => !!currentVocabulary.value)
  const hasRelatedItems = computed(() => relatedVocabulary.value.length > 0)
  const isFavorite = computed(() => currentVocabulary.value?.isSaved || false)

  // Actions
  async function fetchVocabularyById(id: string) {
    loading.value = true
    error.value = null

    try {
      const vocabulary = await vocabularyService.getVocabularyById(id)
      currentVocabulary.value = vocabulary
      await fetchRelatedTerms(vocabulary)
      return vocabulary
    } catch (err: any) {
      console.error('Error fetching vocabulary details:', err)
      error.value = err.response?.data?.message || 'Failed to load vocabulary details'
      const toast = useToast()
      toast.error(error.value, {
        position: 'top',
        duration: 3000
      })
      return null
    } finally {
      loading.value = false
    }
  }

  async function fetchRelatedTerms(vocabulary: VocabularyItem) {
    try {
      // Fetch related terms based on category or JLPT level
      const filters = {
        category: vocabulary.category,
        jlptLevel: vocabulary.jlptLevel,
        page: 0,
        size: 5
      } as VocabularyFilter

      const response = await vocabularyService.getVocabulary(filters)

      if (response && Array.isArray(response.content)) {
        // Filter out the current vocabulary item
        relatedVocabulary.value = response.content
          .filter(item => item.vocabId !== vocabulary.vocabId)
          .slice(0, 4) // Limit to 4 items
      }
    } catch (err) {
      console.error('Error fetching related terms:', err)
      relatedVocabulary.value = []
      // Don't show error toast, this is not critical
    }
  }

  async function toggleFavorite() {
    if (!currentVocabulary.value) return false

    const toast = useToast()
    try {
      if (isFavorite.value) {
        await vocabularyService.removeSavedVocabulary(currentVocabulary.value.vocabId)
        toast.success('Removed from saved items', {
          position: 'top',
          duration: 2000
        })
      } else {
        await vocabularyService.saveVocabulary(currentVocabulary.value.vocabId)
        toast.success('Added to saved items', {
          position: 'top',
          duration: 2000
        })
      }

      // Toggle the state locally
      if (currentVocabulary.value) {
        currentVocabulary.value = {
          ...currentVocabulary.value,
          isSaved: !isFavorite.value
        }
      }

      return true
    } catch (err) {
      console.error('Error toggling save status:', err)
      toast.error('Failed to update saved status', {
        position: 'top',
        duration: 3000
      })
      return false
    }
  }

  function reset() {
    currentVocabulary.value = null
    relatedVocabulary.value = []
    loading.value = false
    error.value = null
    processedExample.value = ''
  }

  function setProcessedExample(text: string) {
    processedExample.value = text
  }

  return {
    // State
    currentVocabulary,
    relatedVocabulary,
    loading,
    error,
    processedExample,

    // Getters
    hasVocabulary,
    hasRelatedItems,
    isFavorite,

    // Actions
    fetchVocabularyById,
    fetchRelatedTerms,
    toggleFavorite,
    reset,
    setProcessedExample
  }
})