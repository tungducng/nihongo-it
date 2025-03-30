import { createRouter, createWebHistory } from 'vue-router'
// import { authGuard, roleGuard } from './guards'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/learning/DashboardView.vue'),
    // No requiresAuth - accessible to everyone
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
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
    component: () => import('@/views/learning/VocabularyView.vue'),
    // No requiresAuth - publicly accessible
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
    path: '/admin',
    name: 'admin',
    component: () => import('@/views/admin/AdminDashboard.vue'),
    meta: { requiresAuth: true, role: 'ROLE_ADMIN' }, // Admin section remains protected
    children: [
      {
        path: 'content',
        name: 'content-management',
        component: () => import('@/views/admin/ContentManagement.vue'),
      },
      {
        path: 'users',
        name: 'user-management',
        component: () => import('@/views/admin/UserManagement.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// router.beforeEach(authGuard)
// router.beforeEach(roleGuard)

export default router
