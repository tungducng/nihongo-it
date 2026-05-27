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

const CAT_PREFIX = 'e2e-ccat'
const TOPIC_PREFIX = 'e2e-ctopic'
const VOCAB_PREFIX = 'e2e-cvocab'

test.describe('S4 — Vocab comment thread', () => {
  test.afterEach(async () => {
    // Comments are FK-CASCADE-deleted with the vocab row, so we just need
    // to wipe the category tree.
    await deleteVocabularyByPrefix(VOCAB_PREFIX)
    await deleteTopicsByPrefix(TOPIC_PREFIX)
    await deleteCategoriesByPrefix(CAT_PREFIX)
  })

  test('TC-S4-01 post a top-level comment and see it appear @smoke', async ({ page }) => {
    const catName = uniqueName(CAT_PREFIX)
    const topicName = uniqueName(TOPIC_PREFIX)
    const term = uniqueName(VOCAB_PREFIX)
    const cat = await createCategory(catName, 'Comment-test cat')
    await createTopic(topicName, 'Comment-test topic', cat.categoryId)
    const vocab = await createVocabulary(term, 'Comment-test meaning', topicName)

    await page.goto(`/vocabulary/${vocab.vocabId}`)
    await expect(page.getByText(term, { exact: true }).first()).toBeVisible({
      timeout: 30_000,
    })

    const composer = page.getByPlaceholder('Viết bình luận của bạn...')
    await composer.waitFor({ timeout: 10_000 })

    const content = `S4 smoke ${Date.now()} — デプロイ vs リリース?`
    await composer.fill(content)
    await page.getByRole('button', { name: 'Gửi' }).click()

    // Toast appears + comment visible in the list
    await expect(page.getByText('Đã đăng bình luận')).toBeVisible({ timeout: 10_000 })
    await expect(page.getByText(content)).toBeVisible({ timeout: 5_000 })

    // Header count is now "1 bình luận"
    await expect(page.getByText(/1 bình luận/)).toBeVisible({ timeout: 5_000 })
  })

  test('TC-S4-02 like + unlike toggles the counter and persists across reload', async ({ page }) => {
    const catName = uniqueName(CAT_PREFIX)
    const topicName = uniqueName(TOPIC_PREFIX)
    const term = uniqueName(VOCAB_PREFIX)
    const cat = await createCategory(catName, 'Like-test cat')
    await createTopic(topicName, 'Like-test topic', cat.categoryId)
    const vocab = await createVocabulary(term, 'Like-test meaning', topicName)

    await page.goto(`/vocabulary/${vocab.vocabId}`)
    await expect(page.getByText(term, { exact: true }).first()).toBeVisible({ timeout: 30_000 })

    const content = `S4 like ${Date.now()}`
    await page.getByPlaceholder('Viết bình luận của bạn...').fill(content)
    await page.getByRole('button', { name: 'Gửi' }).click()
    await expect(page.getByText(content)).toBeVisible({ timeout: 10_000 })

    // Like — aria-label flips from "Thích" → "Bỏ thích" (use getByLabel which
    // matches aria-label even when aria-pressed makes the role ambiguous).
    await page.getByLabel('Thích', { exact: true }).first().click()
    await expect(page.getByLabel('Bỏ thích', { exact: true }).first()).toBeVisible({
      timeout: 5_000,
    })

    // Reload — like state persists
    await page.reload()
    await expect(page.getByText(content)).toBeVisible({ timeout: 30_000 })
    await expect(page.getByLabel('Bỏ thích', { exact: true }).first()).toBeVisible({
      timeout: 10_000,
    })

    // Unlike
    await page.getByLabel('Bỏ thích', { exact: true }).first().click()
    await expect(page.getByLabel('Thích', { exact: true }).first()).toBeVisible({
      timeout: 5_000,
    })
  })
})
