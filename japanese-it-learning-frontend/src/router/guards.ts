import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router';
import authService from '@/services/auth.service';

export const requireAuth = (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) => {
  if (authService.isLoggedIn()) {
    next();
  } else {
    next({ name: 'login', query: { redirect: to.fullPath } });
  }
};

export const redirectIfAuthenticated = (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) => {
  if (authService.isLoggedIn()) {
    next({ name: 'dashboard' });
  } else {
    next();
  }
};
