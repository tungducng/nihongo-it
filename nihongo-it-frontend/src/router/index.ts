import { createRouter, createWebHistory } from 'vue-router'
import { requireAuth, redirectIfAuthenticated } from './guards'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    // No requiresAuth - accessible to everyone
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    beforeEnter: redirectIfAuthenticated
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    beforeEnter: redirectIfAuthenticated
  },
  {
    path: '/statistics',
    name: 'statistics',
    component: () => import('@/views/learning/StatisticsView.vue'),
  },
  {
    path: '/learning/vocabulary-storage',
    name: 'vocabularyStorage',
    component: () => import('@/views/learning/VocabularyStorageView.vue'),
  },
  {
    path: '/account/profile',
    name: 'profile',
    component: () => import('@/views/auth/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/account/settings',
    name: 'accountSettings',
    component: () => import('@/views/auth/AccountSettingsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/account/notifications',
    name: 'notificationSettings',
    component: () => import('@/views/auth/NotificationSettingsView.vue')
  },
  {
    path: '/vocabulary',
    name: 'vocabulary',
    component: () => import('@/views/learning/vocabulary/VocabularyView.vue'),
    // No requiresAuth - publicly accessible
    meta: { requiresAuth: true }, // Personalized content requires auth

  },
  {
    path: '/vocabulary/category',
    name: 'vocabularyLearning',
    component: () => import('@/views/learning/VocabularyLearningView.vue'),
    meta: { requiresAuth: true }, // Personalized content requires auth
  },
  {
    path: '/vocabulary/category/:slug',
    name: 'categoryDetail',
    component: () => import('@/views/learning/CategoryDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/vocabulary/topic/:name',
    name: 'topicDetail',
    component: () => import('@/views/learning/VocabularyTopicView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/vocabulary/topic/:id/:term',
    name: 'vocabularyDetail',
    component: () => import('@/views/learning/vocabulary/VocabularyDetailView.vue'),
    meta: { requiresAuth: true }, // Pronunciation scoring requires auth
  },
  {
    path: '/speech-analyzer',
    name: 'speechAnalyzer',
    component: () => import('@/views/learning/SpeechAnalyzer.vue'),
    meta: { requiresAuth: true }  // Require authentication for speech analysis
  },
  {
    path: '/furigana',
    name: 'furigana',
    component: () => import('@/views/learning/FuriganaView.vue'),
    // No requiresAuth - publicly accessible for demonstrating furigana
  },
  {
    path: '/translations',
    name: 'translations',
    component: () => import('@/views/learning/TranslationView.vue'),
    // No requiresAuth - publicly accessible for translations
  },
  {
    path: '/kaiwa',
    name: 'kaiwa',
    component: () => import('@/views/learning/KaiwaView.vue'),
    // No requiresAuth - publicly accessible for demo
  },
  {
    path: '/account/forgot-password',
    name: 'forgotPassword',
    component: () => import('@/views/auth/ForgotPasswordView.vue'),
    // No requiresAuth - must be accessible to request password reset
  },
  {
    path: '/account/reset-password',
    name: 'resetPassword',
    component: () => import('@/views/auth/ResetPasswordView.vue'),
    // No requiresAuth - must be accessible to request password reset
  },
  {
    path: '/account/change-password',
    name: 'changePassword',
    component: () => import('@/views/auth/ChangePasswordView.vue'),
    meta: { requiresAuth: true } // Requires authentication to change password
  },
  {
    path: '/flashcards/study',
    name: 'flashcardStudy',
    component: () => import('@/views/learning/FlashcardStudyView.vue'),
    meta: { requiresAuth: true } // Flashcard study requires authentication
  },
  {
    path: '/flashcards/statistics',
    name: 'flashcardStatistics',
    component: () => import('@/views/study/FlashcardStatsView.vue'),
    meta: { requiresAuth: true } // Statistics require authentication
  },
  // Admin routes
  {
    path: '/admin',
    name: 'adminDashboard',
    component: () => import('@/views/admin/DashboardView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'adminUsers',
    component: () => import('@/views/admin/UsersView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users/:id',
    name: 'adminUserDetail',
    component: () => import('@/views/admin/UserDetailView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/categories',
    name: 'adminCategories',
    component: () => import('@/views/admin/CategoryManagementView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/topics',
    name: 'adminTopics',
    component: () => import('@/views/admin/TopicManagementView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/vocabulary',
    name: 'adminVocabulary',
    component: () => import('@/views/admin/VocabularyManagementView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach(async (to, from, next) => {
  // Combine authentication and admin role checks to avoid calling next() multiple times
  if (to.meta.requiresAuth) {
    // If authentication is required, check if user is logged in
    if (!localStorage.getItem('auth_token')) {
      return next({ name: 'login', query: { redirect: to.fullPath } });
    }

    try {
      // Validate token with the server
      const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/v1/auth/current`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        }
      });

      if (!response.ok) {
        // Token is invalid, clear it and redirect to login
        localStorage.removeItem('auth_token');
        return next({
          name: 'login',
          query: {
            redirect: to.fullPath,
            error: 'Your session has expired. Please log in again.'
          }
        });
      }

      // Parse API response data
      const responseData = await response.json();

      // If admin role is required, check user role from API response
      if (to.meta.requiresAdmin) {
        // Check if user has admin role based on API response
        if (!responseData || !responseData.userInfo || responseData.userInfo.roleId !== 1) {
          console.warn('Access denied: Admin role required');
          return next({ name: 'home' });
        }
      }

      // User is authenticated and has required role
      return next();
    } catch (error) {
      console.error('Authentication error:', error);
      // Error during validation - if network is down, let them proceed
      if (!window.navigator.onLine) {
        return next();
      } else {
        return next({
          name: 'login',
          query: {
            redirect: to.fullPath,
            error: 'Authentication error. Please try again.'
          }
        });
      }
    }
  } else {
    // No authentication required
    return next();
  }
});

// TODO: Add role guard when role-based auth is implemented

export default router
