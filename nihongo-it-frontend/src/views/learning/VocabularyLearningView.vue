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

    <!-- Active Filters (if any) -->
    <div class="active-filters px-4 mb-3" v-if="hasActiveFilters">
      <div class="d-flex flex-wrap">
        <v-chip
          v-if="selectedJlptLevel"
          size="small"
          color="primary"
          class="mr-2 mb-1"
          closable
          @click:close="clearFilter('jlptLevel')"
        >
          {{ selectedJlptLevel }}
        </v-chip>
        <v-chip
          v-if="selectedTopic"
          size="small"
          color="success"
          class="mr-2 mb-1"
          closable
          @click:close="clearFilter('topic')"
        >
          {{ selectedTopic.meaning }}
        </v-chip>
        <v-chip
          v-if="selectedCategory"
          size="small"
          color="info"
          class="mr-2 mb-1"
          closable
          @click:close="clearFilter('category')"
        >
          {{ selectedCategory.meaning }}
        </v-chip>
      </div>
    </div>

    <!-- Loading Indicator -->
    <div v-if="loading" class="text-center py-8">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
      <p class="text-body-1 text-medium-emphasis mt-3">Loading...</p>
    </div>

    <!-- Main Content -->
    <div v-else class="main-content">
      <!-- Category View (when no category is selected) -->
      <div v-if="!selectedCategory" class="category-section">
        <div v-for="category in categories" :key="category.id" class="mb-6">
          <div class="px-4 mb-3">
            <div class="text-subtitle-1 text-grey text-center">カテゴリー</div>
            <div class="text-h4 font-weight-bold text-center japanese-text">{{ category.name }}</div>
            <div class="text-subtitle-1 text-center">{{ category.meaning }}</div>
          </div>

          <div class="category-card px-4 mb-4">
            <v-card
              class="category-lesson-card mb-4"
              variant="outlined"
              rounded="lg"
              @click="selectCategory(category)"
            >
              <v-img
                :src="getImagePath(category.name)"
                height="180"
                cover
                class="position-relative"
              >
                <div class="category-overlay d-flex flex-column justify-center align-center">
                  <div class="text-h5 font-weight-bold text-white text-center pb-2">
                    {{ category.meaning }}
                  </div>
                  <div class="text-body-1 text-white">
                    {{ getTopicsCount(category) }} topics
                  </div>
                </div>
              </v-img>
            </v-card>
          </div>
        </div>
      </div>

      <!-- Topic View (when a category is selected) -->
      <div v-else-if="selectedCategory && !selectedTopic" class="topic-section">
        <div class="px-4 mb-3">
          <div class="text-subtitle-1 text-grey text-center">カテゴリー</div>
          <div class="text-h4 font-weight-bold text-center japanese-text">{{ selectedCategory.name }}</div>
          <div class="text-subtitle-1 text-center">{{ selectedCategory.meaning }}</div>
        </div>

        <div class="topics-list px-4">
          <div v-for="topic in getTopicsByCategory(selectedCategory)" :key="topic.id" class="mb-4">
            <v-card
              class="topic-card"
              variant="outlined"
              rounded="lg"
              @click="handleTopicSelect(topic)"
            >
              <v-card-title class="d-flex align-center">
                <span class="japanese-text">{{ topic.name }}</span>
                <v-spacer></v-spacer>
                <v-chip size="small" color="success">{{ getVocabularyCount(topic) }} words</v-chip>
              </v-card-title>
              <v-card-text>
                <div class="text-body-1">{{ topic.meaning }}</div>
              </v-card-text>
            </v-card>
          </div>
        </div>
      </div>

      <!-- Vocabulary List (when a topic is selected) -->
      <div v-else-if="selectedTopic" class="vocabulary-section px-4">
        <div class="d-flex align-center mb-4">
          <v-btn icon class="mr-2" @click="backToTopics" variant="text">
            <v-icon>mdi-arrow-left</v-icon>
          </v-btn>
          <div>
            <div class="text-h6 font-weight-bold japanese-text">{{ selectedTopic.name }}</div>
            <div class="text-subtitle-1">{{ selectedTopic.meaning }}</div>
          </div>
        </div>

        <div v-if="vocabularyItems.length === 0" class="text-center py-8">
          <v-icon size="large" icon="mdi-book-search-outline" class="mb-4"></v-icon>
          <h3 class="text-h6">Không tìm thấy từ vựng</h3>
          <p class="text-body-1 text-medium-emphasis">
            Hãy điều chỉnh bộ lọc tìm kiếm hoặc chọn chủ đề khác.
          </p>
        </div>

        <div v-else>
          <!-- Pagination at the top -->
          <div class="d-flex justify-center mb-4" v-if="totalPages > 1">
            <v-pagination
              v-model="currentPage"
              :length="totalPages"
              :total-visible="5"
              rounded="circle"
              size="small"
              @update:modelValue="handlePageChange"
            ></v-pagination>
          </div>

          <v-card
            v-for="item in vocabularyItems"
            :key="item.vocabId"
            class="vocabulary-item mb-4"
            variant="outlined"
            rounded="lg"
            @click="toggleExpand(item.vocabId)"
          >
            <div class="pa-4">
              <div class="d-flex align-center mb-2">
                <div>
                  <div class="d-flex align-center">
                    <div class="text-h6 japanese-text font-weight-bold">{{ item.term }}</div>
                    <v-chip
                      :color="getJlptColor(item.jlptLevel)"
                      size="small"
                      class="ml-3"
                    >
                      {{ item.jlptLevel }}
                    </v-chip>
                  </div>
                  <div v-if="item.pronunciation" class="text-subtitle-1 japanese-text text-medium-emphasis">
                    {{ item.pronunciation }}
                  </div>
                </div>
                <v-spacer></v-spacer>
                <div class="d-flex">
                  <v-btn
                    icon="mdi-volume-high"
                    size="small"
                    variant="text"
                    @click.stop="playAudio(item)"
                    class="mr-2"
                    color="blue"
                    title="Phát âm"
                  ></v-btn>
                  <v-btn
                    :icon="item.isSaved ? 'mdi-bookmark' : 'mdi-bookmark-outline'"
                    size="small"
                    variant="text"
                    :color="item.isSaved ? 'warning' : undefined"
                    @click.stop="toggleSave(item)"
                    class="mr-2"
                    title="Lưu từ vựng"
                  ></v-btn>
                  <v-btn
                    :icon="expandedItems.includes(item.vocabId) ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                    size="small"
                    variant="text"
                    class="mr-2"
                    title="Xem thêm"
                  ></v-btn>
                </div>
              </div>

              <div class="text-body-1 meaning-text">{{ item.meaning }}</div>

              <!-- Expanded Content -->
              <div v-if="expandedItems.includes(item.vocabId)" class="mt-4">
                <v-divider class="mb-3"></v-divider>

                <!-- Example Section -->
                <div v-if="item.example" class="example-section mb-3 pa-3 rounded">
                  <div class="d-flex align-items-center">
                    <v-icon class="mr-2" size="x-small" color="grey">mdi-format-quote-open</v-icon>
                    <div class="flex-grow-1 japanese-text">{{ item.example }}</div>
                    <v-btn
                      icon="mdi-volume-high"
                      size="x-small"
                      variant="text"
                      @click.stop="playAudio(item, true)"
                      color="blue"
                      title="Phát âm câu ví dụ"
                    ></v-btn>
                  </div>
                  <div v-if="item.exampleMeaning" class="example-translation ml-6 mt-1 text-caption text-medium-emphasis">
                    {{ item.exampleMeaning }}
                  </div>
                </div>

                <!-- View Details Button -->
                <div class="text-center">
                  <v-btn
                    variant="outlined"
                    color="primary"
                    size="small"
                    @click.stop="navigateToDetail(item.vocabId)"
                  >
                    Xem chi tiết
                  </v-btn>
                </div>
              </div>
            </div>
          </v-card>

          <!-- Pagination at the bottom -->
          <div class="d-flex justify-center mt-4" v-if="totalPages > 1">
            <v-pagination
              v-model="currentPage"
              :length="totalPages"
              :total-visible="5"
              rounded="circle"
              size="small"
              @update:modelValue="handlePageChange"
            ></v-pagination>
          </div>
        </div>
      </div>
    </div>

    <!-- Filter Dialog -->
    <v-dialog v-model="showFilterDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h6 font-weight-bold">
          Lọc từ vựng
        </v-card-title>
        <v-card-text>
          <v-select
            v-model="selectedJlptLevel"
            label="Trình độ JLPT"
            :items="jlptLevels"
            clearable
            variant="outlined"
            class="mb-4"
          ></v-select>

          <v-select
            v-model="tempSelectedCategory"
            label="Danh mục"
            :items="categories"
            item-title="meaning"
            item-value="id"
            clearable
            variant="outlined"
            class="mb-4"
            @update:model-value="updateTopicsForCategory"
          ></v-select>

          <v-select
            v-model="tempSelectedTopic"
            label="Chủ đề"
            :items="availableTopics"
            item-title="meaning"
            item-value="id"
            clearable
            variant="outlined"
            :disabled="!tempSelectedCategory"
          ></v-select>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="clearAllFilters">Xóa tất cả</v-btn>
          <v-btn color="primary" @click="applyFilters">Áp dụng</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import vocabularyService from '@/services/vocabulary.service'
import type { VocabularyItem, VocabularyFilter } from '@/services/vocabulary.service'
import { useToast } from 'vue-toast-notification'

// Define interfaces for the data structures
interface Topic {
  id: string
  name: string
  meaning: string
  categoryId: string
  displayOrder: number
  vocabularyCount?: number
}

interface Category {
  id: string
  name: string
  meaning: string
  displayOrder: number
}

const router = useRouter()
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

// Computed
const availableTopics = computed(() => {
  if (!tempSelectedCategory.value) return []
  return topics.value.filter(t => t.categoryId === tempSelectedCategory.value)
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

// Lifecycle hooks
onMounted(async () => {
  await Promise.all([
    fetchCategories(),
    fetchTopics(),
    fetchJlptLevels()
  ])
})

// Watchers
watch([selectedTopic], () => {
  if (selectedTopic.value) {
    // Reset pagination when topic changes
    currentPage.value = 1
    fetchVocabulary()
  }
})

// API Methods
async function fetchCategories() {
  try {
    loading.value = true
    const response = await vocabularyService.getCategories()
    categories.value = response
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
    topics.value = response
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
  return topics.value.filter(t => t.categoryId === category.id).length
}

function getTopicsByCategory(category: Category): Topic[] {
  return topics.value.filter(t => t.categoryId === category.id)
    .sort((a, b) => a.displayOrder - b.displayOrder)
}

function getVocabularyCount(topic: Topic): number | string {
  // This would ideally come from the API, but for now we can return a placeholder
  return topic.vocabularyCount || '?'
}

function selectCategory(category: Category) {
  tempSelectedCategory.value = category.id
  selectedCategory.value = category
  selectedTopic.value = null
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

  // Reset to initial state
  vocabularyItems.value = []
}

function handlePageChange(page: number) {
  currentPage.value = page
  fetchVocabulary()
}

async function playAudio(item: VocabularyItem, isExample = false) {
  const text = isExample ? item.example : item.term
  console.log(`Playing audio for ${isExample ? 'example' : 'term'}: ${text}`)

  // In a real implementation, this would call a TTS service or play an audio file
  // For now, we'll just show a toast
  toast.info(`Playing: ${text}`, {
    position: 'top',
    duration: 2000
  })
}

async function toggleSave(item: VocabularyItem) {
  try {
    loading.value = true

    if (item.isSaved) {
      await vocabularyService.removeSavedVocabulary(item.vocabId)
      toast.success('Removed from saved vocabulary', {
        position: 'top',
        duration: 2000
      })
    } else {
      await vocabularyService.saveVocabulary(item.vocabId)
      toast.success('Added to saved vocabulary', {
        position: 'top',
        duration: 2000
      })
    }

    // Update the local state
    item.isSaved = !item.isSaved
  } catch (error) {
    console.error('Error toggling save status:', error)
    toast.error('Failed to update saved status', {
      position: 'top',
      duration: 3000
    })
  } finally {
    loading.value = false
  }
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

function navigateToDetail(vocabId: string) {
  router.push({ name: 'vocabulary-detail', params: { id: vocabId } })
}
</script>

<style scoped lang="scss">
.vocabulary-learning-container {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 64px;
}

.japanese-text {
  font-family: 'Noto Sans JP', sans-serif;
}

.search-field {
  background-color: #f0f0f0;
  height: 48px;
  border-radius: 24px;
}

.category-lesson-card {
  overflow: hidden;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1) !important;
  }
}

.category-overlay {
  background: linear-gradient(0deg, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.3) 100%);
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  padding: 20px;
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
</style>