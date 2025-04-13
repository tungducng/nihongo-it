<template>
  <div class="vocabulary-detail-container">
    <!-- Loading State -->
    <div v-if="vocabularyStore.loading" class="loading-container">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
      <span class="mt-4 text-body-1">Loading vocabulary...</span>
    </div>

    <!-- Error State -->
    <div v-else-if="vocabularyStore.error" class="error-container">
      <v-alert type="error" class="mb-4">{{ vocabularyStore.error }}</v-alert>
      <v-btn @click="$router.push({ name: 'vocabulary' })" color="primary" variant="outlined">
        <v-icon start>mdi-arrow-left</v-icon>
        Back to Vocabulary List
      </v-btn>
    </div>

    <!-- Main Content -->
    <template v-else-if="vocabularyStore.hasVocabulary">
      <div class="d-flex align-center mb-6">
        <v-btn icon @click="$router.push({ name: 'vocabulary' })" class="mr-3" color="secondary" variant="text">
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>
        <h1 class="text-h5 font-weight-bold">Vocabulary Details</h1>
      </div>

      <!-- Main Vocabulary Card -->
      <v-card class="main-vocab-card mb-6" variant="elevated">
        <!-- Header Section -->
        <div class="vocab-header d-flex align-center px-6 py-4">
          <div class="word-container">
            <div class="d-flex align-center mb-1">
              <h2 class="text-h4 japanese-text">{{ vocabulary.hiragana }}</h2>
              <v-chip :color="getJlptColor(vocabulary.jlptLevel)" class="ml-4">
                {{ vocabulary.jlptLevel }}
              </v-chip>
            </div>
            <div class="reading-container text-h6 text-medium-emphasis">{{ vocabulary.kanji }}</div>
          </div>
          <v-spacer></v-spacer>
          <div class="action-buttons">
            <v-btn icon class="action-btn mr-2" @click="playAudio" color="primary" variant="text">
              <v-icon>mdi-volume-high</v-icon>
            </v-btn>
            <v-btn
              icon
              class="action-btn"
              @click="vocabularyStore.toggleFavorite"
              :color="vocabularyStore.isFavorite ? 'amber' : 'secondary'"
              variant="text"
            >
              <v-icon>mdi-star</v-icon>
            </v-btn>
          </div>
        </div>

        <v-divider></v-divider>

        <!-- Content Section -->
        <v-card-text class="py-5">
          <!-- Meaning Section -->
          <section class="meaning-section mb-6">
            <h3 class="section-title">
              <v-icon color="primary" class="mr-2">mdi-translate</v-icon>
              Meaning
            </h3>
            <p class="section-content text-body-1">{{ vocabulary.meaning }}</p>
          </section>

          <!-- Example Section -->
          <section v-if="vocabulary.exampleSentence" class="example-section mb-6">
            <h3 class="section-title">
              <v-icon color="primary" class="mr-2">mdi-format-quote-open</v-icon>
              Example
            </h3>
            <v-card flat color="background" class="example-card pa-4">
              <p class="japanese-text mb-2" v-html="vocabularyStore.processedExample"></p>
              <p class="text-body-2 text-medium-emphasis">{{ vocabulary.exampleSentenceTranslation }}</p>

              <div class="d-flex justify-end">
                <v-btn icon size="small" @click="playExampleAudio" color="primary" variant="text">
                  <v-icon>mdi-volume-high</v-icon>
                </v-btn>
              </div>
            </v-card>
          </section>

          <!-- Category and Notes Section -->
          <div class="d-flex flex-wrap">
            <section class="category-section me-6 mb-6" style="min-width: 200px">
              <h3 class="section-title">
                <v-icon color="primary" class="mr-2">mdi-tag</v-icon>
                Category
              </h3>
              <v-chip color="success" variant="outlined">{{ vocabulary.category || 'Uncategorized' }}</v-chip>
            </section>

            <section v-if="vocabulary.notes" class="notes-section mb-6 flex-grow-1">
              <h3 class="section-title">
                <v-icon color="primary" class="mr-2">mdi-note-text</v-icon>
                Notes
              </h3>
              <p class="section-content text-body-2">{{ vocabulary.notes }}</p>
            </section>
          </div>

          <!-- Additional Info -->
          <section class="additional-info-section mt-4">
            <div class="d-flex flex-wrap text-caption text-medium-emphasis">
              <div class="me-4 mb-2">
                <span class="font-weight-medium">Created by:</span> {{ vocabulary.createdBy }}
              </div>
              <div class="me-4 mb-2">
                <span class="font-weight-medium">Created on:</span> {{ formatDate(vocabulary.createdAt) }}
              </div>
            </div>
          </section>
        </v-card-text>

        <v-divider></v-divider>

        <!-- Actions Section -->
        <v-card-actions class="pa-4">
          <v-btn @click="startPractice" color="primary" prepend-icon="mdi-book-open-variant">
            Practice
          </v-btn>
          <v-btn @click="createFlashcard" color="secondary" prepend-icon="mdi-card-text">
            Create Flashcard
          </v-btn>
          <v-spacer></v-spacer>
          <v-btn v-if="isUserAdmin" @click="editVocabulary" color="info" prepend-icon="mdi-pencil">
            Edit
          </v-btn>
        </v-card-actions>
      </v-card>

      <!-- Related Vocabulary Section -->
      <v-card v-if="vocabularyStore.hasRelatedItems" variant="outlined" class="related-vocab-card">
        <v-card-title class="related-title pb-1 pt-4 px-6">
          <v-icon color="primary" class="mr-2">mdi-link-variant</v-icon>
          Related Vocabulary
        </v-card-title>
        <v-card-subtitle class="text-caption px-6 pb-2">
          Other vocabulary terms in the same category or JLPT level
        </v-card-subtitle>

        <v-card-text class="px-6 py-4">
          <div class="related-items-container">
            <v-hover v-for="item in vocabularyStore.relatedVocabulary" :key="item.vocabId" v-slot="{ isHovering, props }">
              <v-card
                flat
                v-bind="props"
                :elevation="isHovering ? 4 : 0"
                @click="goToVocabularyDetail(item.vocabId)"
                class="related-item pa-4 mb-2"
                :class="{ 'on-hover': isHovering }"
              >
                <div class="d-flex align-center">
                  <div>
                    <div class="font-weight-bold text-subtitle-1">{{ item.hiragana }}</div>
                    <div class="text-caption text-medium-emphasis mb-1">{{ item.meaning }}</div>
                    <div v-if="item.kanji" class="text-caption japanese-text">{{ item.kanji }}</div>
                  </div>
                  <v-spacer></v-spacer>
                  <v-chip size="small" :color="getJlptColor(item.jlptLevel)">{{ item.jlptLevel }}</v-chip>
                </div>
              </v-card>
            </v-hover>
          </div>
        </v-card-text>
      </v-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toast-notification'
import { useVocabularyStore, useAuthStore } from '@/stores'
import axios from 'axios'

// Router
const route = useRoute()
const router = useRouter()

// Stores
const vocabularyStore = useVocabularyStore()
const authStore = useAuthStore()

// Computed
const isUserAdmin = computed(() => {
  const userRoleId = authStore.user?.roleId
  return userRoleId === 1 // Assuming roleId 1 is ADMIN
})
const vocabulary = computed(() => vocabularyStore.currentVocabulary!)

// Watch for route change
watch(
  () => route.params.id,
  async (newId) => {
    if (newId) {
      await loadVocabulary(newId as string)
    }
  }
)

// Lifecycle hooks
onMounted(async () => {
  vocabularyStore.reset()
  const vocabId = route.params.id as string
  if (vocabId) {
    await loadVocabulary(vocabId)
  }
})

// Methods
async function loadVocabulary(id: string) {
  const vocab = await vocabularyStore.fetchVocabularyById(id)
  if (vocab?.exampleSentence) {
    try {
      // Use a simple implementation for furigana if module is not available
      const processedText = vocab.exampleSentence
      vocabularyStore.setProcessedExample(processedText)
    } catch (error) {
      console.error('Error processing example sentence:', error)
      vocabularyStore.setProcessedExample(vocab.exampleSentence) // Use original text as fallback
    }
  }
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

function formatDate(dateString?: string): string {
  if (!dateString) return 'Unknown'

  try {
    const date = new Date(dateString)
    return date.toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch (e) {
    return dateString
  }
}

function goToVocabularyDetail(id: string) {
  router.push({ name: 'vocabularyDetail', params: { id } })
}

async function playAudio() {
  if (!vocabulary.value) return
  await playAudioHelper(vocabulary.value.audioPath, false)
}

async function playExampleAudio() {
  if (!vocabulary.value?.exampleSentence) return
  await playAudioHelper(null, true)
}

async function playAudioHelper(audioPath: string | null | undefined, isExampleSentence: boolean = false) {
  const toast = useToast()

  if (audioPath) {
    // If there's an existing audio path, use it
    try {
      const audio = new Audio(audioPath)
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

  // Verify authentication before proceeding
  if (!authStore.isAuthenticated) {
    toast.error('Please log in to use text-to-speech', {
      position: 'top',
      duration: 4000
    })
    setTimeout(() => {
      router.push({
        name: 'login',
        query: { redirect: route.fullPath }
      })
    }, 1500)
    return
  }

  // No audio path available, use TTS API
  try {
    // Determine which text to use for speech
    let textToSpeak = ''

    if (isExampleSentence && vocabulary.value.exampleSentence) {
      textToSpeak = vocabulary.value.exampleSentence
    } else {
      // Use vocabulary word based on priority: katakana -> kanji -> hiragana
      if (vocabulary.value.katakana) {
        textToSpeak = vocabulary.value.katakana
      } else if (vocabulary.value.kanji) {
        textToSpeak = vocabulary.value.kanji
      } else if (vocabulary.value.hiragana) {
        textToSpeak = vocabulary.value.hiragana
      } else {
        toast.warning('No Japanese text available for this vocabulary', {
          position: 'top',
          duration: 3000
        })
        return
      }
    }

    // Show loading indicator
    toast.info('Generating audio...', {
      position: 'top',
      duration: 2000
    })

    // Get the backend API URL
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

    // Set speed: 0.9 for vocabulary, 1.0 for example sentences
    const speed = isExampleSentence ? 1.0 : 0.9

    // Get auth token from localStorage directly as a fallback
    const authToken = localStorage.getItem('auth_token')

    // Call the TTS API with Authorization header
    const response = await axios.post(`${apiUrl}/api/v1/tts/generate`, textToSpeak, {
      headers: {
        'Content-Type': 'text/plain; charset=UTF-8',
        'Accept-Language': 'ja-JP',
        'X-Speech-Speed': speed.toString(),
        'X-Content-Language': 'ja',
        'X-Content-Is-Example': isExampleSentence.toString(),
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
    await audio.play()

    // Clean up the object URL after playing
    audio.onended = () => {
      URL.revokeObjectURL(audioUrl)
    }
  } catch (error) {
    console.error('Error generating or playing TTS audio:', error)

    // Special handling for 401 errors
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      toast.error('TTS service requires authentication. Please log in again.', {
        position: 'top',
        duration: 3000
      })
    } else {
      toast.error(error instanceof Error ? error.message : 'Failed to generate speech', {
        position: 'top',
        duration: 3000
      })
    }
  }
}

function startPractice() {
  router.push({
    name: 'practice',
    query: { vocabularyId: vocabulary.value.vocabId }
  })
}

function editVocabulary() {
  router.push(`/vocabulary/edit/${vocabulary.value.vocabId}`)
}

async function createFlashcard() {
  const toast = useToast()
  try {
    // Get the backend API URL
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

    // Get auth token from localStorage directly
    const authToken = localStorage.getItem('auth_token')

    // Create flashcard from vocabulary
    await axios.post(`${apiUrl}/api/v1/flashcards/from-vocabulary/${vocabulary.value.vocabId}`, null, {
      headers: {
        'Authorization': `Bearer ${authToken}`,
        'Accept': 'application/json'
      }
    })

    toast.success('Flashcard created successfully!', {
      position: 'top',
      duration: 3000
    })
  } catch (error) {
    console.error('Error creating flashcard:', error)

    // Check if it's already created
    if (axios.isAxiosError(error) && error.response?.status === 400 &&
        error.response?.data?.message?.includes('already exists')) {
      toast.info('A flashcard for this vocabulary already exists', {
        position: 'top',
        duration: 3000
      })
    } else {
      toast.error('Failed to create flashcard', {
        position: 'top',
        duration: 3000
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.vocabulary-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-align: center;
}

.main-vocab-card {
  border-radius: 12px;
  overflow: hidden;
}

.vocab-header {
  background-color: var(--v-surface-variant);
}

.word-container {
  flex-grow: 1;
}

.japanese-text {
  font-family: 'Noto Sans JP', sans-serif;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 12px;
  color: rgba(0, 0, 0, 0.87);
}

.section-content {
  line-height: 1.6;
}

.example-card {
  border-radius: 8px;
  line-height: 1.8;
}

.related-vocab-card {
  border-radius: 12px;
}

.related-item {
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.related-item.on-hover {
  background-color: var(--v-surface-variant);
}

.action-btn {
  opacity: 0.8;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 1;
  }
}
</style>
