import { test, expect } from '@playwright/test'

test.describe('Admin auth', () => {
  test('admin landing redirects away from /login @smoke', async ({ page }) => {
    await page.goto('/')
    await expect(page).not.toHaveURL(/\/login$/)
  })

  test('login page renders for unauthed visitor', async ({ browser }) => {
    const ctx = await browser.newContext({ storageState: { cookies: [], origins: [] } })
    const page = await ctx.newPage()
    await page.goto('/login')
    await expect(page.locator('input[type="email"]')).toBeVisible()
    await expect(page.locator('input[type="password"]')).toBeVisible()
    await ctx.close()
  })
})
