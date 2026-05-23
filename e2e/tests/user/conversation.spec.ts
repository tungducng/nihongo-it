import { test, expect } from '@fixtures/auth.fixture'

test.describe('03 — Conversation (user app)', () => {
  test('TC-03-01 conversation list renders @smoke', async ({ page }) => {
    await page.goto('/conversation')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Hội thoại', exact: true }),
    ).toBeVisible({ timeout: 30_000 })
  })
})
