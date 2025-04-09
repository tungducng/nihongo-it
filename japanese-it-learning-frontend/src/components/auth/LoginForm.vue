<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const email = ref('')
const password = ref('')
const loading = ref(false)
const showPassword = ref(false)

const rules = {
  email: [
    (v: string) => !!v || 'Email is required',
    (v: string) => /.+@.+\..+/.test(v) || 'Email must be valid',
  ],
  password: [
    (v: string) => !!v || 'Password is required',
    (v: string) => v.length >= 6 || 'Password must be at least 6 characters',
  ],
}

const handleLogin = async () => {
  try {
    loading.value = true
    // TODO: Implement login logic here
    // const response = await authService.login(email.value, password.value)
    // router.push('/dashboard')
  } catch (error) {
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <v-card class="mx-auto pa-6" max-width="400">
    <v-card-title class="text-center text-h5 mb-4"> Login </v-card-title>

    <v-form @submit.prevent="handleLogin">
      <v-text-field
        v-model="email"
        label="Email"
        type="email"
        :rules="rules.email"
        required
        variant="outlined"
        full-width
        class="mb-4"
        prepend-inner-icon="mdi-email"
      />

      <v-text-field
        v-model="password"
        label="Password"
        :type="showPassword ? 'text' : 'password'"
        :rules="rules.password"
        required
        variant="outlined"
        full-width
        class="mb-4"
        prepend-inner-icon="mdi-lock"
        :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
        @click:append-inner="showPassword = !showPassword"
      />

      <v-btn type="submit" color="primary" block :loading="loading" class="mb-4"> Login </v-btn>

      <div class="text-center">
        <v-btn variant="text" color="primary" to="/register">
          Don't have an account? Register
        </v-btn>
      </div>
    </v-form>
  </v-card>
</template>

<style scoped>
.v-card {
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
</style>
