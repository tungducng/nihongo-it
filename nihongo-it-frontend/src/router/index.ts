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
    path: '/learning/progress',
    name: 'learningProgress',
    component: () => import('@/views/learning/LearningProgressView.vue'),
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
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    requireAuth(to, from, next);
  } else {
    next();
  }
});

// TODO: Add role guard when role-based auth is implemented

export default router
