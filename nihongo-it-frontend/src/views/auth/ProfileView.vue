<template>
  <v-container class="profile-container">
    <v-row justify="center" align="center">
      <v-col cols="12" sm="10" md="8" lg="6">
        <v-card class="profile-card">
          <v-card-title class="text-center">
            <h2 class="title">Hồ Sơ Của Tôi</h2>
          </v-card-title>

          <v-card-text>
            <v-progress-circular
              v-if="loading"
              indeterminate
              color="primary"
              class="mx-auto d-block my-6"
            ></v-progress-circular>

            <template v-else-if="user">
              <div class="profile-header">
                <v-avatar size="80" class="mr-4">
                  <v-img
                    v-if="user.profilePicture"
                    :src="user.profilePicture"
                    alt="Ảnh đại diện"
                  ></v-img>
                  <span v-else class="text-h4 white--text">{{ avatarInitials }}</span>
                </v-avatar>
                <div class="profile-name">
                  <h2 class="text-h5 mb-1">{{ user.fullName }}</h2>
                  <p class="email text-subtitle-1 text-medium-emphasis">{{ user.email }}</p>
                </div>
              </div>

              <v-divider class="my-4"></v-divider>

              <v-list>
                <v-list-item>
                  <v-list-item-title class="detail-label">Trình Độ Hiện Tại:</v-list-item-title>
                  <v-list-item-subtitle class="detail-value">{{ user.currentLevel || 'Chưa thiết lập' }}</v-list-item-subtitle>
                </v-list-item>

                <v-list-item>
                  <v-list-item-title class="detail-label">Mục Tiêu JLPT:</v-list-item-title>
                  <v-list-item-subtitle class="detail-value">{{ user.jlptGoal || 'Chưa thiết lập' }}</v-list-item-subtitle>
                </v-list-item>

                <v-list-item>
                  <v-list-item-title class="detail-label">Lần đăng nhập cuối:</v-list-item-title>
                  <v-list-item-subtitle class="detail-value">{{ formatDate(user.lastLogin) }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </template>

            <v-alert
              v-else
              type="error"
              class="my-4"
            >
              Không thể tải thông tin hồ sơ. Vui lòng thử lại sau.
              <v-btn
                color="error"
                text
                @click="fetchProfile"
                class="mt-2"
              >
                Thử Lại
              </v-btn>
            </v-alert>
          </v-card-text>

          <v-card-actions v-if="user" class="justify-space-between">
            <v-btn
              color="secondary"
              variant="outlined"
              @click="navigateToSettings"
              class="px-6"
            >
              Cài Đặt
            </v-btn>
            <v-btn
              color="primary"
              class="px-6"
            >
              Cập Nhật
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores'
import type { UserInfo } from '@/services/auth.service'
import { useToast } from 'vue-toast-notification'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const user = computed<UserInfo | null>(() => {
  return authStore.user
})

const loading = computed<boolean>(() => {
  return authStore.loading
})

const avatarInitials = computed<string>(() => {
  if (!user.value?.fullName) return ''

  return user.value.fullName
    .split(' ')
    .map(name => name.charAt(0))
    .join('')
    .toUpperCase()
})

const formatDate = (dateString?: string): string => {
  if (!dateString) return 'Chưa bao giờ'

  try {
    const date = new Date(dateString)
    const options: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }

    return date.toLocaleDateString('vi-VN', options)
  } catch (e) {
    return dateString
  }
}

const fetchProfile = async (): Promise<void> => {
  try {
    await authStore.fetchCurrentUser()
  } catch (error) {
    toast.error('Không thể tải thông tin hồ sơ')
  }
}

const navigateToSettings = (): void => {
  router.push({ name: 'accountSettings' })
}

onMounted((): void => {
  if (!user.value) {
    fetchProfile()
  }
})
</script>

<style lang="scss" scoped>
.profile-container {
  min-height: 80vh;
  display: flex;
  align-items: center;
  padding: 2rem 0;
}

.profile-card {
  width: 100%;
  padding: 1rem;
}

.title {
  color: rgba(0, 0, 0, 0.8);
  margin-bottom: 1rem;
  text-align: center;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.profile-name {
  flex: 1;
}

.detail-label {
  font-weight: 600;
  color: rgba(0, 0, 0, 0.7);
}

.detail-value {
  color: rgba(0, 0, 0, 0.6);
}

:deep(.v-avatar) {
  background-color: rgb(var(--v-theme-primary));
}
</style>
