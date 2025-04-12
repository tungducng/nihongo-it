<template>
  <v-container class="profile-container">
    <v-row justify="center" align="center">
      <v-col cols="12" sm="10" md="8" lg="6">
        <v-card class="profile-card">
          <v-card-title class="text-center">
            <h1 class="title">My Profile</h1>
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
                    alt="Profile picture"
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
                  <v-list-item-title class="detail-label">Current Level:</v-list-item-title>
                  <v-list-item-subtitle class="detail-value">{{ user.currentLevel || 'Not set' }}</v-list-item-subtitle>
                </v-list-item>

                <v-list-item>
                  <v-list-item-title class="detail-label">JLPT Goal:</v-list-item-title>
                  <v-list-item-subtitle class="detail-value">{{ user.jlptGoal || 'Not set' }}</v-list-item-subtitle>
                </v-list-item>

                <v-list-item>
                  <v-list-item-title class="detail-label">Last Login:</v-list-item-title>
                  <v-list-item-subtitle class="detail-value">{{ formatDate(user.lastLogin) }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </template>

            <v-alert
              v-else
              type="error"
              class="my-4"
            >
              Unable to load profile information. Please try again later.
              <v-btn
                color="error"
                text
                @click="fetchProfile"
                class="mt-2"
              >
                Try Again
              </v-btn>
            </v-alert>
          </v-card-text>

          <v-card-actions v-if="user" class="justify-center">
            <v-btn
              color="primary"
              @click="logout"
              class="px-6"
            >
              Logout
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import { useAuthStore } from '@/stores'
import type { UserInfo } from '@/services/auth.service'
import { useToast } from 'vue-toast-notification'
import authService from '@/services/auth.service'

@Component({
  name: 'ProfileView'
})
export default class ProfileView extends Vue {
  private authStore = useAuthStore()
  private $toast = useToast()

  get user(): UserInfo | null {
    return this.authStore.user
  }

  get loading(): boolean {
    return this.authStore.loading
  }

  get avatarInitials(): string {
    if (!this.user?.fullName) return ''

    return this.user.fullName
      .split(' ')
      .map(name => name.charAt(0))
      .join('')
      .toUpperCase()
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'Never'

    return dateString
  }

  async fetchProfile(): Promise<void> {
    await this.authStore.fetchCurrentUser()
  }

  logout(): void {
    this.authStore.logout()
    this.$toast.success('You have been logged out successfully', {
      position: 'top',
      duration: 3000
    })
    this.$router.push({ name: 'login' })
  }

  mounted(): void {
    if (!this.user) {
      this.fetchProfile()
    }
  }
}
</script>

<style lang="sass" scoped>
.profile-container
  min-height: 80vh
  display: flex
  align-items: center
  padding: 2rem 0

.profile-card
  width: 100%
  padding: 1rem

.title
  color: #333
  margin-bottom: 1rem
  width: 100%

.profile-header
  display: flex
  align-items: center
  margin-bottom: 1rem

.profile-name
  flex: 1

.detail-label
  font-weight: 600
  color: rgba(0, 0, 0, 0.7)

.detail-value
  color: rgba(0, 0, 0, 0.6)

::v-deep .v-avatar
  background-color: var(--v-primary-base)
</style>
