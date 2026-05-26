/**
 * Populate the seed user with realistic data so the /dashboard route shows
 * non-empty cards (due flashcards, suggested vocab, conversation of the day,
 * heatmap activity). Idempotent — re-running just no-ops on conflicts.
 */
import * as dotenv from 'dotenv'
import * as path from 'path'

// Load .env the same way playwright.config.ts does so E2E_ADMIN_PASSWORD etc.
// resolve to the same values the playwright suite uses.
dotenv.config({ path: path.resolve(__dirname, '..', '.env') })

import {
  createCategory,
  createTopic,
  createVocabulary,
  createConversation,
  createFlashcardFromVocab,
  deleteCategoriesByPrefix,
  deleteTopicsByPrefix,
  deleteVocabularyByPrefix,
  deleteConversationsByPrefix,
} from '../helpers/api-admin'

const VOCAB_DEFS: Array<{ term: string; meaning: string; jlpt: 'N1' | 'N2' | 'N3' | 'N4' | 'N5' }> = [
  { term: 'デプロイ', meaning: 'triển khai · deployment', jlpt: 'N3' },
  { term: '会議', meaning: 'cuộc họp', jlpt: 'N4' },
  { term: '仕様書', meaning: 'tài liệu đặc tả', jlpt: 'N2' },
  { term: '不具合', meaning: 'lỗi / bug', jlpt: 'N3' },
  { term: '実装', meaning: 'triển khai chức năng (implement)', jlpt: 'N2' },
  { term: '構築', meaning: 'xây dựng (hệ thống)', jlpt: 'N2' },
  { term: '障害', meaning: 'sự cố (incident)', jlpt: 'N2' },
]

const PREFIX = 'demo-dashboard'

async function main() {
  // Clean slate so the script is idempotent
  console.log('cleaning previous demo data...')
  await deleteVocabularyByPrefix(PREFIX)
  await deleteConversationsByPrefix(PREFIX)
  await deleteTopicsByPrefix(PREFIX)
  await deleteCategoriesByPrefix(PREFIX)

  console.log('creating category + topic...')
  const cat = await createCategory(`${PREFIX}-IT`, 'Từ vựng IT chung')
  const topic = await createTopic(`${PREFIX}-Backend`, 'Phần backend', cat.categoryId)

  console.log(`creating ${VOCAB_DEFS.length} vocabulary entries...`)
  const vocabs = []
  for (const v of VOCAB_DEFS) {
    const created = await createVocabulary(`${PREFIX}-${v.term}`, v.meaning, topic.name, v.jlpt)
    vocabs.push(created)
  }

  console.log('creating 1 conversation...')
  await createConversation(`${PREFIX}-コードレビュー`, 'Code review — báo cáo & feedback', 'N3')

  console.log('creating flashcards for the seed user (3 of them)...')
  for (const v of vocabs.slice(0, 3)) {
    try {
      await createFlashcardFromVocab(v.vocabId)
    } catch (err) {
      console.warn(`flashcard creation skipped for ${v.term}:`, (err as Error).message)
    }
  }

  console.log('done')
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
