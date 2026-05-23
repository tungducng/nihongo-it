import { test, expect } from '@fixtures/auth.fixture'

// Update profile then revert so seed state is restored for downstream tests.

const ORIGINAL_NAME = 'E2E User'

test.describe('10 — User profile update', () => {
  test('TC-10-01 update fullName then revert @smoke', async ({ page }) => {
    await page.goto('/profile')
    await expect(
      page.getByText('Hồ sơ cá nhân', { exact: true }),
    ).toBeVisible({ timeout: 30_000 })

    const newName = 'E2E Updated ' + Date.now()
    await page.locator('#fullName').fill(newName)
    await page.getByRole('button', { name: /Lưu thay đổi/i }).click()
    await expect(page.getByText(/Cập nhật hồ sơ thành công/i)).toBeVisible({
      timeout: 10_000,
    })

    // Revert
    await page.locator('#fullName').fill(ORIGINAL_NAME)
    await page.getByRole('button', { name: /Lưu thay đổi/i }).click()
    await expect(page.getByText(/Cập nhật hồ sơ thành công/i).last()).toBeVisible({
      timeout: 10_000,
    })
  })
})

test.describe('11 — User change password', () => {
  // Force-reset the user password before/after so prior aborted runs don't
  // block this one, and downstream specs always see the seed credentials.
  test.beforeEach(async () => {
    const { upsertUser } = await import('@helpers/db')
    await upsertUser({
      email: 'user@e2e.test',
      password: 'User#2026',
      fullName: 'E2E User',
      isAdmin: false,
    })
  })
  test.afterEach(async () => {
    const { upsertUser } = await import('@helpers/db')
    await upsertUser({
      email: 'user@e2e.test',
      password: 'User#2026',
      fullName: 'E2E User',
      isAdmin: false,
    })
  })

  test('TC-11-01 change password (state restored via SQL after)', async ({ page }) => {
    const ORIGINAL = 'User#2026'
    const TEMP = 'TempE2E#' + Math.floor(Math.random() * 100000)

    await page.goto('/account/change-password')
    await expect(page.getByText('Đổi mật khẩu', { exact: true }).first()).toBeVisible({
      timeout: 30_000,
    })

    await page.locator('#currentPassword').fill(ORIGINAL)
    await page.locator('#newPassword').fill(TEMP)
    await page.locator('#confirmPassword').fill(TEMP)
    await page.getByRole('button', { name: /Đổi mật khẩu/i }).last().click()
    await expect(page.getByText(/Đổi mật khẩu thành công/i)).toBeVisible({
      timeout: 10_000,
    })
  })
})
