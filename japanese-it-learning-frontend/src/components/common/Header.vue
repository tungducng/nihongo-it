<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
import { RouterLink } from 'vue-router'

defineProps({
  isLoggedIn: {
    type: Boolean,
    default: false,
  },
  isAdmin: {
    type: Boolean,
    default: false,
  },
  userLevel: {
    type: String,
    default: 'N5',
  },
})

const emit = defineEmits(['logout', 'toggle-drawer'])

const handleLogout = () => {
  emit('logout')
}
</script>

<template>
  <v-app-bar app>
    <v-app-bar-title>
      <RouterLink to="/" class="logo-link"> Nihongo IT </RouterLink>
    </v-app-bar-title>

    <v-spacer></v-spacer>

    <div v-if="isLoggedIn" class="level-indicator">
      <v-chip color="primary">{{ userLevel }}</v-chip>
    </div>

    <v-app-bar-nav-icon
      v-if="isLoggedIn"
      variant="text"
      @click.stop="emit('toggle-drawer')"
    ></v-app-bar-nav-icon>

    <nav v-if="isLoggedIn">
      <v-btn to="/" variant="text">Dashboard</v-btn>
      <v-btn to="/learning-path" variant="text">Learning Path</v-btn>
      <v-btn to="/conversation" variant="text">Conversation</v-btn>
      <v-btn to="/vocabulary" variant="text">Vocabulary</v-btn>
      <v-btn to="/exercises" variant="text">Exercises</v-btn>
      <v-btn to="/progress" variant="text">Progress</v-btn>
      <v-btn v-if="isAdmin" to="/admin" variant="text">Admin</v-btn>
      <v-btn @click="handleLogout" variant="outlined">Logout</v-btn>
    </nav>

    <nav v-else>
      <v-btn to="/login" variant="text">Login</v-btn>
      <v-btn to="/register" variant="outlined">Register</v-btn>
    </nav>
  </v-app-bar>
</template>

<style lang="scss" scoped>
.logo-link {
  text-decoration: none;
  color: inherit;
  font-weight: bold;
  font-size: 1.5rem;
}

.level-indicator {
  margin-right: 1rem;
}

nav {
  display: flex;
  gap: 0.5rem;
}
</style>
