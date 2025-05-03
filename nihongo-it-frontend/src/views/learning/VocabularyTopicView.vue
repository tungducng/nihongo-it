<template>
  <div class="vocabulary-topic-container">
    <!-- Header Section -->
    <div class="header px-4 py-3 d-flex justify-space-between align-center">
      <div class="d-flex align-center">
        <v-btn icon class="mr-2" @click="goBack">
          <v-icon>mdi-chevron-left</v-icon>
        </v-btn>
        <div class="text-h6 font-weight-bold text-dark">
          <span class="text-primary">IT</span>
          <span class="japanese-text">単語学習 - トピック</span>
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
          v-if="topic"
          size="small"
          color="success"
          class="mr-2 mb-1"
        >
          {{ topic.meaning }}
        </v-chip>
      </div>
    </div>

    <!-- Topic Header -->
    <div v-if="topic" class="px-4 mb-3">
      <div class="d-flex align-center mb-4">
        <div>
          <div class="text-h6 font-weight-bold japanese-text">{{ topic.name }}</div>
          <div class="text-subtitle-1">{{ topic.meaning }}</div>
        </div>
      </div>
    </div>

    <!-- Loading Indicator -->
    <div v-if="loading" class="text-center py-8">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
      <p class="text-body-1 text-medium-emphasis mt-3">Loading...</p>
    </div>

    <!-- Vocabulary List for Topic -->
    <div v-else-if="topic" class="vocabulary-section px-4">
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
                  :loading="playingAudioId === item.vocabId"
                  :disabled="playingAudioId !== null && playingAudioId !== item.vocabId || playingExampleAudioId !== null"
                ></v-btn>
                <v-btn
                  :icon="item.isSaved ? 'mdi-bookmark' : 'mdi-bookmark-outline'"
                  size="small"
                  variant="text"
                  :color="item.isSaved ? 'warning' : undefined"
                  @click.stop="toggleSave(item)"
                  class="mr-2"
                  :title="item.isSaved ? 'Bỏ lưu' : 'Lưu từ vựng'"
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
                    :loading="playingExampleAudioId === item.vocabId"
                    :disabled="playingExampleAudioId !== null && playingExampleAudioId !== item.vocabId || playingAudioId !== null"
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
                  @click.stop="navigateToDetail(item.term)"
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

    <!-- Empty State -->
    <div v-else class="text-center py-8">
      <v-icon size="large" icon="mdi-book-search-outline" class="mb-4"></v-icon>
      <h3 class="text-h6">トピックが見つかりません</h3>
      <p class="text-body-1 text-medium-emphasis">
        The requested topic could not be found.
      </p>
      <v-btn color="primary" class="mt-4" @click="goToCategories">
        View All Categories
      </v-btn>
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

const router = useRouter()
const route = useRoute()
const toast = useToast()

// State variables
const loading = ref(true)
const topic = ref<Topic | null>(null)
const vocabularyItems = ref<VocabularyItem[]>([])
const totalItems = ref(0)
const totalPages = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const jlptLevels = ref<string[]>([])
const search = ref('')
const selectedJlptLevel = ref<string | null>(null)
const expandedItems = ref<string[]>([])
const showFilterDialog = ref(false)
const playingAudioId = ref<string | null>(null)
const playingExampleAudioId = ref<string | null>(null)

// Get topic name from route
const topicParam = computed(() => route.params.name as string)

// Computed properties
const hasActiveFilters = computed(() => {
  return selectedJlptLevel.value || search.value
})

// Watchers
watch(topicParam, async (newValue) => {
  if (newValue) {
    console.log(`Topic name changed to: ${newValue}`);
    await fetchTopicDetails();
    fetchVocabulary();
  }
}, { immediate: true });

watch([selectedJlptLevel, search], () => {
  if (topic.value) {
    console.log("Filter criteria changed, fetching vocabulary");
    currentPage.value = 1; // Reset to first page when filters change
    fetchVocabulary();
  }
});

watch(currentPage, () => {
  if (topic.value) {
    console.log(`Page changed to ${currentPage.value}, fetching vocabulary`);
    fetchVocabulary();
  }
});

// Lifecycle hooks
onMounted(async () => {
  // Fetch JLPT levels
  fetchJlptLevels();

  // Note: Topic details and vocabulary will be loaded by the topicParam watcher
})

// Debounce function for search
let searchTimeout: any = null
function debouncedFilterVocabulary() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    fetchVocabulary()
  }, 500)
}

// Methods
async function fetchTopicDetails() {
  try {
    loading.value = true

    // Fetch all topics first
    const topicsResponse = await vocabularyService.getTopics()

    if (Array.isArray(topicsResponse)) {
      // Decode the URL parameter which could be a name or ID
      const paramValue = topicParam.value;
      const decodedParam = decodeURIComponent(paramValue);

      console.log(`Fetching topic details for: ${decodedParam}`);

      // Try finding the topic by ID first (backward compatibility)
      let foundTopic = topicsResponse.find(t => t.id === paramValue || t.topicId === paramValue);

      // If not found by ID, try with decoded name
      if (!foundTopic) {
        foundTopic = topicsResponse.find(t =>
          t.name === decodedParam ||
          encodeURIComponent(t.name) === paramValue
        );
      }

      if (foundTopic) {
        console.log("Topic found:", foundTopic);
        topic.value = {
          id: foundTopic.id || foundTopic.topicId,
          name: foundTopic.name,
          meaning: foundTopic.meaning,
          categoryId: foundTopic.categoryId || foundTopic.category?.id,
          displayOrder: foundTopic.displayOrder || 0,
          vocabularyCount: foundTopic.vocabularyCount || 0
        }
      } else {
        console.error(`Topic not found for parameter: ${paramValue}`);
        topic.value = null
        vocabularyItems.value = []
        toast.error('Topic not found', {
          position: 'top',
          duration: 3000
        })
      }
    }
  } catch (error) {
    console.error('Error fetching topic details:', error)
    toast.error('Failed to load topic details', {
      position: 'top',
      duration: 3000
    })
    topic.value = null
    vocabularyItems.value = []
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

    if (!topic.value) {
      vocabularyItems.value = []
      totalItems.value = 0
      totalPages.value = 0
      return
    }

    // Create filter object
    const filter: VocabularyFilter = {
      keyword: search.value || null,
      jlptLevel: selectedJlptLevel.value,
      topicName: topic.value.name, // Using topic name for filtering
      page: currentPage.value - 1, // API uses 0-based pagination
      size: pageSize.value
    }

    console.log("Fetching vocabulary with filter:", filter);

    const response = await vocabularyService.getVocabulary(filter)

    if (response && Array.isArray(response.content)) {
      vocabularyItems.value = response.content
      totalItems.value = response.totalElements
      totalPages.value = response.totalPages
      console.log(`Loaded ${response.content.length} vocabulary items`);
    } else {
      vocabularyItems.value = []
      totalItems.value = 0
      totalPages.value = 0
      console.log("No vocabulary items found or invalid response format");
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

function toggleExpand(vocabId: string) {
  const index = expandedItems.value.indexOf(vocabId)
  if (index === -1) {
    expandedItems.value.push(vocabId)
  } else {
    expandedItems.value.splice(index, 1)
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  fetchVocabulary()
}

function navigateToDetail(term: string) {
  router.push({
    name: 'vocabularyDetail',
    params: {
      id: topicParam.value,
      term: encodeURIComponent(term)
    }
  });
}

function goBack() {
  router.back()
}

function goToCategories() {
  router.push({ name: 'vocabularyLearning' })
}

function openFilterDialog() {
  showFilterDialog.value = true
}

function applyFilters() {
  showFilterDialog.value = false
  fetchVocabulary()
}

function clearFilter(type: string) {
  if (type === 'jlptLevel') {
    selectedJlptLevel.value = null
  }
  fetchVocabulary()
}

function clearAllFilters() {
  selectedJlptLevel.value = null
  search.value = ''
  fetchVocabulary()
  showFilterDialog.value = false
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
</script>

<style scoped lang="scss">
.vocabulary-topic-container {
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
