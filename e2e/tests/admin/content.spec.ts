import { test, expect } from '@playwright/test'

// Admin content-management routes — each is a CRUD list page. Tests assert
// the page hydrates with its heading; CRUD interaction is a later phase.

test.describe('06 — Admin content', () => {
  test('TC-06-01 admin vocabulary list renders @smoke', async ({ page }) => {
    await page.goto('/vocabulary')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Từ vựng', exact: true }),
    ).toBeVisible({ timeout: 30_000 })
  })

  test('TC-06-02 admin categories list renders', async ({ page }) => {
    await page.goto('/categories')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Danh mục', exact: true }),
    ).toBeVisible({ timeout: 30_000 })
  })

  test('TC-06-03 admin topics list renders', async ({ page }) => {
    await page.goto('/topics')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Chủ đề', exact: true }),
    ).toBeVisible({ timeout: 30_000 })
  })

  test('TC-06-04 admin conversations list renders', async ({ page }) => {
    await page.goto('/conversations')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Hội thoại', exact: true }),
    ).toBeVisible({ timeout: 30_000 })
  })

  test('TC-06-05 admin statistics overview renders', async ({ page }) => {
    await page.goto('/statistics')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
    await expect(
      page.getByRole('heading', { name: 'Thống kê', exact: true }),
    ).toBeVisible({ timeout: 30_000 })
  })
})
