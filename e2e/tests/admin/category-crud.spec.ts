import { test, expect } from '@playwright/test'
import {
  uniqueName,
  deleteCategoriesByPrefix,
  listCategories,
} from '@helpers/api-admin'

const CAT_PREFIX = 'e2e-cat'

test.describe('07 — Admin category CRUD', () => {
  test.afterEach(async () => {
    // Idempotent cleanup so reruns aren't blocked by UNIQUE(name).
    await deleteCategoriesByPrefix(CAT_PREFIX)
  })

  test('TC-07-01 create category via dialog @smoke', async ({ page }) => {
    const name = uniqueName(CAT_PREFIX)

    await page.goto('/categories')
    await expect(
      page.getByRole('heading', { name: 'Danh mục', exact: true }),
    ).toBeVisible({ timeout: 30_000 })

    await page.getByRole('button', { name: /Tạo mới/i }).first().click()
    await expect(
      page.getByRole('dialog').getByText('Tạo danh mục mới'),
    ).toBeVisible()
    await page.getByLabel('Tên danh mục').fill(name)
    await page.getByLabel('Nghĩa').fill('Created by E2E')
    await page.getByRole('dialog').getByRole('button', { name: 'Tạo mới' }).click()

    await expect(page.getByText(/Đã tạo danh mục/i)).toBeVisible({ timeout: 10_000 })
    // The new category appears in the list.
    await expect(page.getByText(name, { exact: true })).toBeVisible({ timeout: 10_000 })

    // API echo
    const all = await listCategories()
    expect(all.some((c) => c.name === name)).toBe(true)
  })

  test('TC-07-02 edit category', async ({ page, request: _ }) => {
    // Seed one to edit
    const initial = uniqueName(CAT_PREFIX)
    const renamed = uniqueName(CAT_PREFIX + '-renamed')
    const { createCategory } = await import('@helpers/api-admin')
    await createCategory(initial, 'Initial meaning')

    await page.goto('/categories')
    // Open the row's actions menu for the seeded category, then click Sửa.
    const row = page.getByRole('row', { name: new RegExp(initial) })
    await row.getByRole('button').last().click() // MoreHorizontal trigger
    await page.getByRole('menuitem', { name: /Sửa/i }).click()

    await expect(
      page.getByRole('dialog').getByText('Sửa danh mục'),
    ).toBeVisible()
    await page.getByLabel('Tên danh mục').fill(renamed)
    await page.getByRole('dialog').getByRole('button', { name: 'Cập nhật' }).click()

    await expect(page.getByText(/Đã cập nhật danh mục/i)).toBeVisible({ timeout: 10_000 })
    await expect(page.getByText(renamed, { exact: true })).toBeVisible({ timeout: 10_000 })
    await expect(page.getByText(initial, { exact: true })).toHaveCount(0)
  })

  test('TC-07-03 delete category via row menu', async ({ page }) => {
    const name = uniqueName(CAT_PREFIX)
    const { createCategory } = await import('@helpers/api-admin')
    await createCategory(name, 'Will be deleted')

    await page.goto('/categories')
    const row = page.getByRole('row', { name: new RegExp(name) })
    await expect(row).toBeVisible({ timeout: 10_000 })
    await row.getByRole('button').last().click()
    await page.getByRole('menuitem', { name: /Xo[áạ]/i }).click()
    // Confirm dialog
    await page.getByRole('alertdialog').getByRole('button', { name: /Xo[áạ]/i }).click()

    await expect(page.getByText(/Đã xo[áạ]/i)).toBeVisible({ timeout: 10_000 })
    await expect(page.getByText(name, { exact: true })).toHaveCount(0)

    // Confirm via API
    const all = await listCategories()
    expect(all.some((c) => c.name === name)).toBe(false)
  })
})
