<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const rules = {
  username: [
    (v: string) => !!v || 'Username is required',
    (v: string) => v.length >= 3 || 'Username must be at least 3 characters',
  ],
  email: [
    (v: string) => !!v || 'Email is required',
    (v: string) => /.+@.+\..+/.test(v) || 'Email must be valid',
  ],
  password: [
    (v: string) => !!v || 'Password is required',
    (v: string) => v.length >= 6 || 'Password must be at least 6 characters',
  ],
  confirmPassword: [
    (v: string) => !!v || 'Please confirm your password',
    (v: string) => v === password.value || 'Passwords do not match',
  ],
}

const handleRegister = async () => {
  try {
    loading.value = true
    // TODO: Implement registration logic here
    // const response = await authService.register({
    //   username: username.value,
    //   email: email.value,
    //   password: password.value
    // })
    // router.push('/login')
  } catch (error) {
    console.error('Registration failed:', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <v-card class="mx-auto pa-6" max-width="400">
    <v-card-title class="text-center text-h5 mb-4"> Register </v-card-title>

    <v-form @submit.prevent="handleRegister">
      <v-text-field
        v-model="username"
        label="Username"
        :rules="rules.username"
        required
        variant="outlined"
        full-width
        class="mb-4"
        prepend-inner-icon="mdi-account"
      />

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

      <v-text-field
        v-model="confirmPassword"
        label="Confirm Password"
        :type="showConfirmPassword ? 'text' : 'password'"
        :rules="rules.confirmPassword"
        required
        variant="outlined"
        full-width
        class="mb-4"
        prepend-inner-icon="mdi-lock-check"
        :append-inner-icon="showConfirmPassword ? 'mdi-eye-off' : 'mdi-eye'"
        @click:append-inner="showConfirmPassword = !showConfirmPassword"
      />

      <v-btn type="submit" color="primary" block :loading="loading" class="mb-4"> Register </v-btn>

      <div class="text-center">
        <v-btn variant="text" color="primary" to="/login"> Already have an account? Login </v-btn>
      </div>
    </v-form>
  </v-card>
</template>

<style scoped>
.register-view {
  background-color: #f5f5f5;
}
</style>
