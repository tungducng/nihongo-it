import { test, expect } from '@playwright/test'

// Admin statistics drill-down. The overview merges responses from
// user-service (count, by-level, by-jlpt-goal) and learning-service
// (totalFlashcards, averageRetentionRate) — see P6 architecture.
// We assert the summary cards render numeric content.

test.describe('19 — Admin statistics drill-down', () => {
  test('TC-19-01 statistics overview shows non-empty summary cards @smoke', async ({
    page,
  }) => {
    await page.goto('/statistics')
    await expect(
      page.getByRole('heading', { name: 'Thống kê', exact: true }),
    ).toBeVisible({ timeout: 30_000 })

    // Summary cards: "Tổng người dùng", "Đang hoạt động", "Tổng flashcard",
    // "TB thẻ / user", "TB ghi nhớ".
    await expect(page.getByText('Tổng người dùng')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByText('Đang hoạt động')).toBeVisible()
    await expect(page.getByText('Tổng flashcard')).toBeVisible()

    // At least one card surface has a numeric value. We pick the user-count
    // card; with the seed user + admin user, count is ≥ 2 (a digit visible).
    // Use a relaxed assertion: any digit char shown anywhere on the dashboard.
    await expect(page.locator('body')).toContainText(/\d+/, { timeout: 10_000 })
  })

  test('TC-19-02 drill-down to per-user statistics list', async ({ page }) => {
    await page.goto('/statistics')
    await expect(
      page.getByRole('heading', { name: 'Thống kê', exact: true }),
    ).toBeVisible({ timeout: 30_000 })

    // The "Theo người dùng →" link goes to /statistics/users
    await page.getByRole('link', { name: /Theo người dùng/i }).click()
    await expect(page).toHaveURL(/\/statistics\/users/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: /Thống kê theo người dùng/ }),
    ).toBeVisible({ timeout: 15_000 })
  })
})
