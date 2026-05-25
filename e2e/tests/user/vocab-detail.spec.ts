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

const CAT_PREFIX = 'e2e-dcat'
const TOPIC_PREFIX = 'e2e-dtopic'
const VOCAB_PREFIX = 'e2e-dvocab'

test.describe('15 — User vocab detail page', () => {
  test.afterEach(async () => {
    await deleteVocabularyByPrefix(VOCAB_PREFIX)
    await deleteTopicsByPrefix(TOPIC_PREFIX)
    await deleteCategoriesByPrefix(CAT_PREFIX)
  })

  test('TC-15-01 vocab detail renders + save toggle @smoke', async ({ page }) => {
    const catName = uniqueName(CAT_PREFIX)
    const topicName = uniqueName(TOPIC_PREFIX)
    const term = uniqueName(VOCAB_PREFIX)
    const cat = await createCategory(catName, 'Detail-test cat')
    await createTopic(topicName, 'Detail-test topic', cat.categoryId)
    const vocab = await createVocabulary(term, 'Detail meaning', topicName)

    await page.goto(`/vocabulary/${vocab.vocabId}`)
    await expect(page.getByText(term, { exact: true }).first()).toBeVisible({
      timeout: 30_000,
    })
    await expect(page.getByText('Detail meaning')).toBeVisible({ timeout: 10_000 })

    // Save toggle
    await page.getByRole('button', { name: 'Lưu từ' }).click()
    await expect(page.getByRole('button', { name: 'Bỏ lưu' })).toBeVisible({
      timeout: 10_000,
    })
  })
})
