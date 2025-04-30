<template>
  <div class="change-password">
    <div class="change-password-container">
      <div class="change-password-header">
        <h1>Change Password</h1>
        <p>Enter your current password and new password below.</p>
      </div>

      <div class="change-password-form">
        <v-form @submit.prevent="handleSubmit" ref="form">
          <v-alert
            v-if="error"
            type="error"
            density="compact"
            class="mb-4"
            closable
          >
            {{ error }}
          </v-alert>

          <v-alert
            v-if="success"
            type="success"
            density="compact"
            class="mb-4"
            closable
          >
            {{ success }}
          </v-alert>

          <v-text-field
            v-model="currentPassword"
            label="Current Password"
            type="password"
            :rules="[rules.required]"
            variant="outlined"
            prepend-inner-icon="mdi-lock-outline"
            class="mb-2"
          ></v-text-field>

          <v-text-field
            v-model="newPassword"
            label="New Password"
            type="password"
            :rules="[rules.required, rules.minLength]"
            variant="outlined"
            prepend-inner-icon="mdi-lock-outline"
            class="mb-2"
          ></v-text-field>

          <v-text-field
            v-model="confirmPassword"
            label="Confirm New Password"
            type="password"
            :rules="[rules.required, rules.passwordMatch]"
            variant="outlined"
            prepend-inner-icon="mdi-lock-outline"
            class="mb-2"
          ></v-text-field>

          <v-btn
            type="submit"
            block
            color="primary"
            class="mt-2"
            size="large"
            :loading="loading"
          >
            Change Password
          </v-btn>

          <div class="text-center mt-4">
            <router-link to="/account/profile" class="back-link">
              Back to Profile
            </router-link>
          </div>
        </v-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)
const form = ref<any>(null)

const rules = {
  required: (v: string) => !!v || 'This field is required',
  minLength: (v: string) => v.length >= 8 || 'Password must be at least 8 characters',
  passwordMatch: (v: string) => v === newPassword.value || 'Passwords do not match'
}

const handleSubmit = async () => {
  console.log('Submit button clicked')

  // Check form validation
  const { valid } = await form.value.validate()
  console.log('Form validation result:', valid)

  if (!valid) {
    console.log('Form validation failed')
    return
  }

  if (newPassword.value === currentPassword.value) {
    console.log('New password matches current password')
    error.value = 'New password cannot be the same as current password'
    return
  }

  console.log('Preparing to call API')
  error.value = ''
  success.value = ''
  loading.value = true

  try {
    console.log('Calling authStore.changePassword with:', {
      currentPassword: '****',
      newPassword: '****',
      confirmPassword: '****'
    })

    const result = await authStore.changePassword({
      currentPassword: currentPassword.value,
      newPassword: newPassword.value,
      confirmPassword: confirmPassword.value
    })

    console.log('API call result:', result)

    if (result) {
      success.value = 'Your password has been changed successfully'
      // Clear form
      currentPassword.value = ''
      newPassword.value = ''
      confirmPassword.value = ''

      // Redirect to profile after 3 seconds
      setTimeout(() => {
        router.push('/account/profile')
      }, 3000)
    } else if (authStore.error) {
      error.value = authStore.error
    }
  } catch (err: any) {
    console.error('Error during password change:', err)
    error.value = err.message || 'Something went wrong. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.change-password {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 64px);
  padding: 1rem;
  background: #f5f5f5;

  &-container {
    max-width: 450px;
    width: 100%;
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    padding: 2rem;
  }

  &-header {
    text-align: center;
    margin-bottom: 2rem;

    h1 {
      margin-bottom: 0.5rem;
      font-weight: 600;
      color: #333;
    }

    p {
      color: #666;
      font-size: 0.9rem;
      line-height: 1.4;
    }
  }
}

.back-link {
  color: var(--v-primary-base);
  text-decoration: none;
  font-size: 0.9rem;

  &:hover {
    text-decoration: underline;
  }
}
</style>
