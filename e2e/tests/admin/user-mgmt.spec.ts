import { test, expect } from '@playwright/test'
import { createThrowawayUser, deleteThrowawayUser, uniqueName } from '@helpers/api-admin'

// Admin user management — activate/deactivate via row menu + confirm dialog.
// We use a throwaway user (not the seed admin or seed user) so a failed run
// can't lock the seed accounts.

test.describe('12 — Admin user management', () => {
  const throwawayEmail = `throwaway-${Date.now()}@e2e.test`
  const password = uniqueName('Throw#') + 'X1'

  test.beforeEach(async () => {
    await createThrowawayUser(throwawayEmail, password, 'Throwaway User')
  })
  test.afterEach(async () => {
    await deleteThrowawayUser(throwawayEmail)
  })

  test('TC-12-01 deactivate a throwaway user via row menu @smoke', async ({ page }) => {
    await page.goto('/users')
    await expect(
      page.getByRole('heading', { name: 'Người dùng', exact: true }),
    ).toBeVisible({ timeout: 30_000 })

    // Search for the throwaway to bring it into the page.
    await page.getByPlaceholder(/Tìm|tên|email/i).first().fill(throwawayEmail)

    const row = page.getByRole('row', { name: new RegExp(throwawayEmail) })
    await expect(row).toBeVisible({ timeout: 15_000 })

    // Open row menu, click "Vô hiệu hoá", confirm.
    await row.getByRole('button', { name: 'Tác vụ' }).click()
    await page.getByRole('menuitem', { name: /Vô hiệu hoá/i }).click()
    await page.getByRole('alertdialog').getByRole('button', { name: /Vô hiệu hoá/i }).click()

    await expect(page.getByText(/Đã vô hiệu hoá/i)).toBeVisible({ timeout: 10_000 })
  })

  test('TC-12-02 promote throwaway user to ADMIN via detail page + revert', async ({ page }) => {
    // Look up the userId from DB so we can navigate straight to the detail
    // page (faster + avoids row-search flakiness).
    const { withClient } = await import('@helpers/db')
    const userId = await withClient('user', async (c) => {
      const r = await c.query<{ user_id: string }>(
        `SELECT user_id FROM users WHERE email = $1`,
        [throwawayEmail],
      )
      return r.rows[0].user_id
    })

    await page.goto(`/users/${userId}`)
    await expect(page.getByText(throwawayEmail)).toBeVisible({ timeout: 30_000 })

    // Promote
    await page.getByRole('button', { name: /Cấp Admin/i }).click()
    await page.getByRole('alertdialog').getByRole('button', { name: /Cấp quyền/i }).click()
    await expect(page.getByText(/Đã cấp quyền Admin/i)).toBeVisible({ timeout: 10_000 })
    await expect(page.getByRole('button', { name: /Thu hồi Admin/i })).toBeVisible({
      timeout: 10_000,
    })

    // Revert
    await page.getByRole('button', { name: /Thu hồi Admin/i }).click()
    await page.getByRole('alertdialog').getByRole('button', { name: /Thu hồi/i }).click()
    await expect(page.getByText(/Đã thu hồi quyền Admin/i)).toBeVisible({ timeout: 10_000 })
  })
})
