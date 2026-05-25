import { test, expect } from '@playwright/test'
import {
  uniqueName,
  createConversation,
  listConversations,
  deleteConversationsByPrefix,
} from '@helpers/api-admin'

const PREFIX = 'e2e-conv'

test.describe('17 — Admin conversation CRUD', () => {
  test.afterEach(async () => {
    await deleteConversationsByPrefix(PREFIX)
  })

  test('TC-17-01 create conversation via dialog @smoke', async ({ page }) => {
    const title = uniqueName(PREFIX)

    await page.goto('/conversations')
    await expect(
      page.getByRole('heading', { name: 'Hội thoại', exact: true }),
    ).toBeVisible({ timeout: 30_000 })

    await page.getByRole('button', { name: /Tạo mới/i }).first().click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()
    await dialog.locator('#title').fill(title)
    await dialog.locator('#description').fill('Created by E2E')
    // jlptLevel select
    await dialog.locator('#jlptLevel').click()
    await page.getByRole('option', { name: 'N5' }).click()
    await dialog.getByRole('button', { name: 'Tạo mới' }).click()

    await expect(page.getByText(/Đã tạo hội thoại/i)).toBeVisible({ timeout: 10_000 })
    const all = await listConversations()
    expect(all.some((c) => c.title === title)).toBe(true)
  })

  test('TC-17-02 edit then delete conversation via row menu', async ({ page }) => {
    const title = uniqueName(PREFIX)
    await createConversation(title, 'initial')

    await page.goto('/conversations')
    const row = page.getByRole('row', { name: new RegExp(title) })
    await expect(row).toBeVisible({ timeout: 15_000 })

    // Edit — "Sửa thông tin" opens dialog; "Sửa câu thoại" navigates to /edit page.
    await row.getByRole('button').last().click()
    await page.getByRole('menuitem', { name: 'Sửa thông tin' }).click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()
    await dialog.locator('#description').fill('Edited by E2E')
    await dialog.getByRole('button', { name: 'Cập nhật' }).click()
    await expect(page.getByText(/Đã cập nhật hội thoại/i)).toBeVisible({
      timeout: 10_000,
    })

    // Delete
    await row.getByRole('button').last().click()
    await page.getByRole('menuitem', { name: /Xo[áạ]/i }).click()
    await page.getByRole('alertdialog').getByRole('button', { name: /Xo[áạ]/i }).click()
    await expect(page.getByText(/Đã xo[áạ] hội thoại/i)).toBeVisible({ timeout: 10_000 })

    const all = await listConversations()
    expect(all.some((c) => c.title === title)).toBe(false)
  })
})
