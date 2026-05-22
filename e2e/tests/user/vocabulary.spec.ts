import { test, expect } from '@fixtures/auth.fixture'

// Protected routes require an /auth/refresh-token roundtrip via the FE axios
// interceptor before first paint, so heading visibility gets a 30s timeout
// while the URL-not-/login check uses the standard 15s.

test.describe('01 — Vocabulary (user app)', () => {
  test('TC-01-01 vocabulary list page is reachable @smoke', async ({ page }) => {
    await page.goto('/vocabulary')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Từ vựng', exact: true }),
    ).toBeVisible({ timeout: 30_000 })
  })

  test('TC-01-02 saved vocabulary URL is reachable', async ({ page }) => {
    // Heading assertion intentionally omitted — see spec "Known limitation".
    await page.goto('/vocabulary/saved')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
  })

  test('TC-01-03 learning-by-topic page renders', async ({ page }) => {
    await page.goto('/vocabulary/learning')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Học theo chủ đề', exact: true }),
    ).toBeVisible({ timeout: 15_000 })
  })

  test('TC-01-04 vocabulary URL remains accessible after refresh', async ({ page }) => {
    await page.goto('/vocabulary')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await page.reload()
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
  })
})
