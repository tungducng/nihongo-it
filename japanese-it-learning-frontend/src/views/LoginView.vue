<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="4">
        <v-card>
          <v-card-title class="headline">Login</v-card-title>
          <v-card-text>
            <v-form @submit.prevent="login">
              <v-text-field v-model="form.username" label="Username" required></v-text-field>
              <v-text-field
                v-model="form.password"
                label="Password"
                type="password"
                required
              ></v-text-field>
              <v-btn type="submit" color="primary" block>Login</v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import AuthService from '@/services/auth.service'

const router = useRouter()
const authStore = useAuthStore()
const form = ref({
  username: '',
  password: '',
})

const login = async () => {
  try {
    const user = await AuthService.login(form.value)
    authStore.setUser(user)
    router.push('/')
  } catch (error) {
    console.error('Login failed:', error)
  }
}
</script>

<style lang="sass" scoped>

.fill-height
  height: 100vh
</style>
