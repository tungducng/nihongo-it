import '@/assets/styles/main.sass'
import 'vuetify/styles'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import vuetify from './plugins/vuetify'
import App from './App.vue'
import router from './router'
import VueToast from 'vue-toast-notification'
import 'vue-toast-notification/dist/theme-sugar.css'
import { useAuthStore } from './stores'
import axios from 'axios'

// Configure axios defaults
axios.defaults.headers.common['Accept'] = 'application/json'
axios.defaults.baseURL = import.meta.env.VITE_API_URL || ''

// Add request interceptor to handle API URLs and authentication
axios.interceptors.request.use(
  config => {
    // For API requests, ensure they go to the backend server
    if (config.url?.startsWith('/api')) {
      config.baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    }

    // Add authorization token if available
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  error => Promise.reject(error)
)

// Add response interceptor to handle unauthorized errors
axios.interceptors.response.use(
  response => response,
  error => {
    // Handle 401 Unauthorized errors
    if (error.response && error.response.status === 401) {
      console.log('Unauthorized access detected, redirecting to login')

      // Clear auth token and user data
      localStorage.removeItem('auth_token')

      // Get Pinia store outside of Vue component
      const pinia = createPinia()
      const authStore = useAuthStore(pinia)
      authStore.logout()

      // Check if we're not already on the login page to avoid redirect loops
      const currentPath = window.location.pathname
      if (!currentPath.includes('/login')) {
        // Redirect to login page with current location for redirect after login
        window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
      }
    }
    return Promise.reject(error)
  }
)

const app = createApp(App)
const pinia = createPinia()

app.use(vuetify)
app.use(pinia)
app.use(router)
app.use(VueToast)

// Initialize auth state before mounting the app
const authStore = useAuthStore()
authStore.initializeAuth()

app.mount('#app')
