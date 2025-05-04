<template>
  <div class="notification-settings-container">
    <div class="header-container d-flex align-center">
      <v-btn icon @click="$router.back()" class="back-btn" size="small">
        <v-icon>mdi-chevron-left</v-icon>
      </v-btn>
      <h1 class="text-subtitle-1 font-weight-bold text-center flex-grow-1">Cài đặt thông báo</h1>
    </div>

    <v-card class="mt-4 mb-6">
      <v-card-title class="text-subtitle-1 pb-0">Thông báo chung</v-card-title>

      <v-card-text>
        <v-list density="compact">
          <v-list-item>
            <template v-slot:prepend>
              <v-icon color="primary" class="mr-2">mdi-bell</v-icon>
            </template>
            <v-list-item-title>Thông báo từ hệ thống</v-list-item-title>
            <template v-slot:append>
              <v-switch
                v-model="preferences.system"
                color="primary"
                hide-details
              ></v-switch>
            </template>
          </v-list-item>

          <v-list-item>
            <template v-slot:prepend>
              <v-icon color="primary" class="mr-2">mdi-bell-ring</v-icon>
            </template>
            <v-list-item-title>Thông báo mục tiêu hàng ngày</v-list-item-title>
            <template v-slot:append>
              <v-switch
                v-model="preferences.dailyGoal"
                color="primary"
                hide-details
              ></v-switch>
            </template>
          </v-list-item>
        </v-list>
      </v-card-text>
    </v-card>

    <!-- Flashcard Reminder Settings -->
    <v-card class="mb-6">
      <v-card-title class="text-subtitle-1 pb-0 d-flex align-center">
        <v-icon color="primary" class="mr-2">mdi-card-text</v-icon>
        Nhắc nhở học thẻ ghi nhớ
      </v-card-title>

      <v-card-text>
        <div class="d-flex justify-space-between align-center py-2">
          <div>
            <div class="text-subtitle-2">Bật nhắc nhở ôn tập</div>
            <div class="text-caption text-medium-emphasis">Nhận thông báo khi có thẻ cần ôn tập</div>
          </div>
          <v-switch
            v-model="preferences.dueCards"
            color="primary"
            hide-details
          ></v-switch>
        </div>

        <v-divider class="my-2"></v-divider>

        <div class="py-2">
          <div class="text-subtitle-2 mb-2">Thời gian nhắc nhở</div>
          <div class="time-selector-container">
            <div class="d-flex align-center">
              <v-select
                v-model="timeHour"
                :items="hours"
                variant="outlined"
                density="compact"
                class="time-select mr-2"
                hide-details
                :disabled="!preferences.dueCards"
                @update:model-value="updateReminderTime"
              ></v-select>
              <span class="mx-1">:</span>
              <v-select
                v-model="timeMinute"
                :items="minutes"
                variant="outlined"
                density="compact"
                class="time-select ml-2"
                hide-details
                :disabled="!preferences.dueCards"
                @update:model-value="updateReminderTime"
              ></v-select>
            </div>
          </div>
          <div class="text-caption text-medium-emphasis mt-2">
              Bạn sẽ nhận được thông báo vào {{ timeHour }}:{{ timeMinute }} hàng ngày nếu có thẻ ghi nhớ cần ôn tập
            </div>
        </div>

        <v-divider class="my-2"></v-divider>

        <div class="py-2">
          <div class="text-subtitle-2 mb-1">Số lượng thẻ tối thiểu</div>
          <div class="text-caption text-medium-emphasis mb-2">Chỉ nhận thông báo khi có nhiều hơn số lượng thẻ này</div>
          <v-slider
            v-model="minCardThreshold"
            :min="1"
            :max="20"
            :step="1"
            :ticks="{ 1: '1', 5: '5', 10: '10', 15: '15', 20: '20' }"
            show-ticks="always"
            thumb-label
            :disabled="!preferences.dueCards"
          ></v-slider>
        </div>
      </v-card-text>
    </v-card>

    <!-- Notification Channels -->
    <v-card class="mb-4">
      <v-card-title class="text-subtitle-1 pb-0">Kênh thông báo</v-card-title>

      <v-card-text>
        <v-list density="compact">
          <v-list-item>
            <template v-slot:prepend>
              <v-icon color="primary" class="mr-2">mdi-bell</v-icon>
            </template>
            <v-list-item-title>Thông báo trong ứng dụng</v-list-item-title>
            <template v-slot:append>
              <v-switch
                v-model="channels.app"
                color="primary"
                hide-details
              ></v-switch>
            </template>
          </v-list-item>

          <v-list-item>
            <template v-slot:prepend>
              <v-icon color="primary" class="mr-2">mdi-email</v-icon>
            </template>
            <v-list-item-title>Email</v-list-item-title>
            <template v-slot:append>
              <v-switch
                v-model="channels.email"
                color="primary"
                hide-details
              ></v-switch>
            </template>
          </v-list-item>
        </v-list>
      </v-card-text>
    </v-card>

    <v-btn
      block
      color="primary"
      class="mt-4 mb-8"
      @click="savePreferences"
      :loading="loading"
      size="large"
    >
      Lưu thay đổi
    </v-btn>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useToast } from 'vue-toast-notification'
import axios from 'axios'
import authService from '@/services/auth.service'

const toast = useToast()
const loading = ref(false)

// Reminder settings
const preferences = ref({
  system: true,
  dailyGoal: true,
  dueCards: true,
  leechCards: true
})

// Notification channels
const channels = ref({
  app: true,
  email: true
})

// Time picker for reminder time
const reminderTime = ref('20:00')

// Minimum card threshold
const minCardThreshold = ref(5)

// Time selector components
const timeHour = ref('20')
const timeMinute = ref('00')
const hours = Array.from({ length: 24 }, (_, i) => i.toString().padStart(2, '0'))
const minutes = Array.from({ length: 60 }, (_, i) => i.toString().padStart(2, '0'))

// Load user preferences when component mounts
onMounted(async () => {
  await loadUserPreferences()
})

async function loadUserPreferences() {
  try {
    loading.value = true

    // Get preferences from the backend
    const response = await axios.get('/api/v1/user/preferences', {
      headers: { Authorization: `Bearer ${authService.getToken()}` }
    })

    // If preferences exist, set them
    if (response.data) {
      if (response.data.notificationPreferences) {
        const notifPrefs = response.data.notificationPreferences.split(',').map((p: string) => p.trim())
        channels.value.app = notifPrefs.includes('app')
        channels.value.email = notifPrefs.includes('email')
      }

      if (response.data.reminderEnabled !== undefined) {
        preferences.value.dueCards = response.data.reminderEnabled
      }

      if (response.data.reminderTime) {
        reminderTime.value = response.data.reminderTime

        // Parse the time values for the selectors
        const [hour, minute] = response.data.reminderTime.split(':')
        timeHour.value = hour.padStart(2, '0')
        timeMinute.value = minute.padStart(2, '0')
      }

      if (response.data.minCardThreshold !== undefined) {
        minCardThreshold.value = response.data.minCardThreshold
      }

      if (response.data.leechNotificationsEnabled !== undefined) {
        preferences.value.leechCards = response.data.leechNotificationsEnabled
      }
    }
  } catch (error) {
    console.error('Error loading user preferences:', error)
    toast.error('Không thể tải thiết lập thông báo', {
      position: 'top'
    })
  } finally {
    loading.value = false
  }
}

async function savePreferences() {
  try {
    loading.value = true

    // Construct notification preferences string
    const notificationPrefsArray = []
    if (channels.value.app) notificationPrefsArray.push('app')
    if (channels.value.email) notificationPrefsArray.push('email')

    const userData = {
      notificationPreferences: notificationPrefsArray.join(','),
      reminderEnabled: preferences.value.dueCards,
      reminderTime: reminderTime.value,
      minCardThreshold: minCardThreshold.value,
      leechNotificationsEnabled: preferences.value.leechCards
    }

    // Send to the backend
    await axios.put('/api/v1/user/preferences', userData, {
      headers: { Authorization: `Bearer ${authService.getToken()}` }
    })

    toast.success('Cài đặt thông báo đã được lưu', {
      position: 'top'
    })
  } catch (error) {
    console.error('Error saving preferences:', error)
    toast.error('Không thể lưu thiết lập thông báo', {
      position: 'top'
    })
  } finally {
    loading.value = false
  }
}

function updateReminderTime() {
  reminderTime.value = `${timeHour.value}:${timeMinute.value}`
}
</script>

<style scoped>
.notification-settings-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
}

.header-container {
  margin-bottom: 16px;
}

.back-btn {
  margin-right: 8px;
  background-color: rgba(0, 0, 0, 0.03);
  border-radius: 50%;
}

.time-selector-container {
  max-width: 300px;
}

.time-select {
  width: 100px;
}
</style>
