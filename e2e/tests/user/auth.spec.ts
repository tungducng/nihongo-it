import { test, expect } from '@fixtures/auth.fixture'
import { E2E_USERS } from '@helpers/feature-flags'

test.describe('Auth — user app', () => {
  // Render-only smoke: storageState already has us logged in, so the unauthed
  // login page check uses a fresh context.
  test('login page renders form @smoke', async ({ browser }) => {
    const ctx = await browser.newContext({ storageState: { cookies: [], origins: [] } })
    const page = await ctx.newPage()
    await page.goto('/login')

    await expect(page.locator('input[type="email"]')).toBeVisible()
    await expect(page.locator('input[type="password"]')).toBeVisible()
    await expect(page.locator('button[type="submit"]')).toBeVisible()

    await ctx.close()
  })

  test('logged-in user lands on app (not /login)', async ({ page }) => {
    // storageState (user.json) provides the session — going to / should NOT
    // bounce to /login.
    await page.goto('/')
    await expect(page).not.toHaveURL(/\/login$/)
  })

  test('invalid credentials show error', async ({ browser }) => {
    const ctx = await browser.newContext({ storageState: { cookies: [], origins: [] } })
    const page = await ctx.newPage()
    await page.goto('/login')

    await page.locator('input[type="email"]').fill(E2E_USERS.user.email)
    await page.locator('input[type="password"]').fill('definitely-wrong-password')
    await page.locator('button[type="submit"]').click()

    // The FE shows the error via toast (sonner) or inline error text. Accept either.
    const toast = page.locator('[data-sonner-toast][data-type="error"]')
    const inlineErr = page.getByText(/sai|invalid|incorrect|không đúng/i)
    await expect(toast.or(inlineErr).first()).toBeVisible({ timeout: 10_000 })

    await ctx.close()
  })
})
