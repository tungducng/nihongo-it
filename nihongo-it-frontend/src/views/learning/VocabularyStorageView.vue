<template>
  <div class="vocabulary-storage-container">
    <!-- Header with Back Button -->
    <div class="header-container d-flex align-center px-4 pb-2">
      <v-btn icon class="mr-2" @click="goBack">
        <v-icon>mdi-chevron-left</v-icon>
      </v-btn>
      <h1 class="text-h6 font-weight-bold">Kho từ của tôi</h1>
    </div>

    <!-- Vocabulary Status Tabs -->
    <div class="vocab-status-tabs px-4 pt-2">
      <div class="d-flex">
        <v-btn
          v-for="(tab, index) in tabs"
          :key="index"
          :color="activeTabIndex === index ? tab.activeColor : 'grey lighten-4'"
          :class="[
            'status-tab',
            'mr-2',
            { 'active-tab': activeTabIndex === index }
          ]"
          @click="activeTabIndex = index"
          :style="activeTabIndex === index ? { color: 'white' } : {}"
          rounded="pill"
          variant="flat"
        >
          {{ tab.name }}
        </v-btn>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center my-8">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
      <p class="text-body-1 text-medium-emphasis mt-3">Đang tải dữ liệu...</p>
    </div>

    <div v-else-if="!loading && errorMessage" class="text-center my-8">
      <v-alert type="error" class="mx-4">
        {{ errorMessage }}
      </v-alert>
    </div>

    <div v-else-if="!loading && savedVocabulary.length === 0" class="empty-state-container text-center px-4">
      <v-img
        src="https://cdn.iconscout.com/icon/free/png-256/free-box-1439-1156305.png?f=webp"
        max-width="180"
        class="mx-auto my-12 empty-box-image"
      ></v-img>
      <div class="text-h6 text-grey-darken-1 empty-message mb-8">
        Hiện không có từ nào đã được lưu.
      </div>
    </div>

    <template v-else>
      <!-- Group vocabulary by topic -->
      <div v-for="(group, topic) in groupedVocabulary" :key="topic" class="mt-4">
        <!-- Divider with Topic Information -->
        <div class="category-divider mt-4 px-4">
          <div class="d-flex justify-space-between align-center">
            <div class="category-label text-subtitle-1 font-weight-medium" :class="activeTabIndex === 0 ? 'text-warning' : 'text-grey'">
              {{ topic }}
            </div>
            <div class="count-label text-subtitle-1 font-weight-medium" :class="activeTabIndex === 0 ? 'text-warning' : 'text-grey'">
              {{ group.length }} từ
            </div>
          </div>
          <v-divider :color="activeTabIndex === 0 ? '#ffcc00' : '#e0e0e0'" class="mt-1"></v-divider>
        </div>

        <!-- Vocabulary List -->
        <div class="vocabulary-list px-4 mt-4">
          <div
            v-for="item in group"
            :key="item.vocabId"
            class="vocabulary-item d-flex"
          >
            <v-icon
              size="24"
              class="mr-4 mt-1"
              :color="playingAudioId === item.vocabId ? 'primary' : 'grey'"
              :loading="playingAudioId === item.vocabId"
              @click.stop="playAudio(item)"
            >
              mdi-volume-high
            </v-icon>
            <div class="vocabulary-content" @click="openFlashcard(item)">
              <div class="text-h6 font-weight-bold">{{ item.term }}</div>
              <div class="vocabulary-meaning text-subtitle-1 text-grey">{{ item.meaning }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="d-flex justify-center my-6" v-if="totalPages > 1">
        <v-pagination
          v-model="currentPage"
          :length="totalPages"
          :total-visible="5"
          rounded="circle"
          @update:model-value="fetchSavedVocabulary"
        ></v-pagination>
      </div>
    </template>

    <!-- Bottom Navigation Placeholder -->
    <div class="bottom-navigation-placeholder" style="height: 56px"></div>

    <!-- Flashcard Dialog -->
    <v-dialog v-model="showFlashcard" max-width="600" height="500" persistent>
      <v-card class="flashcard-container" :class="{ 'flipped': isFlipped }">
        <!-- Close button in absolute top right position -->
        <v-btn
          icon="mdi-close"
          variant="text"
          color="red"
          size="medium"
          class="close-btn"
          @click.stop="closeFlashcard"
        ></v-btn>

        <div class="flashcard-inner" @click="flipCard">
          <!-- Front Side (Term) -->
          <div class="flashcard-front">
            <div class="flashcard-content">
              <v-chip
                v-if="currentVocab?.jlptLevel"
                size="small"
                :color="getJlptLevelColor(currentVocab.jlptLevel)"
                class="mb-4 absolute-top-left"
              >
                {{ currentVocab.jlptLevel }}
              </v-chip>

              <div class="text-h3 font-weight-bold japanese-text mb-6">{{ currentVocab?.term || '' }}</div>

              <div v-if="currentVocab?.pronunciation" class="text-h6 japanese-text text-medium-emphasis mb-6">
                {{ currentVocab.pronunciation }}
              </div>

              <div class="flip-hint">
                <v-icon icon="mdi-rotate-3d-variant" class="mr-2"></v-icon>
                <span>Nhấn để lật thẻ</span>
              </div>
            </div>
          </div>

          <!-- Back Side (Meaning) -->
          <div class="flashcard-back">
            <div class="flashcard-content">
              <v-chip
                v-if="currentVocab?.topicName"
                size="small"
                color="info"
                class="mb-2 absolute-top-left"
              >
                {{ currentVocab.topicName }}
              </v-chip>

              <div class="text-h4 font-weight-bold mb-4">{{ currentVocab?.meaning || '' }}</div>

              <v-divider class="my-4"></v-divider>

              <div v-if="currentVocab?.example" class="example-section pa-4 rounded-lg mb-4">
                <div class="japanese-text text-h6 mb-2">{{ currentVocab.example }}</div>
                <div class="text-body-1 text-medium-emphasis">{{ currentVocab.exampleMeaning }}</div>
              </div>

              <div class="term-reminder text-body-2 text-medium-emphasis mt-2">
                <span class="font-weight-medium">Từ vựng:</span> {{ currentVocab?.term }}
              </div>

              <div class="flip-hint">
                <v-icon icon="mdi-rotate-3d-variant" class="mr-2"></v-icon>
                <span>Nhấn để lật thẻ</span>
              </div>
            </div>
          </div>
        </div>

        <v-divider></v-divider>

        <!-- Flashcard Controls -->
        <v-card-actions class="flashcard-controls pa-4">
          <div class="d-flex justify-space-between align-center w-100 mb-3">
            <v-btn
              icon="mdi-volume-high"
              variant="text"
              color="primary"
              size="large"
              @click.stop="currentVocab && playAudio(currentVocab)"
              :loading="playingAudioId === currentVocab?.vocabId"
              :disabled="!currentVocab"
            ></v-btn>
            <div class="text-body-1 text-medium-emphasis">Đánh giá mức độ ghi nhớ</div>
            <div style="width: 48px"></div> <!-- Spacer to balance the layout -->
          </div>

          <div class="d-flex justify-space-between w-100 rating-buttons">
            <v-btn
              color="error"
              variant="elevated"
              class="rating-btn"
              @click.stop="rateCard('again')"
              elevation="2"
            >
              <v-icon icon="mdi-reload" size="small" class="mr-1"></v-icon>
              Again
            </v-btn>
            <v-btn
              color="warning"
              variant="elevated"
              class="rating-btn"
              @click.stop="rateCard('hard')"
              elevation="2"
            >
              <v-icon icon="mdi-brain" size="small" class="mr-1"></v-icon>
              Hard
            </v-btn>
            <v-btn
              color="success"
              variant="elevated"
              class="rating-btn"
              @click.stop="rateCard('good')"
              elevation="2"
            >
              <v-icon icon="mdi-check" size="small" class="mr-1"></v-icon>
              Good
            </v-btn>
            <v-btn
              color="primary"
              variant="elevated"
              class="rating-btn"
              @click.stop="rateCard('easy')"
              elevation="2"
            >
              <v-icon icon="mdi-star" size="small" class="mr-1"></v-icon>
              Easy
            </v-btn>
          </div>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toast-notification'
import { useVocabularyStore, useAuthStore } from '@/stores'
import axios from 'axios'
import type { VocabularyItem } from '@/services/vocabulary.service'
import authService from '@/services/auth.service'
import flashcardService from '@/services/flashcard.service'

const router = useRouter()
const toast = useToast()
const vocabularyStore = useVocabularyStore()
const authStore = useAuthStore()

// State
const loading = ref(false)
const errorMessage = ref('')
const savedVocabulary = ref<VocabularyItem[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalPages = ref(0)
const totalItems = ref(0)
const playingAudioId = ref<string | null>(null)
const showFlashcard = ref(false)
const isFlipped = ref(false)
const currentVocab = ref<VocabularyItem | null>(null)

// Tab configuration
const tabs = [
  { name: 'Mới học', activeColor: '#ffcc00' },
  { name: 'Mới ôn', activeColor: '#ff9800' },
  { name: 'Gần nhớ', activeColor: '#ff5722' },
  { name: 'Đã nhớ', activeColor: '#e64a19' }
]

const activeTabIndex = ref(0)

// Computed: Group vocabulary by topic
const groupedVocabulary = computed(() => {
  const groups: Record<string, VocabularyItem[]> = {};

  savedVocabulary.value.forEach(item => {
    const topic = item.topicName || 'Chưa phân loại';
    if (!groups[topic]) {
      groups[topic] = [];
    }
    groups[topic].push(item);
  });

  return groups;
});

// Lifecycle hooks
onMounted(async () => {
  await fetchSavedVocabulary();
});

// Watch for tab changes
watch(activeTabIndex, () => {
  // In a real app, you might filter vocabulary based on the active tab
  // For now, we'll just refetch the data
  fetchSavedVocabulary();
});

// Methods
async function fetchSavedVocabulary() {
  loading.value = true;
  errorMessage.value = '';

  try {
    // Use the store to fetch saved vocabulary
    await vocabularyStore.fetchSavedVocabulary(
      currentPage.value - 1,
      pageSize.value,
      '',  // No search filter
      'date_desc'  // Sort by date, newest first
    );

    // Update local state from store
    savedVocabulary.value = vocabularyStore.savedVocabulary;
    totalItems.value = vocabularyStore.totalSavedItems;
    totalPages.value = vocabularyStore.totalSavedPages;

  } catch (error) {
    console.error('Error fetching saved vocabulary:', error);
    errorMessage.value = 'Không thể tải dữ liệu từ vựng đã lưu.';
    toast.error('Không thể tải dữ liệu từ vựng đã lưu.', {
      position: 'top',
      duration: 3000
    });
  } finally {
    loading.value = false;
  }
}

// Method to go back
function goBack() {
  router.back();
}

// Method to play audio
async function playAudio(item: VocabularyItem) {
  // Set the ID of the item we're playing audio for
  playingAudioId.value = item.vocabId;

  try {
    if (item.audioPath) {
      // If there's an existing audio path, use it
      const audio = new Audio(item.audioPath);
      await audio.play();
    } else {
      // Verify authentication before proceeding
      if (!authStore.isAuthenticated) {
        toast.error('Vui lòng đăng nhập để sử dụng tính năng phát âm', {
          position: 'top',
          duration: 3000
        });
        return;
      }

      // Show loading indicator
      toast.info('Đang tạo âm thanh...', {
        position: 'top',
        duration: 2000
      });

      // Get the backend API URL
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

      // Call the TTS API with Authorization header
      const response = await axios.post(`${apiUrl}/api/v1/tts/generate`, item.term, {
        headers: {
          'Content-Type': 'text/plain; charset=UTF-8',
          'Accept-Language': 'ja-JP',
          'X-Speech-Speed': '0.9',
          'X-Content-Language': 'ja',
          'X-Content-Is-Example': 'false',
          'Authorization': `Bearer ${authService.getToken()}`,
          'Accept': 'audio/mpeg'
        },
        responseType: 'arraybuffer'
      });

      // Convert response to blob and create audio URL
      const audioBlob = new Blob([response.data], { type: 'audio/mpeg' });
      const audioUrl = URL.createObjectURL(audioBlob);

      // Play the audio
      const audio = new Audio(audioUrl);
      audio.onended = () => {
        URL.revokeObjectURL(audioUrl);
        playingAudioId.value = null;
      };
      await audio.play();
    }
  } catch (error) {
    console.error('Error generating or playing TTS audio:', error);
    toast.error('Không thể phát âm thanh. Vui lòng thử lại sau.', {
      position: 'top',
      duration: 3000
    });
  } finally {
    // Reset loading state if no audio was played
    setTimeout(() => {
      if (playingAudioId.value === item.vocabId) {
        playingAudioId.value = null;
      }
    }, 3000);
  }
}

function openFlashcard(item: VocabularyItem) {
  currentVocab.value = item
  isFlipped.value = false
  showFlashcard.value = true
}

function closeFlashcard() {
  showFlashcard.value = false
  setTimeout(() => {
    currentVocab.value = null
    isFlipped.value = false
  }, 300)
}

function flipCard() {
  isFlipped.value = !isFlipped.value
}

async function rateCard(rating: 'again' | 'hard' | 'good' | 'easy') {
  if (!currentVocab.value?.vocabId) return;

  // Convert text ratings to numeric values for FSRS (1-4 scale where 1 is Again, 4 is Easy)
  const ratingMap = {
    again: 1, // Hardest rating
    hard: 2,
    good: 3,
    easy: 4   // Easiest rating
  };

  const numericRating = ratingMap[rating];
  loading.value = true;

  try {
    // Get the flashcards for this vocabulary
    const flashcards = await flashcardService.getFlashcardsByVocabulary(currentVocab.value.vocabId);

    if (flashcards.length > 0) {
      // Use the first flashcard found for this vocabulary
      const flashcard = flashcards[0];

      // Submit the rating with the flashcard ID
      const response = await flashcardService.reviewFlashcard(flashcard.id, numericRating);

      // Show success message based on rating
      const ratingMessages = {
        again: 'Bạn sẽ gặp lại từ này sớm',
        hard: 'Từ này khó, sẽ lặp lại sau',
        good: 'Tốt! Bạn đã nhớ được từ này',
        easy: 'Rất tốt! Bạn đã nắm vững từ này'
      };

      // Update next scheduled date in the UI if available
      const nextReview = response?.data?.due ?
        new Date(response.data.due).toLocaleDateString('vi-VN') :
        'soon';

      toast.success(`${ratingMessages[rating]} - Lần ôn tiếp theo: ${nextReview}`, {
        position: 'top',
        duration: 3000
      });

      // Close the flashcard after rating
      closeFlashcard();
    } else {
      toast.warning('Không tìm thấy thẻ ghi nhớ cho từ vựng này', {
        position: 'top',
        duration: 3000
      });
    }
  } catch (error) {
    console.error('Error submitting flashcard rating:', error);
    toast.error('Không thể lưu đánh giá cho thẻ này. Vui lòng thử lại.', {
      position: 'top',
      duration: 3000
    });
  } finally {
    loading.value = false;
  }
}

function getJlptLevelColor(level: string): string {
  switch (level) {
    case 'N1': return 'deep-purple';
    case 'N2': return 'indigo';
    case 'N3': return 'blue';
    case 'N4': return 'teal';
    case 'N5': return 'green';
    default: return 'grey';
  }
}
</script>

<style scoped lang="scss">
.vocabulary-storage-container {
  background-color: white;
  min-height: 100vh;
  padding-top: 8px;
}

.top-bar {
  margin-bottom: 8px;
}

.data-rate {
  font-size: 0.7rem;
}

.status-icons {
  display: flex;
  align-items: center;
}

.battery-status {
  display: flex;
  align-items: center;
}

.time-display {
  font-weight: 500;
}

.status-tab {
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.2s ease;
  border-radius: 50px;
  text-transform: none;
  letter-spacing: 0;

  &.active-tab {
    font-weight: 600;
  }
}

.vocabulary-item {
  margin-bottom: 16px;
  padding: 12px 0;
  cursor: pointer;

  &:hover {
    background-color: rgba(0, 0, 0, 0.02);
  }
}

.vocabulary-meaning {
  margin-top: 4px;
}

.empty-box-image {
  opacity: 0.7;
}

.empty-message {
  color: #757575;
}

.vocabulary-content {
  cursor: pointer;
  flex-grow: 1;
}

.flashcard-container {
  perspective: 1000px;
  height: 500px;
  width: 100%;
  cursor: pointer;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
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

.flashcard-controls {
  background-color: #fafafa;
}

.rating-buttons {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.close-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 10;
}
</style>
