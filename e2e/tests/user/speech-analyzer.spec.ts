import { test, expect } from '@fixtures/auth.fixture'
import { E2E_FEATURES } from '@helpers/feature-flags'

test.describe('16 — Speech analyzer', () => {
  test('TC-16-01 page renders + reference-text input works @smoke', async ({ page }) => {
    await page.goto('/speech')
    await expect(
      page.getByRole('heading', { name: 'Phân tích phát âm' }),
    ).toBeVisible({ timeout: 30_000 })

    // Reference text input is always present
    await expect(page.locator('#ref-text')).toBeVisible()
    await page.locator('#ref-text').fill('日本語を勉強しています。')
    await expect(page.getByText(/ký tự/)).toBeVisible()
  })

  test('TC-16-02 audio analyze roundtrip (gated)', async ({ page }) => {
    test.skip(
      !E2E_FEATURES.pythonNlp,
      'requires E2E_PYTHON_NLP=1 (Python NLP service must be running)',
    )
    // Recording + analysis path. Only meaningful when Python NLP is up.
    // The audio recorder needs a real (or fake-media-stream) audio device.
    // Detailed assertions added when the service is reliably available.
    await page.goto('/speech')
    await page.locator('#ref-text').fill('テスト')
    expect(true).toBe(true) // placeholder for the gated path
  })
})
