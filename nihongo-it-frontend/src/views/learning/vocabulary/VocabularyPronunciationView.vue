<template>
  <div class="pronunciation-view-container">
    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
      <span class="mt-4 text-body-1">Loading vocabulary...</span>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <v-alert type="error" class="mb-4">{{ error }}</v-alert>
      <v-btn @click="$router.push({ name: 'vocabulary' })" color="primary" variant="outlined">
        <v-icon start>mdi-arrow-left</v-icon>
        Back to Vocabulary List
      </v-btn>
    </div>

    <!-- Main Content -->
    <template v-else-if="vocabulary">
      <div class="d-flex align-center mb-6">
        <v-btn icon @click="$router.back()" class="mr-3" color="secondary" variant="text">
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>
        <h1 class="text-h5 font-weight-bold">Pronunciation Practice</h1>
      </div>

      <!-- Vocabulary Card -->
      <v-card class="main-vocab-card mb-6" variant="elevated">
        <div class="vocab-header d-flex align-center px-6 py-4">
          <div class="word-container">
            <div class="d-flex align-center mb-1">
              <h2 class="text-h4 japanese-text">
                {{ vocabulary.term || '' }}
              </h2>
              <v-chip :color="getJlptColor(vocabulary.jlptLevel)" class="ml-4">
                {{ vocabulary.jlptLevel }}
              </v-chip>
            </div>
            <div v-if="vocabulary.pronunciation" class="reading-container text-h6 text-medium-emphasis">
              {{ vocabulary.pronunciation }}
            </div>
          </div>
          <v-spacer></v-spacer>
          <div class="action-buttons">
            <v-btn icon class="action-btn mr-2" @click="playAudio" color="primary" variant="text">
              <v-icon>mdi-volume-high</v-icon>
            </v-btn>
          </div>
        </div>

        <v-divider></v-divider>

        <!-- Pronunciation Practice Section -->
        <v-card-text class="py-5">
          <section class="pronunciation-section mb-6">
            <h3 class="section-title d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-microphone</v-icon>
              Pronunciation Practice
              <v-tooltip location="top">
                <template v-slot:activator="{ props }">
                  <v-icon v-bind="props" class="ml-2" size="small">mdi-information</v-icon>
                </template>
                Click the record button and pronounce the vocabulary term to check your pronunciation accuracy.
              </v-tooltip>
            </h3>

            <v-card class="pronunciation-controls-card pa-4 mb-4" color="background">
              <div class="d-flex flex-column align-center">
                <div class="recording-indicator mb-4">
                  <v-btn
                    :loading="isRecording"
                    :color="isRecording ? 'error' : 'primary'"
                    size="x-large"
                    icon
                    @click="toggleRecording"
                  >
                    <v-icon size="32">{{ isRecording ? 'mdi-stop' : 'mdi-microphone' }}</v-icon>
                  </v-btn>
                  <div class="mt-2 text-center text-body-2">
                    {{ isRecording ? 'Recording... Click to stop' : 'Click to start recording' }}
                  </div>
                </div>

                <v-progress-linear
                  v-if="isProcessing"
                  indeterminate
                  color="primary"
                  class="mb-4"
                ></v-progress-linear>

                <v-btn
                  v-if="recordedAudio"
                  color="secondary"
                  class="mb-4"
                  prepend-icon="mdi-play"
                  @click="playRecordedAudio"
                >
                  Play your recording
                </v-btn>
              </div>
            </v-card>

            <!-- Results Section -->
            <div v-if="hasScore" class="results-container">
              <h4 class="text-h6 mb-3">Pronunciation Score</h4>

              <v-card class="score-card mb-4" variant="outlined">
                <v-card-text>
                  <div class="d-flex align-center justify-space-between">
                    <div>
                      <div class="text-h4 font-weight-bold" :class="scoreColorClass">
                        {{ pronounciationScore }}%
                      </div>
                      <div class="text-body-2 text-medium-emphasis">Accuracy Score</div>
                    </div>

                    <v-rating
                      :model-value="scoreStars"
                      readonly
                      color="amber"
                      half-increments
                      density="compact"
                    ></v-rating>
                  </div>
                </v-card-text>
              </v-card>

              <v-alert
                v-if="pronunciationFeedback"
                :type="scoreToAlertType"
                variant="tonal"
                class="mb-4"
              >
                {{ pronunciationFeedback }}
              </v-alert>

              <v-btn
                v-if="hasScore"
                color="primary"
                block
                @click="resetRecording"
                prepend-icon="mdi-refresh"
              >
                Try Again
              </v-btn>
            </div>
          </section>

          <!-- Example Section -->
          <section v-if="vocabulary.example" class="example-section mb-6">
            <h3 class="section-title">
              <v-icon color="primary" class="mr-2">mdi-format-quote-open</v-icon>
              Example
            </h3>
            <v-card flat color="background" class="example-card pa-4">
              <p class="japanese-text mb-2">{{ vocabulary.example }}</p>
              <p class="text-body-2 text-medium-emphasis">{{ vocabulary.exampleMeaning }}</p>

              <div class="d-flex justify-end">
                <v-btn icon size="small" @click="playExampleAudio" color="primary" variant="text">
                  <v-icon>mdi-volume-high</v-icon>
                </v-btn>
              </div>
            </v-card>
          </section>
        </v-card-text>
      </v-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toast-notification'
import { useVocabularyStore } from '@/stores'
import axios from 'axios'
import vocabularyService from '@/services/vocabulary.service'

// Define types
interface Vocabulary {
  vocabId: string;
  term: string;
  meaning: string;
  pronunciation?: string;
  example?: string;
  exampleMeaning?: string;
  audioPath?: string;
  jlptLevel?: string;
  topicName?: string;
  categoryName?: string;
}

// Router and stores
const route = useRoute()
const router = useRouter()
const vocabularyStore = useVocabularyStore()
const toast = useToast()

// State
const loading = ref(true)
const error = ref('')
const vocabulary = ref<Vocabulary | null>(null)
const isRecording = ref(false)
const isProcessing = ref(false)
const recordedAudio = ref<string | null>(null)
const mediaRecorder = ref<MediaRecorder | null>(null)
const audioChunks = ref<Blob[]>([])
const pronounciationScore = ref(0)
const pronunciationFeedback = ref('')
const hasScore = ref(false)

// Audio context for recording
let audioContext: AudioContext | null = null
let audioStream: MediaStream | null = null

// Computed
const scoreColorClass = computed(() => {
  const score = pronounciationScore.value
  if (score >= 80) return 'text-success'
  if (score >= 60) return 'text-warning'
  return 'text-error'
})

const scoreStars = computed(() => {
  const score = pronounciationScore.value
  if (score >= 90) return 5
  if (score >= 80) return 4.5
  if (score >= 70) return 4
  if (score >= 60) return 3.5
  if (score >= 50) return 3
  if (score >= 40) return 2.5
  if (score >= 30) return 2
  if (score >= 20) return 1.5
  if (score >= 10) return 1
  return 0.5
})

const scoreToAlertType = computed(() => {
  const score = pronounciationScore.value
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'error'
})

// Methods
const getJlptColor = (level: string | undefined): string => {
  if (!level) return 'grey';
  const colors: Record<string, string> = {
    'N1': 'red',
    'N2': 'orange',
    'N3': 'amber',
    'N4': 'light-green',
    'N5': 'green'
  }
  return colors[level] || 'grey'
}

const playAudio = () => {
  if (!vocabulary.value?.audioPath) {
    toast.error('No audio available for this vocabulary')
    return
  }

  const audio = new Audio(vocabulary.value.audioPath)
  audio.play().catch(err => {
    console.error('Error playing audio:', err)
    toast.error('Failed to play audio')
  })
}

const playExampleAudio = () => {
  // This would normally use a different audio file for the example
  // For now, we'll reuse the vocabulary audio if available
  playAudio()
}

const playRecordedAudio = () => {
  if (recordedAudio.value) {
    const audio = new Audio(recordedAudio.value)
    audio.play().catch(err => {
      console.error('Error playing recorded audio:', err)
      toast.error('Failed to play recorded audio')
    })
  }
}

const toggleRecording = async () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    try {
      await startRecording()
    } catch (err) {
      console.error('Error starting recording:', err)
      toast.error('Failed to access microphone')
    }
  }
}

const startRecording = async () => {
  try {
    audioChunks.value = []
    audioStream = await navigator.mediaDevices.getUserMedia({ audio: true })

    if (audioStream) {
      mediaRecorder.value = new MediaRecorder(audioStream)

      mediaRecorder.value.ondataavailable = (event: BlobEvent) => {
        if (event.data.size > 0) {
          audioChunks.value.push(event.data)
        }
      }

      mediaRecorder.value.onstop = () => {
        const audioBlob = new Blob(audioChunks.value, { type: 'audio/wav' })
        recordedAudio.value = URL.createObjectURL(audioBlob)
        processRecording(audioBlob)
      }

      mediaRecorder.value.start()
      isRecording.value = true
    }
  } catch (err) {
    console.error('Error starting recording:', err)
    toast.error('Failed to access microphone')
  }
}

const stopRecording = () => {
  if (mediaRecorder.value && isRecording.value) {
    mediaRecorder.value.stop()
    isRecording.value = false

    // Stop all audio tracks
    if (audioStream) {
      audioStream.getTracks().forEach(track => track.stop())
    }
  }
}

const processRecording = async (audioBlob: Blob) => {
  isProcessing.value = true

  try {
    // Create FormData and append the audio blob
    const formData = new FormData()
    formData.append('audio', audioBlob, 'recording.wav')

    if (vocabulary.value?.term) {
      formData.append('text', vocabulary.value.term)
    }

    formData.append('language', 'ja-JP')

    // In a real implementation, you would send this to your backend
    // const response = await axios.post('/api/pronunciation/evaluate', formData)

    // Simulate API response with mock data
    await new Promise(resolve => setTimeout(resolve, 1500))

    // Mock response data
    const mockScore = Math.floor(Math.random() * 40) + 60 // Random score between 60-99
    pronounciationScore.value = mockScore

    if (mockScore >= 90) {
      pronunciationFeedback.value = 'Excellent pronunciation! Your accent is very natural.'
    } else if (mockScore >= 80) {
      pronunciationFeedback.value = 'Very good pronunciation. Minor accent adjustments would make it perfect.'
    } else if (mockScore >= 70) {
      pronunciationFeedback.value = 'Good pronunciation. Try focusing on the pitch accent and rhythm.'
    } else if (mockScore >= 60) {
      pronunciationFeedback.value = 'Your pronunciation is understandable but needs improvement on tone and stress.'
    } else {
      pronunciationFeedback.value = 'Keep practicing! Focus on listening to native speakers and try to match their pronunciation.'
    }

    hasScore.value = true
  } catch (err) {
    console.error('Error processing recording:', err)
    toast.error('Failed to process pronunciation')
  } finally {
    isProcessing.value = false
  }
}

const resetRecording = () => {
  recordedAudio.value = null
  pronounciationScore.value = 0
  pronunciationFeedback.value = ''
  hasScore.value = false

  // Clear any existing media resources
  if (audioStream) {
    audioStream.getTracks().forEach(track => track.stop())
  }
}

// Lifecycle hooks
onMounted(async () => {
  const term = route.params.term
  if (term && typeof term === 'string') {
    loading.value = true
    error.value = ''

    try {
      const response = await vocabularyService.getVocabularyByTerm(term)
      vocabulary.value = response
    } catch (err) {
      console.error('Error loading vocabulary:', err)
      error.value = 'Failed to load vocabulary information'
    } finally {
      loading.value = false
    }
  } else {
    error.value = 'No vocabulary term provided'
    loading.value = false
  }
})

onUnmounted(() => {
  // Clean up resources when component is destroyed
  if (isRecording.value) {
    stopRecording()
  }

  if (recordedAudio.value) {
    URL.revokeObjectURL(recordedAudio.value)
  }
})
</script>

<style scoped lang="scss">
.pronunciation-view-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;

  .loading-container,
  .error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 60vh;
  }

  .japanese-text {
    font-family: 'Noto Sans JP', sans-serif;
  }

  .section-title {
    font-weight: 600;
    margin-bottom: 16px;
    color: var(--v-primary-base);
  }

  .recording-indicator {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .score-card {
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
    }
  }

  .pronunciation-controls-card {
    transition: all 0.2s ease;

    &:hover {
      background-color: rgba(var(--v-theme-on-surface), 0.05);
    }
  }

  .text-success {
    color: rgb(var(--v-theme-success));
  }

  .text-warning {
    color: rgb(var(--v-theme-warning));
  }

  .text-error {
    color: rgb(var(--v-theme-error));
  }

  .example-card {
    border-radius: 8px;
    transition: all 0.2s ease;

    &:hover {
      background-color: rgba(var(--v-theme-on-surface), 0.05);
    }
  }
}
</style>
