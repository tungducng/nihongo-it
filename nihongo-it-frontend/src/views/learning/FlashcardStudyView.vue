<template>
  <div class="flashcard-study-container">
    <!-- App Bar -->
    <v-app-bar flat color="white">
      <v-app-bar-nav-icon @click="goBack">
        <v-icon>mdi-arrow-left</v-icon>
      </v-app-bar-nav-icon>
      <v-app-bar-title>Học Thẻ Ghi Nhớ</v-app-bar-title>
      <v-spacer></v-spacer>
      <v-btn icon @click="showStats = true">
        <v-icon>mdi-chart-box</v-icon>
      </v-btn>
    </v-app-bar>

    <!-- Loading State -->
    <div v-if="loading" class="d-flex justify-center align-center" style="height: 80vh">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>

    <!-- No Cards State -->
    <v-container v-else-if="dueCards.length === 0 && !loading" class="text-center py-12">
      <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-check-circle</v-icon>
      <h2 class="text-h5 mb-2">Chúc Mừng!</h2>
      <p class="text-body-1 text-medium-emphasis mb-6">
        Bạn đã hoàn thành tất cả các thẻ ghi nhớ cần ôn tập hôm nay.
      </p>
      <p class="text-body-2 mb-6">
        Hãy quay lại vào ngày mai hoặc thêm thẻ ghi nhớ mới từ danh sách từ vựng của bạn.
      </p>
      <div class="d-flex justify-center">
        <v-btn
          color="primary"
          variant="outlined"
          class="mr-2"
          @click="goToVocabulary"
        >
          Khám Phá Từ Vựng
        </v-btn>
        <v-btn
          color="primary"
          @click="refreshDueCards"
        >
          Tải Lại
        </v-btn>
      </div>
    </v-container>

    <!-- Study Cards -->
    <v-container v-else-if="currentCard" class="py-8">
      <div class="mb-4 d-flex align-center">
        <div class="text-subtitle-2 text-medium-emphasis">
          {{ remainingCount }} thẻ còn lại
        </div>
        <v-spacer></v-spacer>
        <v-chip
          :color="getStateColor(currentCard.state)"
          size="small"
        >
          {{ getStateText(currentCard.state) }}
        </v-chip>
      </div>

      <!-- Flashcard Component - Enhanced Version -->
      <v-card
        class="flashcard-container mx-auto"
        :class="{ 'flipped': isFlipped }"
        elevation="8"
        @click="flipCard"
        height="400px"
      >
        <div class="flashcard-inner">
          <!-- Front of Card -->
          <div class="flashcard-front">
            <div class="flashcard-content">
              <v-chip
                v-if="currentCard.state"
                size="small"
                :color="getStateColor(currentCard.state)"
                class="mb-4 absolute-top-left"
              >
                {{ getStateText(currentCard.state) }}
              </v-chip>

              <div class="text-h3 font-weight-bold japanese-text mb-6">{{ currentCard.frontText }}</div>

              <v-btn
                icon
                size="medium"
                color="primary"
                variant="text"
                @click.stop="playAudio(currentCard)"
                :loading="isPlayingAudio"
                class="mb-6"
              >
                <v-icon>mdi-volume-high</v-icon>
              </v-btn>

              <div class="flip-hint">
                <v-icon icon="mdi-rotate-3d-variant" class="mr-2"></v-icon>
                <span>Nhấn để lật thẻ</span>
              </div>
            </div>
          </div>

          <!-- Back of Card -->
          <div class="flashcard-back">
            <div class="flashcard-content">
              <div class="text-h5 font-weight-bold mb-4">
                {{ getBackTitleText(currentCard.backText) }}
              </div>

              <v-divider class="my-4"></v-divider>

              <div v-if="hasExample(currentCard.backText)" class="example-section pa-4 rounded-lg mb-4">
                <div v-html="getExampleHtml(currentCard.backText)" class="text-body-1"></div>
              </div>

              <div class="term-reminder text-body-2 text-medium-emphasis mt-2">
                <span class="font-weight-medium">Từ vựng:</span> {{ currentCard.frontText }}
              </div>

              <div class="flip-hint">
                <v-icon icon="mdi-rotate-3d-variant" class="mr-2"></v-icon>
                <span>Nhấn để lật thẻ</span>
              </div>
            </div>
          </div>
        </div>
      </v-card>

      <!-- Rating Buttons -->
      <div class="rating-buttons mt-8">
        <div class="d-flex justify-space-between w-100">
          <v-btn
            color="error"
            variant="elevated"
            class="rating-btn"
            @click="rateCard(1)"
            :disabled="!isFlipped"
            elevation="2"
          >
            <v-icon icon="mdi-reload" size="small" class="mr-1"></v-icon>
            Again
          </v-btn>
          <v-btn
            color="warning"
            variant="elevated"
            class="rating-btn"
            @click="rateCard(2)"
            :disabled="!isFlipped"
            elevation="2"
          >
            <v-icon icon="mdi-brain" size="small" class="mr-1"></v-icon>
            Hard
          </v-btn>
          <v-btn
            color="success"
            variant="elevated"
            class="rating-btn"
            @click="rateCard(3)"
            :disabled="!isFlipped"
            elevation="2"
          >
            <v-icon icon="mdi-check" size="small" class="mr-1"></v-icon>
            Good
          </v-btn>
          <v-btn
            color="primary"
            variant="elevated"
            class="rating-btn"
            @click="rateCard(4)"
            :disabled="!isFlipped"
            elevation="2"
          >
            <v-icon icon="mdi-star" size="small" class="mr-1"></v-icon>
            Easy
          </v-btn>
        </div>
      </div>
    </v-container>

    <!-- Statistics Dialog -->
    <v-dialog v-model="showStats" max-width="500">
      <v-card>
        <v-card-title class="text-h5 pt-4 pb-2">
          Thống Kê Học Tập
        </v-card-title>

        <v-card-text v-if="stats">
          <div class="d-flex justify-space-between align-center mb-2">
            <span>Tổng số thẻ:</span>
            <span class="text-subtitle-1 font-weight-bold">{{ stats.totalCards || 0 }}</span>
          </div>
          <div class="d-flex justify-space-between align-center mb-2">
            <span>Thẻ cần ôn hôm nay:</span>
            <span class="text-subtitle-1 font-weight-bold">{{ stats.dueCards || 0 }}</span>
          </div>
          <div class="d-flex justify-space-between align-center mb-2">
            <span>Ôn tập trong 7 ngày qua:</span>
            <span class="text-subtitle-1 font-weight-bold">{{ stats.reviewsLast7Days || 0 }}</span>
          </div>
          <div class="d-flex justify-space-between align-center mb-4">
            <span>Tỉ lệ ghi nhớ:</span>
            <span class="text-subtitle-1 font-weight-bold">{{ formatPercent(stats.retentionRate) }}</span>
          </div>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="text" @click="showStats = false">
            Đóng
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toast-notification'
import flashcardService from '@/services/flashcard.service'
import type { FlashcardDTO } from '@/services/flashcard.service'
import axios from 'axios'
import authService from '@/services/auth.service'

// Router and toast
const router = useRouter()
const toast = useToast()

// State
const loading = ref(true)
const dueCards = ref<FlashcardDTO[]>([])
const currentCardIndex = ref(0)
const isFlipped = ref(false)
const isPlayingAudio = ref(false)
const showStats = ref(false)
const stats = ref<any>(null)

// Computed
const currentCard = computed(() => {
  if (dueCards.value.length > 0 && currentCardIndex.value < dueCards.value.length) {
    return dueCards.value[currentCardIndex.value]
  }
  return null
})

const remainingCount = computed(() => {
  return dueCards.value.length - currentCardIndex.value
})

// Lifecycle hooks
onMounted(async () => {
  await fetchDueCards()
  await fetchStats()
})

// Methods
async function fetchDueCards() {
  loading.value = true
  try {
    dueCards.value = await flashcardService.getDueCards()
    currentCardIndex.value = 0
    isFlipped.value = false
  } catch (error) {
    console.error('Error fetching due cards:', error)
    toast.error('Không thể tải thẻ ghi nhớ. Vui lòng thử lại sau.', {
      position: 'top'
    })
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  try {
    stats.value = await flashcardService.getStudyStatistics()
  } catch (error) {
    console.error('Error fetching study statistics:', error)
  }
}

function formatBackText(text: string) {
  if (!text) return ''
  // Replace newlines with HTML line breaks
  return text.replace(/\n/g, '<br />')
}

// Get the title (first line) from back text
function getBackTitleText(text: string) {
  if (!text) return ''
  return text.split('\n')[0]
}

// Check if the back text contains an example
function hasExample(text: string) {
  if (!text) return false
  return text.includes('Example:')
}

// Format the example part of the back text
function getExampleHtml(text: string) {
  if (!text) return ''

  const lines = text.split('\n')
  const exampleIndex = lines.findIndex(line => line.includes('Example:'))

  if (exampleIndex === -1) return ''

  let result = ''

  // Add the example line and the next line (which should be the translation)
  if (exampleIndex >= 0 && exampleIndex < lines.length) {
    result += `<div class="japanese-text text-h6 mb-2">${lines[exampleIndex].replace('Example: ', '')}</div>`

    if (exampleIndex + 1 < lines.length) {
      result += `<div class="text-body-1 text-medium-emphasis">${lines[exampleIndex + 1]}</div>`
    }
  }

  return result
}

function flipCard() {
  isFlipped.value = !isFlipped.value
}

async function rateCard(rating: number) {
  if (!currentCard.value) return

  try {
    const response = await flashcardService.reviewFlashcard(currentCard.value.id, rating)

    // Show success message based on rating with next review date
    const ratingMessages: Record<number, string> = {
      1: 'Bạn sẽ gặp lại từ này sớm',
      2: 'Từ này khó, sẽ lặp lại sau',
      3: 'Tốt! Bạn đã nhớ được từ này',
      4: 'Rất tốt! Bạn đã nắm vững từ này'
    }

    // Format next review date
    const nextReview = response?.data?.due
      ? new Date(response.data.due).toLocaleDateString('vi-VN')
      : 'soon'

    toast.success(`${ratingMessages[rating]} - Lần ôn tiếp theo: ${nextReview}`, {
      position: 'top',
      duration: 3000
    })

    // Determine next card
    currentCardIndex.value++
    isFlipped.value = false

    // Check if we've reached the end
    if (currentCardIndex.value >= dueCards.value.length) {
      setTimeout(() => {
        toast.success('Chúc mừng! Bạn đã hoàn thành tất cả các thẻ cần ôn tập.', {
          position: 'top',
          duration: 3000
        })
      }, 1000)

      // Refresh stats
      await fetchStats()
    }
  } catch (error) {
    console.error('Error submitting rating:', error)
    toast.error('Không thể lưu đánh giá. Vui lòng thử lại.', {
      position: 'top'
    })
  }
}

function getStateColor(state: string) {
  switch (state) {
    case 'new': return 'info'
    case 'learning': return 'warning'
    case 'review': return 'success'
    case 'relearning': return 'error'
    default: return 'grey'
  }
}

function getStateText(state: string) {
  switch (state) {
    case 'new': return 'Mới'
    case 'learning': return 'Đang học'
    case 'review': return 'Ôn tập'
    case 'relearning': return 'Học lại'
    default: return state
  }
}

async function playAudio(card: FlashcardDTO) {
  if (!card.vocabularyId || isPlayingAudio.value) return

  isPlayingAudio.value = true

  try {
    // Get the audio URL for this vocabulary term
    const response = await axios.get(`/api/v1/speech/vocabulary/${card.vocabularyId}/audio`, {
      headers: {
        Authorization: `Bearer ${authService.getToken()}`
      },
      responseType: 'blob'
    })

    const audioBlob = response.data
    const audioUrl = URL.createObjectURL(audioBlob)
    const audio = new Audio(audioUrl)

    audio.onended = () => {
      isPlayingAudio.value = false
      URL.revokeObjectURL(audioUrl)
    }

    audio.play()
  } catch (error) {
    console.error('Error playing audio:', error)
    isPlayingAudio.value = false
    toast.error('Không thể phát âm thanh', {
      position: 'top'
    })
  }
}

function formatPercent(value: number) {
  if (value === undefined || value === null) return '0%'
  return `${Math.round(value)}%`
}

function refreshDueCards() {
  fetchDueCards()
}

function goToVocabulary() {
  router.push('/vocabulary')
}

function goBack() {
  router.back()
}
</script>

<style scoped lang="scss">
.flashcard-study-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.flashcard-container {
  perspective: 1000px;
  width: 100%;
  max-width: 666px;
  cursor: pointer;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  margin: 0 auto;
}

.flashcard-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.8s;
  transform-style: preserve-3d;
}

.flashcard-container.flipped .flashcard-inner {
  transform: rotateY(180deg);
}

.flashcard-front, .flashcard-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  background: linear-gradient(135deg, #ffffff 0%, #f5f5f5 100%);
}

.flashcard-content {
  padding: 2.5rem;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  position: relative;
}

.flashcard-back {
  transform: rotateY(180deg);
}

.example-section {
  background-color: rgba(0, 0, 0, 0.03);
  border-left: 4px solid #3f51b5;
  border-radius: 10px;
  width: 100%;
  text-align: left;
  transition: all 0.3s ease;
  padding: 1rem;
  margin: 1rem 0;
}

.example-section:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.japanese-text {
  font-family: 'Noto Sans JP', sans-serif;
}

.rating-btn {
  flex: 1;
  margin: 0 6px;
  text-transform: none;
  font-weight: 500;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.12);
  transition: all 0.2s ease;
  height: 44px;
  border-radius: 10px;
}

.rating-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.rating-btn:active {
  transform: translateY(1px);
}

.absolute-top-left {
  position: absolute;
  top: 12px;
  left: 12px;
}

.flip-hint {
  position: absolute;
  bottom: 1rem;
  left: 0;
  right: 0;
  text-align: center;
  color: rgba(0, 0, 0, 0.5);
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rating-buttons {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
