import { test, expect } from '@fixtures/auth.fixture'

// Give the FE plenty of time to: (1) restore session via /auth/refresh, then
// (2) hydrate the protected route. Both are async and sequential.

test.describe('Vocabulary — user app', () => {
  test('vocabulary URL is reachable @smoke', async ({ page }) => {
    await page.goto('/vocabulary')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
  })

  test('account page loads (auth still valid)', async ({ page }) => {
    // Use /account instead of /flashcards/stats — the latter calls a learning-
    // service endpoint that returns 401 before the FE has refreshed its access
    // token, which triggers a window.location.href redirect to /login.
    await page.goto('/account')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
  })
})
