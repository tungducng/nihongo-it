<template>
  <v-container fluid class="password-reset fill-height">
    <v-row justify="center" align="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card class="auth-card mx-auto" elevation="8">
          <v-card-item class="text-center pt-8 pb-4">
            <div class="icon-container mb-4">
              <v-icon class="mail-icon" icon="mdi-email-outline"></v-icon>
            </div>
            <v-card-title class="text-h4 font-weight-bold mb-2">
              {{ isVietnamese ? 'Đặt Lại Mật Khẩu' : 'Reset Password' }}
            </v-card-title>
            <v-card-subtitle>
              {{ isVietnamese ? 'Nhập email của bạn để nhận hướng dẫn đặt lại' : 'Enter your email to receive reset instructions' }}
            </v-card-subtitle>
          </v-card-item>

          <v-card-text class="px-6">
            <v-alert
              v-if="isSubmitted"
              color="success"
              icon="mdi-check-circle"
              variant="tonal"
              class="mb-6"
            >
              {{ isVietnamese ?
                'Nếu tồn tại tài khoản với email đó, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu. Vui lòng kiểm tra email của bạn.' :
                'If an account with that email exists, we\'ve sent instructions to reset your password. Please check your email.'
              }}
            </v-alert>

            <v-form v-else @submit.prevent="handleSubmit">
              <v-text-field
                v-model="email"
                :error-messages="emailError"
                :label="isVietnamese ? 'Địa Chỉ Email' : 'Email Address'"
                type="email"
                variant="outlined"
                prepend-inner-icon="mdi-email"
                class="mb-4"
                required
                autocomplete="email"
              ></v-text-field>

              <v-btn
                type="submit"
                color="primary"
                block
                size="large"
                :loading="isLoading"
                class="auth-submit-btn mb-6"
              >
                <v-icon start>mdi-send</v-icon>
                {{ isVietnamese ? 'Gửi liên kết đặt lại mật khẩu' : 'Send password reset link' }}
              </v-btn>

              <div class="d-flex justify-space-between align-center">
                <v-btn
                  to="/login"
                  color="primary"
                  variant="text"
                  prepend-icon="mdi-arrow-left"
                >
                  {{ isVietnamese ? 'Quay Lại Đăng Nhập' : 'Back to Login' }}
                </v-btn>

                <v-btn
                  icon
                  variant="text"
                  color="primary"
                  @click="toggleLanguage"
                  aria-label="Toggle language"
                >
                  <v-tooltip location="bottom" :text="isVietnamese ? 'Switch to English' : 'Chuyển sang tiếng Việt'">
                    <template v-slot:activator="{ props }">
                      <v-icon v-bind="props">
                        {{ isVietnamese ? 'mdi-translate' : 'mdi-translate' }}
                      </v-icon>
                    </template>
                  </v-tooltip>
                </v-btn>
              </div>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/modules/auth';

const authStore = useAuthStore();

const email = ref('');
const emailError = ref('');
const isLoading = ref(false);
const isSubmitted = ref(false);
const isVietnamese = ref(true);

const toggleLanguage = () => {
  isVietnamese.value = !isVietnamese.value;
  if (emailError.value) {
    validateEmail(); // Refresh validation messages in the new language
  }
};

const validateEmail = (): boolean => {
  emailError.value = '';

  if (!email.value) {
    emailError.value = isVietnamese.value ? 'Email là bắt buộc' : 'Email is required';
    return false;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email.value)) {
    emailError.value = isVietnamese.value ? 'Vui lòng nhập địa chỉ email hợp lệ' : 'Please enter a valid email address';
    return false;
  }

  return true;
};

const handleSubmit = async () => {
  if (!validateEmail()) return;

  isLoading.value = true;

  try {
    const success = await authStore.requestPasswordReset(email.value);
    if (success) {
      isSubmitted.value = true;
    }
  } catch (error) {
    console.error('Password reset request failed:', error);
  } finally {
    isLoading.value = false;
  }
};
</script>

<style lang="scss" scoped>
.password-reset {
  background-color: rgb(var(--v-theme-background));
  min-height: calc(100vh - 64px);
}

.auth-card {
  border-radius: 16px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  max-width: 500px;

  &:hover {
    transform: translateY(-5px);
  }
}

.icon-container {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  background: linear-gradient(45deg, rgb(var(--v-theme-primary)) 0%, rgba(var(--v-theme-primary), 0.8) 100%);

  .mail-icon {
    font-size: 36px;
    color: white;
  }
}

.auth-submit-btn {
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
  }
}
</style>
