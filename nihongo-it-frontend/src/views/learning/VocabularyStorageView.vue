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

    <!-- Divider with Category Information -->
    <div class="category-divider mt-4 px-4">
      <div class="d-flex justify-space-between align-center">
        <div class="category-label text-subtitle-1 font-weight-medium" :class="activeTabIndex === 0 ? 'text-warning' : 'text-grey'">
          Phim Ảnh
        </div>
        <div class="count-label text-subtitle-1 font-weight-medium" :class="activeTabIndex === 0 ? 'text-warning' : 'text-grey'">
          {{ activeTabIndex === 0 ? '1 câu' : '0 câu' }}
        </div>
      </div>
      <v-divider :color="activeTabIndex === 0 ? '#ffcc00' : '#e0e0e0'" class="mt-1"></v-divider>
    </div>

    <!-- Vocabulary List (Only show when activeTabIndex is 0) -->
    <div v-if="activeTabIndex === 0" class="vocabulary-list px-4 mt-4">
      <div class="vocabulary-item d-flex" @click="playAudio">
        <v-icon size="24" class="mr-4 mt-1">mdi-volume-high</v-icon>
        <div class="vocabulary-content">
          <div class="text-h6 font-weight-bold">musical</div>
          <div class="vocabulary-meaning text-subtitle-1 text-grey">thuộc về âm nhạc</div>
        </div>
      </div>
    </div>

    <!-- Empty State (Show when activeTabIndex is not 0) -->
    <div v-else class="empty-state-container text-center px-4">
      <v-img
        src="https://cdn.iconscout.com/icon/free/png-256/free-box-1439-1156305.png?f=webp"
        max-width="180"
        class="mx-auto my-12 empty-box-image"
      ></v-img>
      <div class="text-h6 text-grey-darken-1 empty-message mb-8">
        Hiện không có từ nào thuộc nhóm này.
      </div>
    </div>

    <!-- Bottom Navigation Placeholder -->
    <div class="bottom-navigation-placeholder" style="height: 56px"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentTime = ref('21:19')
const dataRate = ref('0.96 KB/s')

// Tab configuration
const tabs = [
  { name: 'Mới học', activeColor: '#ffcc00' },
  { name: 'Mới ôn', activeColor: '#ff9800' },
  { name: 'Gần nhớ', activeColor: '#ff5722' },
  { name: 'Đã nhớ', activeColor: '#e64a19' }
]

const activeTabIndex = ref(0)

// Method to go back
function goBack() {
  router.back()
}

// Method to play audio
function playAudio() {
  // Implement audio playback functionality
  console.log('Playing audio for "musical"')
  // In a real app, you would use the Web Audio API or HTML5 Audio to play the pronunciation
}

// Method to update time
function updateTime() {
  const date = new Date()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  currentTime.value = `${hours}:${minutes}`
}

// Update data rate (simulated)
function updateDataRate() {
  const rates = ['0.96 KB/s', '1.32 KB/s', '20.7 KB/s']
  dataRate.value = rates[Math.floor(Math.random() * rates.length)]
}

// Lifecycle hook
onMounted(() => {
  updateTime()
  updateDataRate()

  // Update time every minute
  setInterval(updateTime, 60000)

  // Update data rate occasionally (simulation only, remove in production)
  setInterval(updateDataRate, 10000)
})
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
</style>
