import { test, expect } from '@fixtures/auth.fixture'

test.describe('05 — Public tools', () => {
  test('TC-05-01 furigana tool renders @smoke', async ({ page }) => {
    await page.goto('/furigana')
    await expect(
      page.getByRole('heading', { name: '日本語ふりがな生成器', exact: true }),
    ).toBeVisible({ timeout: 15_000 })
  })

  test('TC-05-02 translation tool renders', async ({ page }) => {
    await page.goto('/translation')
    await expect(
      page.getByRole('heading', { name: 'Công cụ dịch thuật', exact: true }),
    ).toBeVisible({ timeout: 15_000 })
  })
})
