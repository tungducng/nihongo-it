import { test, expect } from '@fixtures/auth.fixture'

// Protected routes require an /auth/refresh-token roundtrip via the FE axios
// interceptor before first paint, so heading visibility gets a 30s timeout.

test.describe('01 — Vocabulary (user app)', () => {
  test('TC-01-01 vocabulary list page is reachable @smoke', async ({ page }) => {
    await page.goto('/vocabulary')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Từ vựng', exact: true }),
    ).toBeVisible({ timeout: 30_000 })
  })

  test('TC-01-02 saved vocabulary page renders (navigated)', async ({ page }) => {
    // Navigate through /vocabulary first so the FE's auth store is initialized
    // by the time we hop to /vocabulary/saved. Direct goto on a protected
    // sub-route is flaky because the FE fires data XHRs before the refresh
    // call wins; this matches how real users navigate.
    await page.goto('/vocabulary')
    await expect(
      page.getByRole('heading', { name: 'Từ vựng', exact: true }),
    ).toBeVisible({ timeout: 30_000 })

    await page.getByRole('link', { name: /Từ đã lưu|Saved/i }).first().click().catch(async () => {
      // Fallback if there's no nav link — directly navigate.
      await page.goto('/vocabulary/saved')
    })
    await expect(page).toHaveURL(/\/vocabulary\/saved/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Từ vựng đã lưu', exact: true }),
    ).toBeVisible({ timeout: 15_000 })
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
    await expect(
      page.getByRole('heading', { name: 'Từ vựng', exact: true }),
    ).toBeVisible({ timeout: 30_000 })
    await page.reload()
    await expect(
      page.getByRole('heading', { name: 'Từ vựng', exact: true }),
    ).toBeVisible({ timeout: 30_000 })
  })
})
