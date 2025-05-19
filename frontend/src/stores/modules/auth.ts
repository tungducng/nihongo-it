import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import authService from '@/services/auth.service';
import type { UserInfo, LoginRequest, SignupRequest, ChangePasswordRequest } from '@/services/auth.service';

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<UserInfo | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Getters
  const isAuthenticated = computed(() => {
    const hasToken = !!authService.getToken();
    const hasUser = !!user.value;
    console.log('Auth state check:', { hasToken, hasUser });
    return hasToken;
  });

  // Actions
  async function login(credentials: LoginRequest) {
    loading.value = true;
    error.value = null;

    try {
      console.log('Attempting login for:', credentials.email);
      const response = await authService.login(credentials);

      if (response.result === 'OK' && response.token) {
        console.log('Login successful');
        await fetchCurrentUser();
        return true;
      } else {
        error.value = 'Invalid credentials';
        console.error('Login response indicated failure:', response);
        return false;
      }
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Login failed. Please try again.';
      console.error('Login error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  async function register(userData: SignupRequest) {
    loading.value = true;
    error.value = null;

    try {
      console.log('Attempting registration for:', userData.email);
      const response = await authService.signup(userData);
      return response.status === 'OK';
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Registration failed. Please try again.';
      console.error('Registration error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  async function fetchCurrentUser() {
    loading.value = true;

    try {
      const response = await authService.getCurrentUser();

      if (response.status === 'OK' && response.userInfo) {
        user.value = response.userInfo;
        return true;
      } else {
        user.value = null;
        console.warn('Failed to get user data:', response);
        return false;
      }
    } catch (err: any) {
      user.value = null;
      console.error('Error fetching user data:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  function logout() {
    console.log('Logging out user');
    authService.removeToken();
    user.value = null;
  }

  // Initialize user if token exists
  function initializeAuth() {
    if (authService.getToken()) {
      fetchCurrentUser();
    } else {
      console.log('No token found, not initializing auth');
    }
  }

  async function loginWithGoogle(tokenId: string) {
    loading.value = true;
    error.value = null;

    try {
      console.log('Attempting Google login with token');
      const response = await authService.loginWithGoogle(tokenId);

      if (response.result === 'OK' && response.token) {
        console.log('Google login successful');
        await fetchCurrentUser();
        return true;
      } else {
        error.value = response.message || 'Google login failed';
        console.error('Google login response indicated failure:', response);
        return false;
      }
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Google login failed. Please try again.';
      console.error('Google login error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  async function requestPasswordReset(email: string) {
    loading.value = true;
    error.value = null;

    try {
      console.log('Requesting password reset for:', email);
      const response = await authService.requestPasswordReset(email);
      return response.status === 'OK';
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Password reset request failed. Please try again.';
      console.error('Password reset request error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  async function resetPassword(token: string, password: string) {
    loading.value = true;
    error.value = null;

    try {
      console.log('Resetting password with token');
      const response = await authService.resetPassword(token, password);
      return response.status === 'OK';
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Password reset failed. Please try again.';
      console.error('Password reset error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  async function changePassword(request: ChangePasswordRequest) {
    loading.value = true;
    error.value = null;

    try {
      console.log('Changing password');
      const response = await authService.changePassword(request);
      return response.status === 'OK';
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Password change failed. Please try again.';
      console.error('Password change error:', err);
      return false;
    } finally {
      loading.value = false;
    }
  }

  return {
    user,
    loading,
    error,
    isAuthenticated,
    login,
    loginWithGoogle,
    register,
    fetchCurrentUser,
    logout,
    initializeAuth,
    requestPasswordReset,
    resetPassword,
    changePassword
  };
});
