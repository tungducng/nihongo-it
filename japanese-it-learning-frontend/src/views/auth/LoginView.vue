<template>
  <div class="login-view d-flex align-center justify-center">
    <v-card class="mx-auto pa-6" max-width="400">
      <v-card-title class="text-center text-h5 mb-4">Login</v-card-title>

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

        <v-btn type="submit" color="primary" block :loading="loading" class="mb-4">Login</v-btn>

        <div class="text-center">
          <v-btn variant="text" color="primary" to="/register">
            Don't have an account? Register
          </v-btn>
        </div>
      </v-form>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'

@Component({
  name: 'LoginView',
})
export default class LoginView extends Vue {
  email = ''
  password = ''
  loading = false
  showPassword = false

  get rules() {
    return {
      email: [
        (v: string) => !!v || 'Email is required',
        (v: string) => /.+@.+\..+/.test(v) || 'Email must be valid',
      ],
      password: [
        (v: string) => !!v || 'Password is required',
        (v: string) => v.length >= 6 || 'Password must be at least 6 characters',
      ],
    }
  }

  async handleLogin(): Promise<void> {
    try {
      this.loading = true
      // TODO: Implement login logic here
      console.log('Login with:', { email: this.email, password: this.password })
      // Simulate successful login
      setTimeout(() => {
        this.$router.push('/')
      }, 1000)
    } catch (error) {
      console.error('Login failed:', error)
    } finally {
      this.loading = false
    }
  }
}
</script>

<style lang="sass" scoped>
.login-view
  background-color: #f5f5f5
  min-height: 80vh

.v-card
  border-radius: 8px
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1)
</style>
