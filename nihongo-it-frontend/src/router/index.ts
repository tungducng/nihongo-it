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
    path: '/learning-progress',
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
    path: '/account-settings',
    name: 'accountSettings',
    component: () => import('@/views/auth/AccountSettingsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/conversation',
    name: 'conversation',
    component: () => import('@/views/learning/ConversationView.vue'),
    // No requiresAuth - publicly accessible
  },
  {
    path: '/vocabulary',
    name: 'vocabulary',
    component: () => import('@/views/learning/vocabulary/VocabularyView.vue'),
    // No requiresAuth - publicly accessible
    meta: { requiresAuth: true }, // Personalized content requires auth

  },
  {
    path: '/vocabulary/saved',
    name: 'savedVocabulary',
    component: () => import('@/views/learning/vocabulary/SavedVocabularyView.vue'),
    meta: { requiresAuth: true }, // Saved content requires auth
  },
  {
    path: '/vocabulary/term/:term',
    name: 'vocabularyDetail',
    component: () => import('@/views/learning/vocabulary/VocabularyDetailView.vue'),
    meta: { requiresAuth: true }, // Personalized content requires auth
  },
  {
    path: '/vocabulary/learning',
    name: 'vocabularyLearning',
    component: () => import('@/views/learning/VocabularyLearningView.vue'),
    meta: { requiresAuth: true }, // Personalized content requires auth
  },
  {
    path: '/exercises',
    name: 'exercises',
    component: () => import('@/views/learning/ExercisesView.vue'),
    // No requiresAuth - publicly accessible
  },
  {
    path: '/flashcards',
    name: 'flashcards',
    component: () => import('@/views/learning/FlashcardsView.vue'),
    // meta: { requiresAuth: true },
  },
  {
    path: '/flashcards/create',
    name: 'flashcardsCreate',
    component: () => import('@/views/learning/FlashcardCreateView.vue'),
    // meta: { requiresAuth: true },
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
    path: '/coach',
    name: 'coachHome',
    component: () => import('@/views/learning/CoachHomeView.vue'),
    // No requiresAuth - publicly accessible for demo
  },
  {
    path: '/learning/course/:courseId',
    name: 'lessonDetail',
    component: () => import('@/views/learning/LessonDetailView.vue'),
    // No requiresAuth - publicly accessible for demo
  },
  {
    path: '/learning/lesson/:lessonId',
    name: 'lesson',
    component: () => import('@/views/learning/LessonView.vue'),
    // This would be implemented later for individual lessons
  },
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
