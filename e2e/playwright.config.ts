import { defineConfig, devices } from '@playwright/test'
import * as dotenv from 'dotenv'
import * as path from 'path'

dotenv.config({ path: path.resolve(__dirname, '.env') })

const USER_APP_URL = process.env.E2E_USER_APP_URL ?? 'http://localhost:3000'
const ADMIN_APP_URL = process.env.E2E_ADMIN_APP_URL ?? 'http://localhost:3001'

const isCi = !!process.env.CI

export default defineConfig({
  testDir: path.resolve(__dirname, 'tests'),
  testMatch: '**/*.spec.ts',
  fullyParallel: false,
  workers: 1,
  retries: isCi ? 1 : 0,
  reporter: isCi ? [['list'], ['html', { open: 'never' }]] : [['list'], ['html']],
  timeout: 60_000,
  expect: { timeout: 10_000 },

  use: {
    // Headed by default on local; CI uses headless. The user wants to SEE the browser.
    headless: isCi,
    launchOptions: {
      slowMo: isCi ? 0 : 100,
      args: ['--use-fake-device-for-media-stream', '--use-fake-ui-for-media-stream'],
    },
    actionTimeout: 15_000,
    navigationTimeout: 30_000,
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
    screenshot: 'only-on-failure',
    locale: 'vi-VN',
    timezoneId: 'Asia/Ho_Chi_Minh',
    ignoreHTTPSErrors: true,
  },

  projects: [
    {
      name: 'setup',
      testMatch: 'seed.spec.ts',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'user',
      dependencies: ['setup'],
      testMatch: 'user/**/*.spec.ts',
      use: {
        ...devices['Desktop Chrome'],
        baseURL: USER_APP_URL,
        storageState: '.auth/user.json',
      },
    },
    {
      name: 'admin',
      dependencies: ['setup'],
      testMatch: 'admin/**/*.spec.ts',
      use: {
        ...devices['Desktop Chrome'],
        baseURL: ADMIN_APP_URL,
        storageState: '.auth/admin.json',
      },
    },
  ],

  // Stack lifecycle is handled OUTSIDE Playwright by scripts/start-stack.ps1
  // (postgres docker + eureka-first + 4 BE services + 2 FE apps with ordered
  // readiness checks). This avoids Playwright's parallel webServer[] race where
  // services that depend on Eureka fail to register at startup.
  //
  // To run tests:
  //   ./scripts/start-stack.ps1
  //   npm test
})
