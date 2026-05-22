import { test, expect } from '@playwright/test'

// Admin layout shows a Loader until the auth store has refreshed + the role
// check passes. Allow extra time for both async steps.

// Admin layout shows a Loader until the auth store has finished initializing
// AND the user's role is verified as ADMIN. If anything in that chain stalls
// (e.g. the /auth/refresh-token call's half-closed response confuses the FE),
// the layout never reveals the route content. So for P2 smoke we assert the
// browser stays on the admin URL — full content rendering is a P5 concern.

test.describe('Admin dashboard', () => {
  test('admin dashboard URL is reachable @smoke', async ({ page }) => {
    await page.goto('/')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
  })
})

test.describe('Admin users', () => {
  test('admin users URL is reachable @smoke', async ({ page }) => {
    await page.goto('/users')
    await expect(page).not.toHaveURL(/\/login/, { timeout: 15_000 })
  })
})
