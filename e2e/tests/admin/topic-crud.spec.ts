import { test, expect } from '@playwright/test'
import {
  uniqueName,
  createCategory,
  deleteCategoriesByPrefix,
  deleteTopicsByPrefix,
  listTopics,
} from '@helpers/api-admin'

const CAT_PREFIX = 'e2e-tcat'
const TOPIC_PREFIX = 'e2e-topic'

test.describe('08 — Admin topic CRUD', () => {
  test.afterEach(async () => {
    // Topics must be deleted before their parent category.
    await deleteTopicsByPrefix(TOPIC_PREFIX)
    await deleteCategoriesByPrefix(CAT_PREFIX)
  })

  test('TC-08-01 create topic in seeded category @smoke', async ({ page }) => {
    const catName = uniqueName(CAT_PREFIX)
    const topicName = uniqueName(TOPIC_PREFIX)
    const category = await createCategory(catName, 'Parent cat')

    await page.goto('/topics')
    await expect(
      page.getByRole('heading', { name: 'Chủ đề', exact: true }),
    ).toBeVisible({ timeout: 30_000 })

    await page.getByRole('button', { name: /Tạo mới/i }).first().click()
    await expect(
      page.getByRole('dialog').getByText('Tạo chủ đề mới'),
    ).toBeVisible()

    // Radix Select doesn't use a native <select>. Click the trigger then the item.
    await page.locator('#categoryId').click()
    await page.getByRole('option', { name: catName }).click()
    await page.getByLabel('Tên chủ đề').fill(topicName)
    await page.getByLabel('Nghĩa').fill('Created by E2E')
    await page.getByRole('dialog').getByRole('button', { name: 'Tạo mới' }).click()

    await expect(page.getByText(/Đã tạo chủ đề/i)).toBeVisible({ timeout: 10_000 })

    // API echo
    const all = await listTopics()
    const found = all.find((t) => t.name === topicName)
    expect(found).toBeTruthy()
    expect(found?.categoryId).toBe(category.categoryId)
  })

  test('TC-08-02 delete topic via row menu', async ({ page }) => {
    const catName = uniqueName(CAT_PREFIX)
    const topicName = uniqueName(TOPIC_PREFIX)
    const cat = await createCategory(catName, 'Parent')
    // Seed topic via API for delete test
    const { createTopic } = await import('@helpers/api-admin')
    await createTopic(topicName, 'Will be deleted', cat.categoryId)

    await page.goto('/topics')
    const row = page.getByRole('row', { name: new RegExp(topicName) })
    await expect(row).toBeVisible({ timeout: 10_000 })
    await row.getByRole('button').last().click()
    await page.getByRole('menuitem', { name: /Xo[áạ]/i }).click()
    await page.getByRole('alertdialog').getByRole('button', { name: /Xo[áạ]/i }).click()

    await expect(page.getByText(/Đã xo[áạ]/i)).toBeVisible({ timeout: 10_000 })
    await expect(page.getByText(topicName, { exact: true })).toHaveCount(0)

    const all = await listTopics()
    expect(all.some((t) => t.name === topicName)).toBe(false)
  })
})
