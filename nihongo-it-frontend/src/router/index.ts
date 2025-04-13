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
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/auth/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/learning-path',
    name: 'learningPath',
    component: () => import('@/views/learning/LearningPathView.vue'),
    meta: { requiresAuth: true }, // Personalized content requires auth
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
    path: '/vocabulary/:id',
    name: 'vocabularyDetail',
    component: () => import('@/views/learning/vocabulary/VocabularyDetailView.vue'),
    meta: { requiresAuth: true }, // Personalized content requires auth
  },
  {
    path: '/exercises',
    name: 'exercises',
    component: () => import('@/views/learning/ExercisesView.vue'),
    // No requiresAuth - publicly accessible
  },
  {
    path: '/progress',
    name: 'progress',
    component: () => import('@/views/learning/ProgressView.vue'),
    meta: { requiresAuth: true }, // Personal progress requires auth
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
