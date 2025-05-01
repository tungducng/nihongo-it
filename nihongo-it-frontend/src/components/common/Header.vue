<template>
  <!-- Navigation Header -->
  <v-app-bar flat class="px-3">
    <v-app-bar-title class="text-h6 font-weight-bold">
      <router-link to="/" class="app-name text-decoration-none text-inherit">Nihongo IT</router-link>
    </v-app-bar-title>

    <v-spacer></v-spacer>

    <!-- Main Navigation Links -->
    <div class="d-flex align-center navigation-links">
      <v-btn
        variant="text"
        to="/"
        class="mx-2"
      >
        Trang chủ
      </v-btn>
      <v-btn
        variant="text"
        to="/vocabulary/learning"
        class="mx-2"
      >
        Từ vựng
      </v-btn>
      <v-btn
        variant="text"
        to="/kaiwa"
        class="mx-2"
      >
        Hội thoại
      </v-btn>
      <v-btn
        variant="text"
        to="/furigana"
        class="mx-2"
      >
        Furigana
      </v-btn>
      <v-btn
        variant="text"
        to="/learning/progress"
        class="mx-2"
      >
        Tiến độ học tập
      </v-btn>
    </div>

    <v-spacer></v-spacer>

    <!-- Login/Register Buttons (for logged out users) -->
    <div v-if="!isLoggedIn" class="d-flex align-center">
      <v-btn
        variant="text"
        to="/login"
        class="mx-1"
      >
        Đăng nhập
      </v-btn>
      <v-btn
        to="/register"
        color="primary"
        variant="elevated"
        class="ml-2"
      >
        Đăng ký
      </v-btn>
    </div>

    <!-- User Avatar & Dropdown (for logged in users) -->
    <v-menu v-else min-width="200px" rounded>
      <template v-slot:activator="{ props }">
        <v-btn
          variant="text"
          v-bind="props"
          class="ml-2"
        >
          <v-avatar size="40" color="primary" class="mr-2">
            <span class="text-h6 text-white">{{ avatarInitials }}</span>
          </v-avatar>
          <v-icon>mdi-chevron-down</v-icon>
        </v-btn>
      </template>
      <v-card>
        <v-card-text>
          <div class="d-flex align-center mb-3">
            <v-avatar size="40" color="primary" class="mr-3">
              <span class="text-h6 text-white">{{ avatarInitials }}</span>
            </v-avatar>
            <div>
              <div class="text-subtitle-1 font-weight-medium">{{ username }}</div>
              <div class="text-caption text-medium-emphasis">{{ userLevel }}</div>
            </div>
          </div>
        </v-card-text>
        <v-divider></v-divider>
        <v-list>
          <v-list-item to="/account/profile" prepend-icon="mdi-account-outline">
            <v-list-item-title>Hồ sơ cá nhân</v-list-item-title>
          </v-list-item>
          <v-list-item to="/account/settings" prepend-icon="mdi-cog-outline">
            <v-list-item-title>Cài đặt</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-card>
    </v-menu>
  </v-app-bar>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-facing-decorator'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores'

@Component({
  name: 'AppHeader'
})
export default class AppHeader extends Vue {
  private authStore = useAuthStore()

  get isLoggedIn(): boolean {
    return !!this.authStore.user
  }

  get username(): string {
    return this.authStore.user?.fullName || 'Guest'
  }

  get userLevel(): string {
    return this.authStore.user?.currentLevel || 'N5'
  }

  get avatarInitials(): string {
    return this.username.charAt(0).toUpperCase()
  }

  logout(): void {
    this.authStore.logout()
    const router = useRouter()
    router.push('/login')
  }
}
</script>

<style lang="sass" scoped>
.navigation-links
  position: absolute
  left: 50%
  transform: translateX(-50%)

.text-inherit
  color: inherit !important

::v-deep .app-name
  cursor: pointer

  a
    color: inherit
    &:hover
      opacity: 0.85
</style>
