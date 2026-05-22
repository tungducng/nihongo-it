// External-service gating. Default OFF so PR/CI runs never fail because OpenAI
// credit ran out or the Python service isn't started. Flip to 1 to run the
// gated tests too.
//
// Usage in a spec:
//   import { E2E_FEATURES } from '@helpers/feature-flags'
//   test.skip(!E2E_FEATURES.openai, 'requires OpenAI')

export const E2E_FEATURES = {
  openai: process.env.E2E_OPENAI === '1',
  pythonNlp: process.env.E2E_PYTHON_NLP === '1',
} as const

export const E2E_USERS = {
  user: {
    email: process.env.E2E_USER_EMAIL ?? 'user@e2e.test',
    password: process.env.E2E_USER_PASSWORD ?? 'User#2026',
    fullName: process.env.E2E_USER_NAME ?? 'E2E User',
  },
  admin: {
    email: process.env.E2E_ADMIN_EMAIL ?? 'admin@e2e.test',
    password: process.env.E2E_ADMIN_PASSWORD ?? 'Admin#2026',
    fullName: process.env.E2E_ADMIN_NAME ?? 'E2E Admin',
  },
} as const

export const E2E_URLS = {
  gateway: process.env.E2E_GATEWAY_URL ?? 'http://localhost:8080',
  userApp: process.env.E2E_USER_APP_URL ?? 'http://localhost:3000',
  adminApp: process.env.E2E_ADMIN_APP_URL ?? 'http://localhost:3001',
} as const
