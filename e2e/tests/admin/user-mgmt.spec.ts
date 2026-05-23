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
})
