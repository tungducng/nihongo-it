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
    <v-dialog v-model="showFlashcard" max-width="500" persistent>
      <v-card class="flashcard-container" @click="flipCard" :class="{ 'flipped': isFlipped }">
        <div class="flashcard-inner">
          <!-- Front Side (Term) -->
          <div class="flashcard-front">
            <v-card-title class="text-center d-block pt-6 pb-2">
              <div class="text-h5 font-weight-bold japanese-text">{{ currentVocab?.term || '' }}</div>
              <v-chip
                v-if="currentVocab?.jlptLevel"
                size="small"
                color="primary"
                class="my-2"
              >
                {{ currentVocab.jlptLevel }}
              </v-chip>
            </v-card-title>

            <v-card-text class="text-center pb-6">
              <div class="text-body-1 text-medium-emphasis">Click to view meaning</div>
              <div class="text-caption text-medium-emphasis mt-4">Or press the audio button below to hear pronunciation</div>
            </v-card-text>
          </div>

          <!-- Back Side (Meaning) -->
          <div class="flashcard-back">
            <v-card-title class="text-center d-block pt-6 pb-2">
              <div class="text-h6 font-weight-bold">{{ currentVocab?.meaning || '' }}</div>
            </v-card-title>

            <v-card-text class="text-center">
              <div v-if="currentVocab?.example" class="example-section pa-3 rounded-lg mb-3">
                <div class="japanese-text mb-1">{{ currentVocab.example }}</div>
                <div class="text-caption text-medium-emphasis">{{ currentVocab.exampleMeaning }}</div>
              </div>
              <div class="text-body-1 text-medium-emphasis">Click to return to term</div>
            </v-card-text>
          </div>
        </div>

        <v-divider></v-divider>

        <!-- Flashcard Controls -->
        <v-card-actions class="d-flex flex-column pa-4">
          <div class="d-flex justify-center align-center mb-3 w-100">
            <v-btn
              icon="mdi-volume-high"
              variant="text"
              color="primary"
              class="mx-2"
              @click.stop="currentVocab && playAudio(currentVocab)"
              :loading="playingAudioId === currentVocab?.vocabId"
              :disabled="!currentVocab"
            ></v-btn>
            <v-spacer></v-spacer>
            <v-btn
              icon="mdi-close"
              variant="text"
              color="grey"
              class="mx-2"
              @click.stop="closeFlashcard"
            ></v-btn>
          </div>

          <div class="d-flex justify-space-between w-100">
            <v-btn
              color="error"
              variant="outlined"
              class="rating-btn"
              @click.stop="rateCard('again')"
            >
              Again
            </v-btn>
            <v-btn
              color="warning"
              variant="outlined"
              class="rating-btn"
              @click.stop="rateCard('hard')"
            >
              Hard
            </v-btn>
            <v-btn
              color="success"
              variant="outlined"
              class="rating-btn"
              @click.stop="rateCard('good')"
            >
              Good
            </v-btn>
            <v-btn
              color="primary"
              variant="outlined"
              class="rating-btn"
              @click.stop="rateCard('easy')"
            >
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

function rateCard(rating: 'again' | 'hard' | 'good' | 'easy') {
  // In a real implementation, you would store the rating and update the learning algorithm
  // For now, we'll just show a toast message
  const ratingMessages = {
    again: 'Bạn sẽ gặp lại từ này sớm',
    hard: 'Từ này khó, sẽ lặp lại sau',
    good: 'Tốt! Bạn đã nhớ được từ này',
    easy: 'Rất tốt! Bạn đã nắm vững từ này'
  }

  toast.success(ratingMessages[rating], {
    position: 'top',
    duration: 2000
  })

  // Close the flashcard after rating
  closeFlashcard()
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
  height: 350px;
  cursor: pointer;
}

.flashcard-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.6s;
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
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.flashcard-back {
  transform: rotateY(180deg);
}

.example-section {
  background-color: #f5f5f5;
  border-left: 3px solid rgba(0, 0, 0, 0.1);
}

.japanese-text {
  font-family: 'Noto Sans JP', sans-serif;
}

.rating-btn {
  flex: 1;
  margin: 0 4px;
  text-transform: none;
}
</style>
