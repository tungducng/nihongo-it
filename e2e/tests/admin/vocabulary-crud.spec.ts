import { test, expect } from '@playwright/test'
import {
  uniqueName,
  createCategory,
  createTopic,
  deleteCategoriesByPrefix,
  deleteTopicsByPrefix,
  deleteVocabularyByPrefix,
  listVocabularyPage,
} from '@helpers/api-admin'

const CAT_PREFIX = 'e2e-vcat'
const TOPIC_PREFIX = 'e2e-vtopic'
const VOCAB_PREFIX = 'e2e-vocab'

test.describe('09 — Admin vocabulary CRUD', () => {
  test.afterEach(async () => {
    // Cascade matters: vocab → topic → category.
    await deleteVocabularyByPrefix(VOCAB_PREFIX)
    await deleteTopicsByPrefix(TOPIC_PREFIX)
    await deleteCategoriesByPrefix(CAT_PREFIX)
  })

  test('TC-09-01 create vocabulary in seeded topic @smoke', async ({ page }) => {
    const catName = uniqueName(CAT_PREFIX)
    const topicName = uniqueName(TOPIC_PREFIX)
    const term = uniqueName(VOCAB_PREFIX)
    const cat = await createCategory(catName, 'Parent cat')
    await createTopic(topicName, 'Parent topic', cat.categoryId)

    await page.goto('/vocabulary')
    await expect(
      page.getByRole('heading', { name: 'Từ vựng', exact: true }),
    ).toBeVisible({ timeout: 30_000 })

    await page.getByRole('button', { name: /Tạo mới|Thêm mới/i }).first().click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()

    // Use IDs inside the dialog — "Từ vựng" matches both dialog heading + the
    // term input, so getByLabel would be ambiguous.
    await dialog.locator('#term').fill(term)
    await dialog.locator('#meaning').fill('Created by E2E')
    await dialog.locator('#topicName').click()
    await page.getByRole('option', { name: topicName }).click()
    await dialog.getByRole('button', { name: 'Tạo mới' }).click()

    await expect(page.getByText(/Đã tạo từ vựng/i)).toBeVisible({ timeout: 10_000 })

    // API echo
    const items = await listVocabularyPage()
    expect(items.some((v) => v.term === term)).toBe(true)
  })

  test('TC-09-02 edit then delete vocabulary via row menu', async ({ page }) => {
    const catName = uniqueName(CAT_PREFIX)
    const topicName = uniqueName(TOPIC_PREFIX)
    const term = uniqueName(VOCAB_PREFIX)
    const cat = await createCategory(catName, 'Parent cat')
    await createTopic(topicName, 'Parent topic', cat.categoryId)

    const { createVocabulary } = await import('@helpers/api-admin')
    await createVocabulary(term, 'Initial meaning', topicName)

    await page.goto('/vocabulary')
    await expect(
      page.getByRole('heading', { name: 'Từ vựng', exact: true }),
    ).toBeVisible({ timeout: 30_000 })

    // Edit
    const row = page.getByRole('row', { name: new RegExp(term) })
    await expect(row).toBeVisible({ timeout: 15_000 })
    await row.getByRole('button').last().click()
    await page.getByRole('menuitem', { name: /Sửa/i }).click()

    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()
    await dialog.locator('#meaning').fill('Updated meaning')
    await dialog.getByRole('button', { name: 'Cập nhật' }).click()
    await expect(page.getByText(/Đã cập nhật từ vựng/i)).toBeVisible({ timeout: 10_000 })

    // Delete
    await row.getByRole('button').last().click()
    await page.getByRole('menuitem', { name: /Xo[áạ]/i }).click()
    await page.getByRole('alertdialog').getByRole('button', { name: /Xo[áạ]/i }).click()
    await expect(page.getByText(/Đã xo[áạ] từ vựng/i)).toBeVisible({ timeout: 10_000 })

    const items = await listVocabularyPage()
    expect(items.some((v) => v.term === term)).toBe(false)
  })
})
