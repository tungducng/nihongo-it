<template>
  <div class="learning-progress-container">
    <!-- Header Section with Profile -->
    <div class="header-section" :style="{ backgroundImage: 'linear-gradient(to bottom, #0a192f, #0a192f)' }">
      <div class="d-flex justify-space-between align-center px-4 pb-4">
        <div>
          <h1 class="text-h4 font-weight-bold white--text mb-2">Báo cáo học tập<br />hàng ngày</h1>
          <v-chip color="rgba(255,255,255,0.1)" class="white--text">{{ currentDate }}</v-chip>
        </div>
      </div>
    </div>

    <!-- Daily Progress Card -->
    <v-card class="progress-card mx-4 mt-n6" rounded="lg">
      <v-card-text class="text-center">
        <h2 class="text-h6 font-weight-medium mb-2">Học trên 30 phút để xem</h2>
        <h2 class="text-h6 font-weight-bold">Tiến bộ hàng ngày</h2>

        <div class="d-flex justify-space-around mt-6">
          <div class="text-center">
            <v-avatar color="grey lighten-3" size="80" class="mb-2">
              <span class="text-h6">-</span>
            </v-avatar>
            <div class="text-body-1 font-weight-bold">Điểm phát âm</div>
          </div>
          <div class="text-center">
            <v-avatar color="grey lighten-3" size="80" class="mb-2">
              <span class="text-h6">-</span>
            </v-avatar>
            <div class="text-body-1 font-weight-bold">Từ trong câu</div>
          </div>
        </div>

        <v-btn
          block
          class="mt-6 py-3 upgrade-btn"
          :style="{ backgroundImage: 'linear-gradient(to right, #f27a54, #a166ab)' }"
          rounded
        >
          <span class="white--text font-weight-bold">Nâng cấp Pro để đạt mục tiêu học</span>
        </v-btn>
      </v-card-text>
    </v-card>

    <!-- Today's Goal -->
    <v-card class="goal-card mx-4 my-4" rounded="lg">
      <v-card-text>
        <div class="text-center">
          <div class="text-subtitle-1 font-weight-medium text-success mb-2">MỤC TIÊU HÔM NAY</div>
          <div class="text-h6 font-weight-bold">Hôm nay tôi đã học được 3 phút</div>
        </div>

        <div class="progress-bar-container mt-4 mb-2">
          <div class="progress-bar-track">
            <div class="progress-indicator" style="left: 10%">
              <v-avatar color="#4cd964" size="36">
                <v-icon color="white">mdi-check</v-icon>
              </v-avatar>
            </div>
            <div class="progress-goal" style="left: 90%">
              <v-avatar color="#ffcc00" size="36">
                <v-icon color="white">mdi-star</v-icon>
              </v-avatar>
            </div>
          </div>
        </div>

        <div class="d-flex justify-space-between time-markers">
          <div class="text-subtitle-1">0'</div>
          <div class="text-subtitle-1">10'</div>
          <div class="text-subtitle-1">20'</div>
          <div class="text-subtitle-1">30'</div>
        </div>

        <div class="day-indicators d-flex justify-space-between mt-4">
          <div v-for="day in ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN']" :key="day" class="day-indicator">
            <v-avatar color="grey lighten-3" size="36">
              <v-icon v-if="day === 'CN'" color="#ffcc00">mdi-check</v-icon>
            </v-avatar>
            <div class="text-subtitle-1 mt-1" :class="{'text-warning font-weight-bold': day === 'CN'}">{{ day }}</div>
          </div>
        </div>
      </v-card-text>
    </v-card>

    <!-- Pronunciation Stats Section -->
    <div class="pronunciation-section mt-4">
      <v-card class="mx-4 mb-4" rounded="lg">
        <v-card-text class="text-center">
          <div class="text-h6 mb-4">
            <span>Tôi đã </span>
            <span class="text-warning">mở miệng nói tiếng Anh</span>
            <span> được</span>
          </div>

          <div class="text-h2 font-weight-bold text-warning">2</div>
          <div class="text-subtitle-1 mb-4">LẦN</div>

          <div class="text-h6 font-weight-bold mb-2">Tình hình học ngữ âm của tôi</div>

          <div class="d-flex justify-space-around mt-4">
            <div class="progress-circle">
              <v-avatar color="#4cd964" size="70" class="white--text">
                <div>
                  <div class="text-h4 font-weight-bold">100%</div>
                  <v-icon small color="white">mdi-arrow-up</v-icon>
                </div>
              </v-avatar>
              <div class="mt-2">/əʊ/</div>
            </div>

            <div class="progress-circle">
              <v-avatar color="#4cd964" size="70" class="white--text">
                <div>
                  <div class="text-h4 font-weight-bold">97%</div>
                  <v-icon small color="white">mdi-arrow-up</v-icon>
                </div>
              </v-avatar>
              <div class="mt-2">/ɔː/</div>
            </div>

            <div class="progress-circle">
              <v-avatar color="#ff3b30" size="70" class="white--text">
                <div>
                  <div class="text-h4 font-weight-bold">15%</div>
                  <v-icon small color="white">mdi-arrow-down</v-icon>
                </div>
              </v-avatar>
              <div class="mt-2">/g/</div>
            </div>
          </div>

          <v-btn
            block
            class="mt-6 py-3 upgrade-btn"
            :style="{ backgroundImage: 'linear-gradient(to right, #a166ab, #f27a54)' }"
            rounded
          >
            <span class="white--text font-weight-bold">Nâng cấp Pro để luyện phát âm</span>
          </v-btn>
        </v-card-text>
      </v-card>

      <!-- Learning Progress Stats -->
      <div class="d-flex mx-4 mb-4">
        <v-card class="flex-grow-1 mr-2" rounded="lg">
          <v-card-text>
            <div>
              <span>Tôi </span>
              <span class="text-info font-weight-bold">sắp nói được</span>
            </div>
            <div>
              <span class="text-info font-weight-bold">Tiếng Anh</span>
              <span> vì đã mở miệng được</span>
            </div>
            <div class="text-h4 text-info font-weight-bold mt-2">1/100 giờ</div>
            <div class="progress-wave mt-4" style="background-color: rgba(0, 200, 180, 0.1);"></div>
          </v-card-text>
        </v-card>

        <v-card class="flex-grow-1 ml-2" rounded="lg">
          <v-card-text>
            <div>
              <span>Tôi đã </span>
              <span class="text-warning font-weight-bold">vượt qua</span>
            </div>
            <div>
              <span class="text-warning font-weight-bold">cơn lười</span>
              <span> của mình được</span>
            </div>
            <div class="text-h4 text-warning font-weight-bold mt-2">1 ngày</div>
            <div class="progress-wave mt-4" style="background-color: rgba(255, 204, 0, 0.1);"></div>
          </v-card-text>
        </v-card>
      </div>

      <!-- Vocabulary Learning Card -->
      <v-card class="mx-4 mb-4" rounded="lg">
        <v-card-text>
          <div class="d-flex justify-space-between align-center">
            <div class="d-flex align-center">
              <v-avatar color="#ffcc00" class="mr-3">
                <v-icon color="white">mdi-book</v-icon>
              </v-avatar>
              <span class="text-h6 font-weight-bold">Từ & cụm</span>
            </div>
            <span class="text-info cursor-pointer" @click="navigateToVocabularyStorage">Xem chi tiết</span>
          </div>

          <div class="text-center mt-6">
            <v-avatar size="140" color="white" class="vocab-progress-circle">
              <v-avatar size="110" color="#fff3e0">
                <div>
                  <div class="text-h4 text-grey-darken-1">1</div>
                  <div class="text-caption text-grey-darken-1">từ</div>
                </div>
              </v-avatar>
            </v-avatar>
            <div class="text-center mt-2">
              <div class="text-body-2">thu nạp</div>
              <div class="text-h5 font-weight-bold">100%</div>
            </div>
          </div>

          <div class="vocab-stats d-flex justify-space-around mt-4">
            <div class="text-caption">
              <span class="vocab-indicator new"></span> mới học (1 từ)
            </div>
            <div class="text-caption">
              <span class="vocab-indicator reviewing"></span> mới ôn (0 từ)
            </div>
          </div>

          <div class="vocab-stats d-flex justify-space-around mt-2">
            <div class="text-caption">
              <span class="vocab-indicator familiar"></span> gần nhớ (0 từ)
            </div>
            <div class="text-caption">
              <span class="vocab-indicator mastered"></span> đã nhớ (0 từ)
            </div>
          </div>
        </v-card-text>
      </v-card>

      <!-- Speaking Test Button -->
      <v-card class="mx-4 mb-4" rounded="lg" @click="checkSpeakingLevel">
        <v-card-text>
          <div class="d-flex justify-space-between align-center">
            <div class="d-flex align-center">
              <v-avatar color="#f27a54" class="mr-3">
                <v-icon color="white">mdi-microphone</v-icon>
              </v-avatar>
              <span class="text-h6 font-weight-bold">Kiểm tra trình độ nói</span>
            </div>
            <v-icon>mdi-chevron-right</v-icon>
          </div>
        </v-card-text>
      </v-card>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const currentTime = ref('21:18')
const currentDate = ref('20/04/2025')

// Method to format date
function formatDate() {
  const date = new Date()
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  currentDate.value = `${day}/${month}/${year}`
}

// Method to update time
function updateTime() {
  const date = new Date()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  currentTime.value = `${hours}:${minutes}`
}

// Method to check speaking level
function checkSpeakingLevel() {
  // Navigate to speaking test page or open dialog
  router.push('/speaking-test')
}

// Method to navigate to vocabulary storage
function navigateToVocabularyStorage() {
  router.push('/learning/vocabulary-storage')
}

// Lifecycle hook
onMounted(() => {
  updateTime()
  formatDate()

  // Update time every minute
  setInterval(updateTime, 60000)
})
</script>

<style scoped lang="scss">
.learning-progress-container {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 64px;
}

.header-section {
  padding-top: 8px;
  padding-bottom: 80px;
  border-bottom-left-radius: 24px;
  border-bottom-right-radius: 24px;
}

.avatar-border {
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.progress-card, .goal-card {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.upgrade-btn {
  letter-spacing: 0.5px;
  text-transform: none;
  height: 48px;
}

.progress-bar-container {
  position: relative;
  margin: 40px 0 20px;
}

.progress-bar-track {
  position: relative;
  height: 4px;
  background-color: #e0e0e0;
  border-radius: 2px;
}

.progress-indicator, .progress-goal {
  position: absolute;
  top: -16px;
  transform: translateX(-50%);
}

.day-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.progress-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.vocab-progress-circle {
  box-shadow: 0 0 0 10px rgba(255, 204, 0, 0.1);
}

.vocab-stats {
  color: #757575;
}

.vocab-indicator {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 4px;
}

.vocab-indicator.new {
  background-color: #ffe0b2;
}

.vocab-indicator.reviewing {
  background-color: #ffcc80;
}

.vocab-indicator.familiar {
  background-color: #ffa726;
}

.vocab-indicator.mastered {
  background-color: #f57c00;
}

.progress-wave {
  height: 24px;
  width: 100%;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}

.bottom-navigation {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  z-index: 10;
}

// Status Bar Styling
.status-icons {
  display: flex;
  align-items: center;
}

.battery-status {
  display: flex;
  align-items: center;
}

.time-display {
  color: white;
  font-weight: 500;
}

.cursor-pointer {
  cursor: pointer;
}
</style>
