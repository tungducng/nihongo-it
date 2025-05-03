<template>
  <v-app>
    <app-header />

    <v-main>
      <v-container fluid>
        <RouterView />
      </v-container>
    </v-main>

    <Footer />

    <!-- Study Reminder Toast -->
    <study-reminder-toast v-if="isAuthenticated" />
  </v-app>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import { computed } from 'vue'
import AppHeader from './components/common/Header.vue'
import Footer from './components/common/Footer.vue'
import StudyReminderToast from './components/common/StudyReminderToast.vue'
import { useAuthStore } from './stores'

const authStore = useAuthStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)
</script>

<style lang="sass"></style>
