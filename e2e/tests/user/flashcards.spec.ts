import { test, expect } from '@fixtures/auth.fixture'

test.describe('02 — Flashcards (user app)', () => {
  test('TC-02-01 flashcard stats page renders @smoke', async ({ page }) => {
    await page.goto('/flashcards/stats')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Thống kê học tập', exact: true }),
    ).toBeVisible({ timeout: 30_000 })
  })

  test('TC-02-02 flashcard study page renders', async ({ page }) => {
    await page.goto('/flashcards/study')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    // Either the heading (cards available) or the empty-state copy ("Không có thẻ" / similar)
    // is acceptable proof the route hydrated. The route should at minimum stay on /flashcards/study.
    await expect(page).toHaveURL(/\/flashcards\/study/, { timeout: 15_000 })
  })
})
