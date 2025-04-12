<template>
  <!-- Navigation Header -->
  <v-app-bar flat class="px-3">
    <v-app-bar-title class="text-h6 font-weight-bold">
      <router-link to="/" class="app-name text-decoration-none text-inherit">日本語 IT</router-link>
    </v-app-bar-title>

    <v-spacer></v-spacer>

    <!-- Main Navigation Links -->
    <div class="d-flex align-center navigation-links">
      <v-btn
        variant="text"
        to="/dashboard"
        class="mx-2"
      >
        Dashboard
      </v-btn>
      <v-btn
        variant="text"
        to="/learning-path"
        class="mx-2"
      >
        Learning Path
      </v-btn>
      <v-btn
        variant="text"
        to="/vocabulary"
        class="mx-2"
      >
        Vocabulary
      </v-btn>
      <v-btn
        variant="text"
        to="/practice"
        class="mx-2"
      >
        Practice
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
        Login
      </v-btn>
      <v-btn
        to="/register"
        color="primary"
        variant="elevated"
        class="ml-2"
      >
        Register
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
              <div class="text-caption text-medium-emphasis">{{ userLevel }} Level</div>
            </div>
          </div>
        </v-card-text>
        <v-divider></v-divider>
        <v-list>
          <v-list-item to="/profile" prepend-icon="mdi-account-outline">
            <v-list-item-title>My Profile</v-list-item-title>
          </v-list-item>
          <v-list-item to="/settings" prepend-icon="mdi-cog-outline">
            <v-list-item-title>Settings</v-list-item-title>
          </v-list-item>
          <v-divider></v-divider>
          <v-list-item @click="logout" prepend-icon="mdi-logout">
            <v-list-item-title>Logout</v-list-item-title>
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
