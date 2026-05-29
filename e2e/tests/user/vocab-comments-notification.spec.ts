import { test, expect } from '@fixtures/auth.fixture'
import {
  uniqueName,
  createCategory,
  createTopic,
  createVocabulary,
  deleteCategoriesByPrefix,
  deleteTopicsByPrefix,
  deleteVocabularyByPrefix,
  adminApi,
  userApi,
} from '@helpers/api-admin'

const CAT_PREFIX = 'e2e-rcat'
const TOPIC_PREFIX = 'e2e-rtopic'
const VOCAB_PREFIX = 'e2e-rvocab'

test.describe('S5 — Reply notification', () => {
  test.afterEach(async () => {
    await deleteVocabularyByPrefix(VOCAB_PREFIX)
    await deleteTopicsByPrefix(TOPIC_PREFIX)
    await deleteCategoriesByPrefix(CAT_PREFIX)
  })

  test('TC-S5-01 user reply triggers an in-app notification for the parent author @smoke', async () => {
    // Seed vocab as admin
    const catName = uniqueName(CAT_PREFIX)
    const topicName = uniqueName(TOPIC_PREFIX)
    const term = uniqueName(VOCAB_PREFIX)
    const cat = await createCategory(catName, 'S5 cat')
    await createTopic(topicName, 'S5 topic', cat.categoryId)
    const vocab = await createVocabulary(term, 'S5 meaning', topicName)

    const admin = await adminApi()
    const user = await userApi()

    // Admin counts baseline notifications BEFORE — anything new must be ours.
    const before = await admin.get('/api/v1/notify/notifications?page=0&size=50')
    const baselineCommentReplyTotal = (
      before.data.content as Array<{ type: string }>
    ).filter((n) => n.type === 'COMMENT_REPLY').length

    // Admin posts a top-level comment
    const parentContent = `S5 parent ${Date.now()}`
    const parent = await admin.post(
      `/api/v1/learning/vocabulary/${vocab.vocabId}/comments`,
      { content: parentContent },
    )
    const parentId = parent.data.commentId as string
    expect(parentId).toBeTruthy()

    // User replies — should dispatch notification to admin
    await user.post(`/api/v1/learning/vocabulary/${vocab.vocabId}/comments`, {
      content: 'S5 reply from another user',
      parentCommentId: parentId,
    })

    // RestClient timeout is 2s connect / 5s read; give it 3s slack on busy CI
    await new Promise((r) => setTimeout(r, 3000))

    const after = await admin.get('/api/v1/notify/notifications?page=0&size=50')
    const replies = (after.data.content as Array<{
      type: string
      title: string
      actionUrl: string | null
    }>).filter((n) => n.type === 'COMMENT_REPLY')

    expect(replies.length, 'admin should have one new COMMENT_REPLY').toBe(
      baselineCommentReplyTotal + 1,
    )
    const newest = replies[0]
    expect(newest.title).toContain('trả lời bình luận')
    expect(newest.actionUrl).toContain(`/vocabulary/${vocab.vocabId}`)
    expect(newest.actionUrl).toContain(`#comment-`)
  })

  test('TC-S5-02 self-reply does NOT generate a notification', async () => {
    const catName = uniqueName(CAT_PREFIX)
    const topicName = uniqueName(TOPIC_PREFIX)
    const term = uniqueName(VOCAB_PREFIX)
    const cat = await createCategory(catName, 'S5 self cat')
    await createTopic(topicName, 'S5 self topic', cat.categoryId)
    const vocab = await createVocabulary(term, 'S5 self meaning', topicName)

    const admin = await adminApi()

    const before = await admin.get('/api/v1/notify/notifications?page=0&size=50')
    const baseline = (
      before.data.content as Array<{ type: string }>
    ).filter((n) => n.type === 'COMMENT_REPLY').length

    // Admin posts + admin replies to own comment
    const parent = await admin.post(
      `/api/v1/learning/vocabulary/${vocab.vocabId}/comments`,
      { content: `S5 self parent ${Date.now()}` },
    )
    await admin.post(`/api/v1/learning/vocabulary/${vocab.vocabId}/comments`, {
      content: `S5 self reply ${Date.now()}`,
      parentCommentId: parent.data.commentId,
    })

    await new Promise((r) => setTimeout(r, 3000))

    const after = await admin.get('/api/v1/notify/notifications?page=0&size=50')
    const replies = (
      after.data.content as Array<{ type: string }>
    ).filter((n) => n.type === 'COMMENT_REPLY').length

    expect(replies, 'self-reply must NOT add a COMMENT_REPLY notification').toBe(baseline)
  })
})
