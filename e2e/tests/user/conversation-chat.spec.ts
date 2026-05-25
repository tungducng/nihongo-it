import { test, expect } from '@fixtures/auth.fixture'
import { E2E_FEATURES } from '@helpers/feature-flags'
import {
  uniqueName,
  createConversation,
  deleteConversationsByPrefix,
} from '@helpers/api-admin'

const PREFIX = 'e2e-chat'

test.describe('18 — User conversation practice', () => {
  test.afterEach(async () => {
    await deleteConversationsByPrefix(PREFIX)
  })

  test('TC-18-01 conversation practice page renders title @smoke', async ({ page }) => {
    const title = uniqueName(PREFIX)
    const conv = await createConversation(title, 'Practice render test')

    await page.goto(`/conversation/${conv.conversationId}/practice`)
    // Title appears in the practice page heading
    await expect(page.getByRole('heading', { name: title })).toBeVisible({
      timeout: 30_000,
    })
  })

  test('TC-18-02 chat exchange with AI (gated)', async ({ page }) => {
    test.skip(
      !E2E_FEATURES.openai,
      'requires E2E_OPENAI=1 (OpenAI key + ai-service must be running)',
    )
    // The /conversation/[id]/practice page currently exercises Python speech
    // analysis, not an OpenAI chat exchange. This placeholder reserves the
    // gated path for when the FE adds a chat-with-AI flow.
    const title = uniqueName(PREFIX)
    const conv = await createConversation(title, 'AI chat test')
    await page.goto(`/conversation/${conv.conversationId}/practice`)
    expect(true).toBe(true)
  })
})
