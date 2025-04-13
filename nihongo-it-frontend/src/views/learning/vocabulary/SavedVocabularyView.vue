<template>
  <div class="saved-vocabulary-container">
    <div class="d-flex align-center mb-6">
      <h1 class="text-h5 font-weight-bold">My Saved Vocabulary</h1>
      <v-spacer></v-spacer>
      <v-btn
        variant="text"
        color="primary"
        prepend-icon="mdi-arrow-left"
        :to="{ name: 'vocabulary' }"
      >
        Back to Vocabulary
      </v-btn>
    </div>

    <!-- Loading State -->
    <div v-if="vocabularyStore.savedLoading" class="d-flex justify-center my-8">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>

    <!-- Error State -->
    <v-alert v-else-if="vocabularyStore.error" type="error" class="mb-6">
      {{ vocabularyStore.error }}
    </v-alert>

    <!-- Empty State -->
    <v-card v-else-if="!vocabularyStore.hasSavedItems" class="text-center pa-8 mb-6">
      <v-icon size="large" icon="mdi-bookmark-outline" class="mb-4"></v-icon>
      <h3 class="text-h6">No saved vocabulary</h3>
      <p class="text-body-1 text-medium-emphasis mb-4">
        You haven't saved any vocabulary items yet. Browse the vocabulary list and save the ones you want to study.
      </p>
      <v-btn color="primary" prepend-icon="mdi-book-open-variant" :to="{ name: 'vocabulary' }">
        Browse Vocabulary
      </v-btn>
    </v-card>

    <!-- Content -->
    <template v-else>
      <!-- Filter & Search -->
      <v-card class="mb-6 filter-card elevation-1">
        <v-card-text>
          <v-row align="center">
            <!-- Search Box -->
            <v-col cols="12" sm="6">
              <div class="d-flex">
                <v-text-field
                  v-model="searchQuery"
                  prepend-inner-icon="mdi-magnify"
                  label="Search saved vocabulary"
                  clearable
                  hide-details
                  density="comfortable"
                  variant="outlined"
                  class="mr-2"
                ></v-text-field>
                <v-btn color="primary" @click="searchVocabulary">
                  Search
                </v-btn>
              </div>
            </v-col>

            <!-- Sort Options -->
            <v-col cols="12" sm="6">
              <v-select
                v-model="sortOption"
                label="Sort By"
                :items="sortOptions"
                hide-details
                density="comfortable"
                variant="outlined"
                @update:model-value="loadSavedVocabulary"
              ></v-select>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Saved Vocabulary Cards -->
      <div class="saved-vocabulary-grid">
        <v-card
          v-for="item in vocabularyStore.savedVocabulary"
          :key="item.vocabId"
          class="vocabulary-card"
          elevation="2"
        >
          <v-card-item>
            <v-card-title>
              <div class="d-flex justify-space-between align-center">
                <v-chip :color="getJlptColor(item.jlptLevel)" size="small" class="mr-2">
                  {{ item.jlptLevel }}
                </v-chip>
                <span v-if="item.category" class="category-chip text-caption">
                  {{ item.category }}
                </span>
              </div>

              <div class="d-flex flex-column mt-3">
                <span class="text-h6 japanese-text">{{ item.hiragana }}</span>
                <span v-if="item.kanji" class="text-body-1 japanese-text text-medium-emphasis mb-1">
                  {{ item.kanji }}
                </span>
              </div>
            </v-card-title>

            <v-card-subtitle class="pt-2">
              {{ item.meaning }}
            </v-card-subtitle>
          </v-card-item>

          <v-card-text v-if="item.exampleSentence" class="example-text">
            <p class="text-caption text-medium-emphasis mb-1">Example:</p>
            <p class="japanese-text">{{ item.exampleSentence }}</p>
          </v-card-text>

          <v-divider></v-divider>

          <v-card-actions>
            <v-btn
              variant="text"
              color="primary"
              size="small"
              @click="viewVocabularyDetails(item.vocabId)"
            >
              View Details
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn
              icon
              variant="text"
              color="primary"
              size="small"
              @click.stop="playAudio(item)"
              title="Play audio"
            >
              <v-icon>mdi-volume-high</v-icon>
            </v-btn>
            <v-btn
              icon
              variant="text"
              color="warning"
              size="small"
              @click.stop="removeFromSaved(item.vocabId)"
              title="Remove from saved"
            >
              <v-icon>mdi-bookmark-remove</v-icon>
            </v-btn>
          </v-card-actions>
        </v-card>
      </div>

      <!-- Pagination -->
      <div class="d-flex justify-center mt-6">
        <v-pagination
          v-if="vocabularyStore.totalSavedPages > 1"
          v-model="currentPage"
          :length="vocabularyStore.totalSavedPages"
          :total-visible="7"
          rounded
          @update:model-value="handlePageChange"
        ></v-pagination>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useVocabularyStore } from '@/stores'
import type { VocabularyItem } from '@/services/vocabulary.service'
import { useToast } from 'vue-toast-notification'

// Router
const router = useRouter()

// Store
const vocabularyStore = useVocabularyStore()

// State
const currentPage = ref(1)
const searchQuery = ref('')
const sortOption = ref('date_desc')
const sortOptions = [
  { title: 'Date Added (Newest)', value: 'date_desc' },
  { title: 'Date Added (Oldest)', value: 'date_asc' },
  { title: 'JLPT Level (N5-N1)', value: 'jlpt_asc' },
  { title: 'JLPT Level (N1-N5)', value: 'jlpt_desc' },
  { title: 'Alphabetical (A-Z)', value: 'alpha_asc' },
  { title: 'Alphabetical (Z-A)', value: 'alpha_desc' }
]

// Lifecycle hooks
onMounted(() => {
  vocabularyStore.resetSaved()
  loadSavedVocabulary()
})

// Watch for search query changes
watch(searchQuery, () => {
  // No longer doing automatic search when typing
})

// Methods
async function loadSavedVocabulary() {
  await vocabularyStore.fetchSavedVocabulary(
    currentPage.value - 1,
    25,
    searchQuery.value,
    sortOption.value
  )
}

function searchVocabulary() {
  // Reset to first page when searching
  currentPage.value = 1
  loadSavedVocabulary()
}

function debouncedSearch() {
  // No longer needed but kept for compatibility
  searchVocabulary()
}

function viewVocabularyDetails(vocabId: string) {
  router.push({ name: 'vocabularyDetail', params: { id: vocabId } })
}

async function removeFromSaved(vocabId: string) {
  await vocabularyStore.removeSavedItem(vocabId)

  // If last item on the page was removed, go to previous page
  if (
    vocabularyStore.savedVocabulary.length === 0 &&
    currentPage.value > 1 &&
    currentPage.value > Math.ceil(vocabularyStore.totalSavedItems / 25)
  ) {
    currentPage.value--
    loadSavedVocabulary()
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadSavedVocabulary()
}

async function playAudio(item: VocabularyItem) {
  const toast = useToast()

  if (item.audioPath) {
    try {
      const audio = new Audio(item.audioPath)
      await audio.play()
    } catch (error) {
      console.error('Error playing audio:', error)
      toast.error('Failed to play audio', {
        position: 'top',
        duration: 3000
      })
    }
    return
  }

  // If no audio path available, show message
  toast.info('No audio available for this vocabulary', {
    position: 'top',
    duration: 2000
  })
}

function getJlptColor(level: string): string {
  switch (level) {
    case 'N1': return 'deep-purple'
    case 'N2': return 'indigo'
    case 'N3': return 'blue'
    case 'N4': return 'teal'
    case 'N5': return 'green'
    default: return 'grey'
  }
}
</script>

<style lang="scss" scoped>
.saved-vocabulary-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.filter-card {
  border-radius: 8px;
  background-color: #f9fafc;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.saved-vocabulary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.vocabulary-card {
  border-radius: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
  }
}

.japanese-text {
  font-family: 'Noto Sans JP', sans-serif;
}

.category-chip {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 2px 8px;
  border-radius: 12px;
  color: rgba(0, 0, 0, 0.6);
}

.example-text {
  background-color: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
  padding: 8px;
  margin-top: 8px;
  flex-grow: 1;
}
</style>
