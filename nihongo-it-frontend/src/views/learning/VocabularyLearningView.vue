<template>
  <div class="vocabulary-learning-container">
    <!-- Header Section -->
    <div class="header px-4 py-3 d-flex justify-space-between align-center">
      <div class="d-flex align-center">
        <v-btn icon class="mr-2" @click="goBack">
          <v-icon>mdi-chevron-left</v-icon>
        </v-btn>
        <div class="text-h6 font-weight-bold text-dark">
          <span class="text-primary">IT</span>
          <span class="japanese-text">単語学習</span>
        </div>
      </div>
      <div class="d-flex align-center">
        <v-btn icon variant="text" @click="openFilterDialog">
          <v-icon>mdi-filter-variant</v-icon>
        </v-btn>
      </div>
    </div>

    <!-- Search Field -->
    <div class="search-container px-4 mb-4">
      <div class="search-field d-flex align-center pa-2 rounded-pill">
        <v-text-field
          v-model="search"
          placeholder="検索 / Tìm kiếm từ vựng"
          prepend-inner-icon="mdi-magnify"
          variant="plain"
          hide-details
          single-line
          density="compact"
          @update:model-value="debouncedFilterVocabulary"
        ></v-text-field>
      </div>
    </div>

    <div class="content-grid px-4">
      <!-- Left Column: Main Content -->
      <div class="main-content-column">
        <!-- Main Content -->
        <div v-if="!loading">
          <!-- Category View -->
          <div class="category-section">
            <div v-for="category in filteredCategories" :key="category.id" class="category-item">
              <div class="text-center mb-1">
                <div class="text-h5 font-weight-bold text-center japanese-text">{{ category.name }}</div>
                <div class="text-subtitle-2 text-center">{{ category.meaning }}</div>
              </div>

              <div class="category-card">
                <v-card
                  class="category-lesson-card"
                  variant="outlined"
                  rounded="lg"
                  @click="selectCategory(category)"
                >
                  <v-img
                    :src="getImagePath(category.name)"
                    height="150"
                    cover
                    class="position-relative"
                  >
                    <div class="category-overlay d-flex flex-column justify-center align-center">
                      <div class="text-h6 font-weight-bold text-white text-center">
                        {{ category.meaning }}
                      </div>
                      <div class="text-body-2 text-white mt-1">
                        {{ getTopicsCount(category) }} topics
                      </div>
                    </div>
                  </v-img>
                </v-card>
              </div>
            </div>

            <!-- Empty State when no categories match search -->
            <div v-if="filteredCategories.length === 0" class="text-center py-8">
              <v-icon size="large" icon="mdi-book-search-outline" class="mb-4"></v-icon>
              <h3 class="text-h6">カテゴリーが見つかりません</h3>
              <p class="text-body-1 text-medium-emphasis">
                No categories match your search criteria.
              </p>
              <v-btn color="primary" class="mt-4" @click="clearAllFilters">
                Clear Search
              </v-btn>
            </div>
          </div>
        </div>

        <!-- Loading Indicator -->
        <div v-if="loading" class="text-center py-8">
          <v-progress-circular indeterminate color="primary"></v-progress-circular>
          <p class="text-body-1 text-medium-emphasis mt-3">Loading...</p>
        </div>
      </div>

      <!-- Right Column: Banners/Guidelines -->
      <div class="banner-column">
        <!-- Learning Path Banner -->
        <v-card class="mb-4 learning-path-card" variant="outlined" rounded="lg">
          <v-card-title class="text-h6 font-weight-bold">
            <v-icon start class="mr-2">mdi-map-marker-path</v-icon>
            学習経路
          </v-card-title>
          <v-card-text>
            <p class="text-body-2">カテゴリーとトピックを選んで、IT用語の学習を始めましょう。</p>
            <div class="d-flex align-center mt-2">
              <v-icon size="small" color="success" class="mr-2">mdi-check-circle</v-icon>
              <span class="text-body-2">基本から応用まで体系的に学べます</span>
            </div>
          </v-card-text>
        </v-card>

        <!-- Study Tips Banner -->
        <v-card class="mb-4 study-tips-card" variant="outlined" rounded="lg">
          <v-card-title class="text-h6 font-weight-bold">
            <v-icon start class="mr-2">mdi-lightbulb-on</v-icon>
            学習のコツ
          </v-card-title>
          <v-card-text>
            <ul class="pl-4">
              <li class="text-body-2 mb-1">毎日少しずつ学習しましょう</li>
              <li class="text-body-2 mb-1">音声を聞いて発音を練習しましょう</li>
              <li class="text-body-2 mb-1">例文を使って実践的に学びましょう</li>
              <li class="text-body-2">不明点はChatGPTに質問しましょう</li>
            </ul>
          </v-card-text>
        </v-card>

        <!-- Resources Banner -->
        <v-card class="resources-card" variant="outlined" rounded="lg">
          <v-card-title class="text-h6 font-weight-bold">
            <v-icon start class="mr-2">mdi-bookshelf</v-icon>
            学習リソース
          </v-card-title>
          <v-card-text>
            <v-img
              src="/images/study-resources.png"
              height="120"
              cover
              class="mb-2 rounded"
            ></v-img>
            <p class="text-body-2">
              IT分野の専門用語を効率よく習得するための追加リソースをご利用ください。
            </p>
            <v-btn
              color="primary"
              variant="text"
              class="px-0 mt-2"
              prepend-icon="mdi-open-in-new"
              density="comfortable"
            >
              リソースを見る
            </v-btn>
          </v-card-text>
        </v-card>
      </div>
    </div>

    <!-- Filter Dialog -->
    <v-dialog v-model="showFilterDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h6 font-weight-bold">
          カテゴリーフィルター
        </v-card-title>
        <v-card-text>
          <v-text-field
            v-model="search"
            label="カテゴリー検索"
            variant="outlined"
            clearable
            class="mb-4"
            prepend-inner-icon="mdi-magnify"
            @update:model-value="debouncedFilterCategories"
          ></v-text-field>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="clearAllFilters">キャンセル</v-btn>
          <v-btn color="primary" @click="showFilterDialog = false">確認</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import vocabularyService from '@/services/vocabulary.service'
import type { VocabularyItem, VocabularyFilter } from '@/services/vocabulary.service'
import { useToast } from 'vue-toast-notification'
import axios from 'axios'
import authService from '@/services/auth.service'

// Define interfaces for the data structures
interface Topic {
  id: string
  name: string
  meaning: string
  categoryId?: string
  category?: {
    id: string
    name: string
  }
  displayOrder: number
  vocabularyCount?: number
}

interface Category {
  id: string
  name: string
  meaning: string
  displayOrder: number
}

// Add interfaces for ChatGPT functionality
interface AnimatedExample {
  japanese: string;
  vietnamese: string;
  japaneseDisplayed?: string;
  vietnameseDisplayed?: string;
}

const router = useRouter()
const route = useRoute()
const toast = useToast()

// State for API data
const loading = ref(false)
const categories = ref<Category[]>([])
const topics = ref<Topic[]>([])
const vocabularyItems = ref<VocabularyItem[]>([])
const totalItems = ref(0)
const totalPages = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// UI state
const search = ref('')
const selectedJlptLevel = ref<string | null>(null)
const selectedCategory = ref<Category | null>(null)
const selectedTopic = ref<Topic | null>(null)
const expandedItems = ref<string[]>([])
const showFilterDialog = ref(false)
const tempSelectedCategory = ref<string | null>(null)
const tempSelectedTopic = ref<string | null>(null)
const jlptLevels = ref<string[]>([])

// Add new states for audio playing and ChatGPT functionality
const playingAudioId = ref<string | null>(null)
const playingExampleAudioId = ref<string | null>(null)
const chatGPTItems = ref<string[]>([])
const loadingChatGPT = ref<string | null>(null)
const chatInputs = ref<Record<string, string>>({})
const typingInProgress = ref<string | null>(null)
const displayedText = ref<Record<string, string>>({})
const typingExamples = ref<Record<string, number>>({})
const typingSpeed = 20 // ms between characters

// Computed
const availableTopics = computed(() => {
  if (!tempSelectedCategory.value) return []
  return topics.value.filter(t => {
    const topicCategoryId = t.categoryId || (t.category && t.category.id)
    return topicCategoryId === tempSelectedCategory.value
  })
})

const filteredCategories = computed(() => {
  if (!search.value) return categories.value

  return categories.value.filter(category => {
    return category.name.toLowerCase().includes(search.value.toLowerCase()) ||
           category.meaning.toLowerCase().includes(search.value.toLowerCase())
  })
})

const hasActiveFilters = computed(() => {
  return selectedJlptLevel.value || selectedCategory.value || selectedTopic.value || search.value
})

// Debounce function for search
let searchTimeout: any = null
function debouncedFilterVocabulary() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    fetchVocabulary()
  }, 500)
}

// Debounce function for category search
let categorySearchTimeout: any = null
function debouncedFilterCategories() {
  clearTimeout(categorySearchTimeout)
  categorySearchTimeout = setTimeout(() => {
    // We don't need to fetch categories as they're already loaded
    // Just let the v-for directive filter them based on search.value
  }, 300)
}

// Watchers
watch([selectedTopic, selectedJlptLevel, selectedCategory, search, currentPage], () => {
  // Save current search/filter state to localStorage
  saveSearchState();

  if (selectedTopic.value) {
    // Reset pagination when topic changes
    currentPage.value = 1
    fetchVocabulary()
  }
})

// Function to save search state to localStorage
function saveSearchState() {
  const searchState = {
    search: search.value,
    jlptLevel: selectedJlptLevel.value,
    categoryId: selectedCategory.value?.id,
    topicId: selectedTopic.value?.id,
    page: currentPage.value
  };

  localStorage.setItem('vocabularyLearningSearchState', JSON.stringify(searchState));
}

async function toggleSave(item: VocabularyItem) {
    const toast = useToast();
    try {
      if (item.isSaved) {
        await vocabularyService.removeSavedVocabulary(item.vocabId);
        toast.success('Removed from saved items', {
          position: 'top',
          duration: 2000
        });
      } else {
        await vocabularyService.saveVocabulary(item.vocabId);
        toast.success('Added to saved items', {
          position: 'top',
          duration: 2000
        });
      }

      // Toggle the state locally
      item.isSaved = !item.isSaved;

    } catch (error) {
      console.error('Error toggling save status:', error);
      toast.error('Failed to update saved status', {
        position: 'top',
        duration: 3000
      });
    }
}

// Function to restore search state from localStorage or URL params
function restoreSearchState() {
  // First check URL query parameters
  const { search: querySearch, jlptLevel, categoryId, topicId, page } = route.query;

  // Then check localStorage
  let savedState = null;
  const savedStateJson = localStorage.getItem('vocabularyLearningSearchState');
  if (savedStateJson) {
    try {
      savedState = JSON.parse(savedStateJson);
    } catch (e) {
      console.error('Error parsing saved search state:', e);
    }
  }

  // Apply state with URL parameters taking precedence over localStorage
  if (querySearch) search.value = querySearch as string;
  else if (savedState?.search) search.value = savedState.search;

  if (jlptLevel) selectedJlptLevel.value = jlptLevel as string;
  else if (savedState?.jlptLevel) selectedJlptLevel.value = savedState.jlptLevel;

  // For category and topic, we need to load the actual objects
  let categoryIdToRestore = (categoryId as string) || savedState?.categoryId;
  let topicIdToRestore = (topicId as string) || savedState?.topicId;

  if (categoryIdToRestore) {
    const foundCategory = categories.value.find(c => c.id === categoryIdToRestore);
    if (foundCategory) {
      selectedCategory.value = foundCategory;
      tempSelectedCategory.value = foundCategory.id;
    }
  }

  if (topicIdToRestore) {
    const foundTopic = topics.value.find(t => t.id === topicIdToRestore);
    if (foundTopic) {
      selectedTopic.value = foundTopic;
      tempSelectedTopic.value = foundTopic.id;
    }
  }

  // Restore page if available
  if (page) currentPage.value = parseInt(page as string, 10);
  else if (savedState?.page) currentPage.value = savedState.page;

  // If we have any filters, fetch vocabulary
  if (selectedTopic.value || selectedJlptLevel.value || search.value) {
    fetchVocabulary();
  }
}

// Lifecycle hooks
onMounted(async () => {
  await Promise.all([
    fetchCategories(),
    fetchTopics(),
    fetchJlptLevels()
  ]);

  // After data is loaded, attempt to restore previous state
  restoreSearchState();
})

// API Methods
async function fetchCategories() {
  try {
    loading.value = true
    const response = await vocabularyService.getCategories()

    if (Array.isArray(response)) {
      categories.value = response.map(category => {
        return {
          id: category.id || category.categoryId,
          name: category.name,
          meaning: category.meaning,
          displayOrder: category.displayOrder || 0
        }
      })
    } else {
      console.error('Categories response is not an array:', response)
      categories.value = []
    }
  } catch (error) {
    console.error('Error fetching categories:', error)
    toast.error('Failed to load categories', {
      position: 'top',
      duration: 3000
    })
  } finally {
    loading.value = false
  }
}

async function fetchTopics() {
  try {
    loading.value = true
    const response = await vocabularyService.getTopics()

    if (Array.isArray(response)) {
      topics.value = response.map(topic => {
        // Make sure each topic has the required fields
        return {
          id: topic.id || topic.topicId,
          name: topic.name,
          meaning: topic.meaning,
          categoryId: topic.categoryId || topic.category?.id,
          displayOrder: topic.displayOrder || 0,
          vocabularyCount: topic.vocabularyCount || 0
        }
      })
    } else {
      console.error('Topics response is not an array:', response)
      topics.value = []
    }
  } catch (error) {
    console.error('Error fetching topics:', error)
    toast.error('Failed to load topics', {
      position: 'top',
      duration: 3000
    })
  } finally {
    loading.value = false
  }
}

async function fetchJlptLevels() {
  try {
    const response = await vocabularyService.getJlptLevels()
    jlptLevels.value = response
  } catch (error) {
    console.error('Error fetching JLPT levels:', error)
    // Use default values if API call fails
    jlptLevels.value = ['N1', 'N2', 'N3', 'N4', 'N5']
  }
}

async function fetchVocabulary() {
  try {
    loading.value = true

    // Create filter object
    const filter: VocabularyFilter = {
      keyword: search.value || null,
      jlptLevel: selectedJlptLevel.value,
      topicName: selectedTopic.value?.name || null,
      page: currentPage.value - 1, // API uses 0-based pagination
      size: pageSize.value
    }

    const response = await vocabularyService.getVocabulary(filter)

    if (response && Array.isArray(response.content)) {
      vocabularyItems.value = response.content
      totalItems.value = response.totalElements
      totalPages.value = response.totalPages
    } else {
      vocabularyItems.value = []
      totalItems.value = 0
      totalPages.value = 0
    }
  } catch (error) {
    console.error('Error fetching vocabulary:', error)
    toast.error('Failed to load vocabulary', {
      position: 'top',
      duration: 3000
    })
    vocabularyItems.value = []
    totalItems.value = 0
    totalPages.value = 0
  } finally {
    loading.value = false
  }
}

async function fetchTopicsByCategory(categoryId: string) {
  try {
    const response = await vocabularyService.getTopicsByCategory(categoryId)
    return response
  } catch (error) {
    console.error(`Error fetching topics for category ${categoryId}:`, error)
    toast.error('Failed to load topics for this category', {
      position: 'top',
      duration: 3000
    })
    return []
  }
}

// Navigation and UI Methods
function goBack() {
  router.back()
}

function getImagePath(categoryName: string): string {
  const categoryImages: Record<string, string> = {
    'IT基礎': 'it-basics.png',
    'プログラミング': 'programming.png',
    'ウェブ開発': 'web-dev.png',
    'データベース': 'database.png',
    'AI・データ': 'ai-data.png',
    'コミュニケーション': 'communication.png',
    'キャリア実務': 'career.png',
  }

  return `/images/categories/${categoryImages[categoryName] || 'default.png'}`
}

function getTopicsCount(category: Category): number {
  const matchingTopics = topics.value.filter(t => {
    const topicCategoryId = t.categoryId || (t.category && t.category.id)
    return topicCategoryId === category.id
  })
  return matchingTopics.length
}

function getTopicsByCategory(category: Category): Topic[] {
  const matchingTopics = topics.value.filter(t => {
    const topicCategoryId = t.categoryId || (t.category && t.category.id)
    return topicCategoryId === category.id
  })

  return matchingTopics.sort((a, b) => a.displayOrder - b.displayOrder)
}

function getVocabularyCount(topic: Topic): number | string {
  // This would ideally come from the API, but for now we can return a placeholder
  return topic.vocabularyCount || '?'
}

function selectCategory(category: Category) {
  // Navigate to the category detail page
  router.push({
    name: 'categoryDetail',
    params: { slug: encodeURIComponent(category.name) }
  });
}

function backToTopics() {
  selectedTopic.value = null
}

function toggleExpand(vocabId: string) {
  const index = expandedItems.value.indexOf(vocabId)
  if (index === -1) {
    expandedItems.value.push(vocabId)
  } else {
    expandedItems.value.splice(index, 1)
  }
}

function openFilterDialog() {
  tempSelectedCategory.value = selectedCategory.value ? selectedCategory.value.id : null
  tempSelectedTopic.value = selectedTopic.value ? selectedTopic.value.id : null
  showFilterDialog.value = true
}

function updateTopicsForCategory() {
  tempSelectedTopic.value = null
}

function applyFilters() {
  // Apply JLPT filter

  // Apply category & topic filters
  if (tempSelectedCategory.value) {
    selectedCategory.value = categories.value.find(c => c.id === tempSelectedCategory.value) || null

    if (tempSelectedTopic.value) {
      selectedTopic.value = topics.value.find(t => t.id === tempSelectedTopic.value) || null
      // When topic is selected, fetch vocabulary
      fetchVocabulary()
    } else {
      selectedTopic.value = null
    }
  } else {
    selectedCategory.value = null
    selectedTopic.value = null
  }

  showFilterDialog.value = false

  // If we have JLPT filter but no topic, fetch vocabulary
  if (selectedJlptLevel.value && !selectedTopic.value) {
    fetchVocabulary()
  }
}

function clearFilter(type: string) {
  if (type === 'jlptLevel') {
    selectedJlptLevel.value = null
  } else if (type === 'category') {
    selectedCategory.value = null
    selectedTopic.value = null
  } else if (type === 'topic') {
    selectedTopic.value = null
  }

  // Fetch vocabulary with updated filters
  if (selectedTopic.value || selectedJlptLevel.value) {
    fetchVocabulary()
  }
}

function clearAllFilters() {
  selectedJlptLevel.value = null
  selectedCategory.value = null
  selectedTopic.value = null
  tempSelectedCategory.value = null
  tempSelectedTopic.value = null
  search.value = ''

  // Hide the filter dialog
  showFilterDialog.value = false
}

function handlePageChange(page: number) {
  currentPage.value = page
  fetchVocabulary()
}

// Add enhanced audio playing functionality
async function playAudio(item: VocabularyItem, isExample = false) {
  // Set the ID of the item we're playing audio for
  if (isExample) {
    playingExampleAudioId.value = item.vocabId
  } else {
    playingAudioId.value = item.vocabId
  }

  const audioPath = item.audioPath || null
  try {
    if (audioPath) {
      // If there's an existing audio path, use it
      const audio = new Audio(audioPath)
      await audio.play()
    } else {
      // Verify authentication before proceeding
      const authToken = authService.getToken()
      if (!authToken) {
        toast.error('Please log in to use text-to-speech', {
          position: 'top',
          duration: 4000
        })
        // Redirect to login page after a short delay
        setTimeout(() => {
          router.push({
            name: 'login',
            query: { redirect: router.currentRoute.value.fullPath }
          })
        }, 1500)
        return
      }

      // Determine which text to use for speech
      let textToSpeak = ''
      if (isExample && item.example) {
        // Use example sentence if requested and available
        textToSpeak = item.example
      } else {
        // Use term for vocabulary pronunciation
        textToSpeak = item.pronunciation ? item.pronunciation : item.term
      }

      // Show loading indicator
      toast.info('Generating audio...', {
        position: 'top',
        duration: 2000
      })

      // Get the backend API URL
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

      // Set speed: 0.9 for vocabulary, 1.0 for example sentences
      const speed = isExample ? 1.0 : 0.9

      // Call the TTS API with Authorization header
      const response = await axios.post(`${apiUrl}/api/v1/tts/generate`, textToSpeak, {
        headers: {
          'Content-Type': 'text/plain; charset=UTF-8',
          'Accept-Language': 'ja-JP',
          'X-Speech-Speed': speed.toString(),
          'X-Content-Language': 'ja',
          'X-Content-Is-Example': isExample.toString(),
          'Authorization': `Bearer ${authToken}`,
          'Accept': 'audio/mpeg'
        },
        responseType: 'arraybuffer'
      })

      // Convert response to blob and create audio URL
      const audioBlob = new Blob([response.data], { type: 'audio/mpeg' })
      const audioUrl = URL.createObjectURL(audioBlob)

      // Play the audio
      const audio = new Audio(audioUrl)
      audio.onended = () => {
        URL.revokeObjectURL(audioUrl)
        if (isExample) {
          playingExampleAudioId.value = null
        } else {
          playingAudioId.value = null
        }
      }
      await audio.play()
    }
  } catch (error) {
    console.error('Error generating or playing TTS audio:', error)

    // Special handling for 401 errors
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      toast.error('TTS service requires authentication. Please log in again when convenient.', {
        position: 'top',
        duration: 3000
      })
    } else {
      toast.error(error instanceof Error ? error.message : 'Failed to generate speech', {
        position: 'top',
        duration: 3000
      })
    }
  } finally {
    // Reset loading state if no audio was played (in case of error)
    // For successful audio playback, the onended event will handle this
    if (!audioPath) {
      setTimeout(() => {
        if (isExample) {
          playingExampleAudioId.value = null
        } else {
          playingAudioId.value = null
        }
      }, 3000)
    } else {
      if (isExample) {
        playingExampleAudioId.value = null
      } else {
        playingAudioId.value = null
      }
    }
  }
}

// Add ChatGPT functionality
function toggleChatGPT(vocabId: string) {
  const index = chatGPTItems.value.indexOf(vocabId)

  if (index >= 0) {
    // If already open, close it
    chatGPTItems.value.splice(index, 1)
  } else {
    // Start loading state
    loadingChatGPT.value = vocabId

    // Find the vocabulary item by ID
    const vocabItem = vocabularyItems.value.find(item => item.vocabId === vocabId)

    if (!vocabItem) {
      console.error('Vocabulary item not found')
      loadingChatGPT.value = null
      return
    }

    // Initialize chat input for this item if not exists
    if (!chatInputs.value[vocabId]) {
      chatInputs.value[vocabId] = ''
    }

    // Get the backend API URL
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

    // Query the AI for vocabulary explanation
    axios.post(`${apiUrl}/api/v1/ai/vocabulary/explain`, null, {
      params: {
        term: vocabItem.term || '',
        pronunciation: vocabItem.pronunciation || '',
        meaning: vocabItem.meaning || '',
        topicName: vocabItem.topicName || '',
        example: vocabItem.example || ''
      }
    })
    .then(response => {
      // Store the AI response for this vocabulary
      const vocabIndex = vocabularyItems.value.findIndex(item => item.vocabId === vocabId)
      if (vocabIndex !== -1) {
        let data = response.data
        // If data is a string, try to parse it as JSON
        if (typeof data === 'string') {
          try {
            // Clean string by removing markdown code blocks if present
            const cleanedString = data
              .replace(/```json/g, '')
              .replace(/```/g, '')
              .trim()

            data = JSON.parse(cleanedString)
          } catch (error) {
            console.error('Failed to parse response as JSON:', error)
            // If parsing fails, create a fallback object
            data = {
              explanation: data, // Use the raw string as explanation
              examples: []
            }
          }
        }

        // Add AI response data to the vocabulary item
        vocabularyItems.value[vocabIndex].aiExplanation = data.explanation
        vocabularyItems.value[vocabIndex].aiExamples = data.examples || []

        // Initialize the displayed text as empty
        displayedText.value[vocabId] = ''

        // Add to visible chatGPT items
        chatGPTItems.value.push(vocabId)

        // Start the typing animation
        animateTyping(vocabId, data.explanation)
      }
    })
    .catch(error => {
      console.error('Error fetching AI explanation:', error)
      toast.error('Failed to fetch AI explanation', {
        position: 'top',
        duration: 3000
      })
    })
    .finally(() => {
      loadingChatGPT.value = null
    })
  }
}

function animateTyping(vocabId: string, text: string) {
  typingInProgress.value = vocabId
  let currentIndex = 0
  displayedText.value[vocabId] = ''

  const typeNextChar = () => {
    if (currentIndex < text.length) {
      displayedText.value[vocabId] += text.charAt(currentIndex)
      currentIndex++
      setTimeout(typeNextChar, typingSpeed)
    } else {
      // Start animating examples after main text is complete
      const vocabItem = vocabularyItems.value.find(item => item.vocabId === vocabId)
      if (vocabItem?.aiExamples && vocabItem.aiExamples.length > 0) {
        typingExamples.value[vocabId] = 0
        animateExamples(vocabId, vocabItem.aiExamples)
      } else {
        typingInProgress.value = null
      }
    }
  }

  typeNextChar()
}

function animateExamples(vocabId: string, examples: any[]) {
  if (!examples || examples.length === 0 || typingExamples.value[vocabId] >= examples.length) {
    typingExamples.value[vocabId] = -1 // All examples are displayed
    typingInProgress.value = null
    return
  }

  const currentExampleIndex = typingExamples.value[vocabId]
  const example = examples[currentExampleIndex] as AnimatedExample

  // Animate Japanese text first
  animateExampleText(vocabId, 'japanese', example.japanese, () => {
    // Then animate Vietnamese text
    animateExampleText(vocabId, 'vietnamese', example.vietnamese, () => {
      // Move to next example
      typingExamples.value[vocabId]++
      setTimeout(() => {
        animateExamples(vocabId, examples)
      }, 300) // Pause between examples
    })
  })
}

function animateExampleText(vocabId: string, field: string, text: string, callback: () => void) {
  let currentIndex = 0
  const exampleIndex = typingExamples.value[vocabId]

  // Initialize the example text if needed
  const vocabIndex = vocabularyItems.value.findIndex(item => item.vocabId === vocabId)
  if (vocabIndex !== -1 && vocabularyItems.value[vocabIndex].aiExamples) {
    const example = vocabularyItems.value[vocabIndex].aiExamples[exampleIndex] as any
    if (!example[field + 'Displayed']) {
      example[field + 'Displayed'] = ''
    }
  }

  const typeNextChar = () => {
    if (currentIndex < text.length) {
      const vocabIndex = vocabularyItems.value.findIndex(item => item.vocabId === vocabId)
      if (vocabIndex !== -1 && vocabularyItems.value[vocabIndex].aiExamples) {
        const example = vocabularyItems.value[vocabIndex].aiExamples[exampleIndex] as any
        example[field + 'Displayed'] = text.substring(0, currentIndex + 1)
      }
      currentIndex++
      setTimeout(typeNextChar, typingSpeed)
    } else {
      callback()
    }
  }

  typeNextChar()
}

async function sendChatMessage(vocabId: string) {
  if (!chatInputs.value[vocabId]) return

  const message = chatInputs.value[vocabId]
  chatInputs.value[vocabId] = '' // Clear input immediately for better UX

  const vocabItem = vocabularyItems.value.find(item => item.vocabId === vocabId)
  if (!vocabItem) {
    console.error('Vocabulary item not found')
    return
  }

  // Create a local copy to maintain reactivity
  const localMessageHistory = vocabItem.chatHistory || []

  // Add user message to history
  localMessageHistory.push({
    role: 'user',
    content: message
  })

  // Update the vocabulary item with the new message
  const vocabIndex = vocabularyItems.value.findIndex(item => item.vocabId === vocabId)
  if (vocabIndex !== -1) {
    // Ensure chatHistory exists and is an array
    if (!vocabularyItems.value[vocabIndex].chatHistory) {
      vocabularyItems.value[vocabIndex].chatHistory = []
    }
    vocabularyItems.value[vocabIndex].chatHistory = [...localMessageHistory]
  }

  // Fetch AI response for the user message
  try {
    // Get the backend API URL
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

    const vocabWord = vocabItem.term || ''

    const response = await axios.post(`${apiUrl}/api/v1/ai/vocabulary/chat`, null, {
      params: {
        vocabWord: vocabWord,
        userMessage: message
      }
    })

    // Process the response data
    let data = response.data
    // If data is a string, try to parse it as JSON
    if (typeof data === 'string') {
      try {
        // Clean string by removing markdown code blocks if present
        const cleanedString = data
          .replace(/```json/g, '')
          .replace(/```/g, '')
          .trim()

        data = JSON.parse(cleanedString)
      } catch (error) {
        console.error('Failed to parse chat response as JSON:', error)
        // If parsing fails, create a fallback object with raw string as message
        data = {
          message: data
        }
      }
    }

    // Add AI response to chat history
    if (vocabIndex !== -1) {
      // Ensure chatHistory exists and is an array
      if (!vocabularyItems.value[vocabIndex].chatHistory) {
        vocabularyItems.value[vocabIndex].chatHistory = []
      }

      // Use the message from the parsed data, or a fallback message
      const responseMessage = data && data.message
        ? data.message
        : "I'm sorry, I don't have a specific response for that. Could you try asking something else?"

      // Add empty response message first (for typing animation)
      const aiMessageIndex = vocabularyItems.value[vocabIndex].chatHistory!.length
      vocabularyItems.value[vocabIndex].chatHistory!.push({
        role: 'assistant',
        content: '' // Empty content initially
      })

      // Start typing animation for the response
      animateTypingChatResponse(vocabId, responseMessage, vocabIndex, aiMessageIndex)
    }
  } catch (error) {
    console.error('Error sending chat message:', error)

    toast.error('Failed to get AI response', {
      position: 'top',
      duration: 3000
    })

    // Add error message to chat history
    if (vocabIndex !== -1) {
      // Ensure chatHistory exists and is an array
      if (!vocabularyItems.value[vocabIndex].chatHistory) {
        vocabularyItems.value[vocabIndex].chatHistory = []
      }
      vocabularyItems.value[vocabIndex].chatHistory!.push({
        role: 'assistant',
        content: 'Sorry, I encountered an error processing your request. Please try again.'
      })
    }
  }
}

function animateTypingChatResponse(vocabId: string, text: string, vocabIndex: number, messageIndex: number) {
  typingInProgress.value = vocabId
  let currentIndex = 0

  const typeNextChar = () => {
    if (currentIndex < text.length) {
      if (vocabularyItems.value[vocabIndex]?.chatHistory &&
          vocabularyItems.value[vocabIndex].chatHistory![messageIndex]) {
        vocabularyItems.value[vocabIndex].chatHistory![messageIndex].content =
          text.substring(0, currentIndex + 1)
      }
      currentIndex++
      setTimeout(typeNextChar, typingSpeed)
    } else {
      typingInProgress.value = null
    }
  }

  typeNextChar()
}

// Helper methods for example display
function getExampleProperty(example: any, property: string): string {
  return example && example[property] ? example[property] : ''
}

function isJapaneseComplete(example: any): boolean {
  return example &&
         example.japaneseDisplayed &&
         example.japanese &&
         example.japaneseDisplayed.length === example.japanese.length
}

function hasVietnameseStarted(example: any): boolean {
  return example && example.vietnameseDisplayed ? true : false
}

function getJlptColor(level: string): string {
  switch (level) {
    case 'N1': return 'red'
    case 'N2': return 'orange'
    case 'N3': return 'amber'
    case 'N4': return 'light-green'
    case 'N5': return 'green'
    default: return 'grey'
  }
}

function handleTopicSelect(topic: Topic) {
  selectedTopic.value = topic
  currentPage.value = 1 // Reset pagination
}

function navigateToDetail(term: string) {
  // Save the current search state before navigating away
  saveSearchState();

  // Add a 'from' parameter to indicate where we're coming from
  router.push({
    name: 'vocabularyPronunciation',
    params: { term },
    query: { from: 'vocabularyLearning' }
  });
}
</script>

<style scoped lang="scss">
.vocabulary-learning-container {
  background-color: #f7f8fa;
  min-height: 100vh;
  padding-bottom: 64px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  max-width: 1440px;
  margin: 0 auto;
}

.main-content-column {
  width: 100%;
  background-color: #f1f5f9;
  border-radius: 8px;
  padding: 16px;
}

.banner-column {
  width: 100%;
}

/* Responsive layout - stack columns on smaller screens */
@media (max-width: 960px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .banner-column {
    display: none;
  }
}

/* Make categories single column on mobile */
@media (max-width: 768px) {
  .category-section {
    grid-template-columns: 1fr;
  }
}

.japanese-text {
  font-family: 'Noto Sans JP', sans-serif;
}

.search-field {
  background-color: #f0f0f0;
  height: 48px;
  border-radius: 24px;
}

.category-item {
  margin-bottom: 24px;
}

.category-lesson-card {
  overflow: hidden;
  transition: transform 0.2s ease;
  max-width: 100%;
  background-color: rgba(255, 255, 255, 0.8);

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1) !important;
  }
}

.category-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 16px;
}

.category-overlay {
  background: linear-gradient(0deg, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.3) 100%);
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

/* Banner card styles */
.learning-path-card {
  border-left: 4px solid #42A5F5 !important;
}

.study-tips-card {
  border-left: 4px solid #66BB6A !important;
}

.resources-card {
  border-left: 4px solid #FFA726 !important;
}

.topic-card {
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
  }
}

.vocabulary-item {
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
  }
}

.meaning-text {
  color: rgba(0, 0, 0, 0.6);
}

.example-section {
  background-color: #f5f5f5;
  border-left: 3px solid rgba(0, 0, 0, 0.1);
}

.example-translation {
  color: rgba(0, 0, 0, 0.6);
  font-style: italic;
}

/* ChatGPT and audio styles */
.chatgpt-content {
  background-color: #f0f7ff;
  border-top: 1px dashed rgba(0, 150, 0, 0.15);
  border-bottom: 1px dashed rgba(0, 150, 0, 0.15);
}

.chatgpt-card {
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
}

.chatgpt-message {
  line-height: 1.5;
  color: rgba(0, 0, 0, 0.7);
}

.example-text {
  color: rgba(0, 0, 0, 0.6);
  font-style: italic;
  padding-left: 12px;
  border-left: 2px solid rgba(0, 150, 0, 0.3);
}

.chat-input-container {
  background-color: rgba(255, 255, 255, 0.8);
  padding: 8px;
  border-radius: 8px;
}

.chat-gpt-btn {
  font-size: 0.75rem;
  height: 28px;
  opacity: 0.9;
  transition: all 0.2s ease;
  margin-left: 4px;

  &:hover {
    opacity: 1;
    transform: scale(1.05);
  }
}

.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 16px;
  background: #333;
  margin-left: 2px;
  animation: blink 0.7s infinite;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
</style>
