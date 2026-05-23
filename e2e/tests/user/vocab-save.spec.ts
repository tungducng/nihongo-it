import { test, expect } from '@fixtures/auth.fixture'
import {
  uniqueName,
  createCategory,
  createTopic,
  createVocabulary,
  deleteCategoriesByPrefix,
  deleteTopicsByPrefix,
  deleteVocabularyByPrefix,
} from '@helpers/api-admin'

const CAT_PREFIX = 'e2e-svcat'
const TOPIC_PREFIX = 'e2e-svtopic'
const VOCAB_PREFIX = 'e2e-svvocab'

test.describe('13 — User vocab save/unsave', () => {
  test.afterEach(async () => {
    await deleteVocabularyByPrefix(VOCAB_PREFIX)
    await deleteTopicsByPrefix(TOPIC_PREFIX)
    await deleteCategoriesByPrefix(CAT_PREFIX)
  })

  test('TC-13-01 save vocabulary then verify in saved list @smoke', async ({ page }) => {
    // Setup test data via admin API
    const catName = uniqueName(CAT_PREFIX)
    const topicName = uniqueName(TOPIC_PREFIX)
    const term = uniqueName(VOCAB_PREFIX)
    const cat = await createCategory(catName, 'Save-test cat')
    await createTopic(topicName, 'Save-test topic', cat.categoryId)
    await createVocabulary(term, 'E2E test meaning', topicName)

    // Navigate to vocab list, search for the new term
    await page.goto('/vocabulary')
    await expect(
      page.getByRole('heading', { name: 'Từ vựng', exact: true }),
    ).toBeVisible({ timeout: 30_000 })

    // Search by keyword — VocabularyFilter has a debounced search input
    await page.getByPlaceholder('Nhập từ hoặc nghĩa...').fill(term)

    // The vocab card with our term should appear
    const card = page.locator('a').filter({ hasText: term }).first()
    await expect(card).toBeVisible({ timeout: 15_000 })

    // Click the save button inside that card (aria-label="Lưu từ vựng")
    await card.getByRole('button', { name: 'Lưu từ vựng' }).click()
    // The button flips to "Bỏ lưu" after a successful save
    await expect(card.getByRole('button', { name: 'Bỏ lưu' })).toBeVisible({
      timeout: 10_000,
    })

    // Go to saved list and verify the item is there
    await page.goto('/vocabulary/saved')
    await expect(
      page.getByRole('heading', { name: 'Từ vựng đã lưu', exact: true }),
    ).toBeVisible({ timeout: 15_000 })
    await expect(page.locator('a').filter({ hasText: term }).first()).toBeVisible({
      timeout: 15_000,
    })
  })
})
