import { test, expect } from '@fixtures/auth.fixture'

test.describe('04 — Account + Profile (user app)', () => {
  test('TC-04-01 profile page renders @smoke', async ({ page }) => {
    await page.goto('/profile')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(page.getByText('Hồ sơ cá nhân', { exact: true })).toBeVisible({
      timeout: 30_000,
    })
  })

  test('TC-04-02 change-password page renders', async ({ page }) => {
    await page.goto('/account/change-password')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    // The page has both a CardTitle "Đổi mật khẩu" and a submit button with
    // the same label — `.first()` picks the card title (rendered first).
    await expect(page.getByText('Đổi mật khẩu', { exact: true }).first()).toBeVisible({
      timeout: 15_000,
    })
  })

  test('TC-04-03 speech analyzer page renders', async ({ page }) => {
    await page.goto('/speech')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Phân tích phát âm' }),
    ).toBeVisible({ timeout: 15_000 })
  })
})
