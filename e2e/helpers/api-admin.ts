import axios, { type AxiosInstance } from 'axios'
import { E2E_URLS, E2E_USERS } from './feature-flags'

// Cached admin access token — login once per process. The BE rotation toggle
// (APP_REFRESH_TOKEN_ROTATION_ENABLED=false in E2E mode) keeps this valid for
// the lifetime of the test run.
let cachedToken: string | null = null

export async function adminAccessToken(): Promise<string> {
  if (cachedToken) return cachedToken
  const resp = await axios.post<{ token: string }>(
    `${E2E_URLS.gateway}/api/v1/user/auth/login`,
    { email: E2E_USERS.admin.email, password: E2E_USERS.admin.password },
    { withCredentials: true, validateStatus: () => true },
  )
  if (resp.status !== 200 || !resp.data.token) {
    throw new Error(`adminAccessToken: login failed ${resp.status}`)
  }
  cachedToken = resp.data.token
  return cachedToken
}

// Build an axios instance pre-authed as admin. Use for setup/cleanup helpers
// that bypass the FE and talk directly to the gateway.
export async function adminApi(): Promise<AxiosInstance> {
  const token = await adminAccessToken()
  return axios.create({
    baseURL: E2E_URLS.gateway,
    headers: { Authorization: `Bearer ${token}` },
    timeout: 15_000,
    validateStatus: () => true,
  })
}

// === Categories ===

export interface CategoryDto {
  categoryId: string
  name: string
  meaning: string
  displayOrder?: number
  isActive?: boolean
}

export async function listCategories(): Promise<CategoryDto[]> {
  const api = await adminApi()
  const r = await api.get<CategoryDto[]>('/api/v1/learning/categories')
  if (r.status >= 400) throw new Error(`listCategories ${r.status}`)
  return r.data
}

export async function createCategory(
  name: string,
  meaning: string,
  displayOrder = 0,
): Promise<CategoryDto> {
  const api = await adminApi()
  // Send isActive explicitly. Jackson + Kotlin defaults are not picked up
  // after the -Xannotation-default-target=param-property compiler flag was
  // added, so primitive non-null fields with Kotlin defaults need to be sent.
  const r = await api.post<CategoryDto>('/api/v1/learning/categories', {
    name,
    meaning,
    description: '',
    displayOrder,
    isActive: true,
  })
  if (r.status >= 400) throw new Error(`createCategory ${r.status}: ${JSON.stringify(r.data)}`)
  return r.data
}

export async function deleteCategory(categoryId: string): Promise<void> {
  const api = await adminApi()
  const r = await api.delete(`/api/v1/learning/categories/${categoryId}`)
  if (r.status >= 400 && r.status !== 404) {
    throw new Error(`deleteCategory ${r.status}`)
  }
}

export async function deleteCategoriesByPrefix(prefix: string): Promise<void> {
  const all = await listCategories()
  for (const c of all.filter((x) => x.name.startsWith(prefix))) {
    await deleteCategory(c.categoryId)
  }
}

// === Topics ===

export interface TopicDto {
  topicId: string
  name: string
  meaning: string
  categoryId: string
  displayOrder?: number
  isActive?: boolean
}

export async function listTopics(): Promise<TopicDto[]> {
  const api = await adminApi()
  const r = await api.get<TopicDto[]>('/api/v1/learning/topics')
  if (r.status >= 400) throw new Error(`listTopics ${r.status}`)
  return r.data
}

export async function createTopic(
  name: string,
  meaning: string,
  categoryId: string,
  displayOrder = 0,
): Promise<TopicDto> {
  const api = await adminApi()
  const r = await api.post<TopicDto>('/api/v1/learning/topics', {
    name,
    meaning,
    categoryId,
    displayOrder,
    isActive: true,
  })
  if (r.status >= 400) throw new Error(`createTopic ${r.status}: ${JSON.stringify(r.data)}`)
  return r.data
}

export async function deleteTopic(topicId: string): Promise<void> {
  const api = await adminApi()
  const r = await api.delete(`/api/v1/learning/topics/${topicId}`)
  if (r.status >= 400 && r.status !== 404) {
    throw new Error(`deleteTopic ${r.status}`)
  }
}

export async function deleteTopicsByPrefix(prefix: string): Promise<void> {
  const all = await listTopics()
  for (const t of all.filter((x) => x.name.startsWith(prefix))) {
    await deleteTopic(t.topicId)
  }
}

/** Globally unique-ish suffix so parallel runs don't clash on UNIQUE constraints. */
export function uniqueName(prefix: string): string {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 1000)}`
}

// === Throwaway users (for admin user-management tests) ===

export async function createThrowawayUser(email: string, password: string, fullName: string): Promise<string> {
  // Signup via gateway (public). Returns userId via DB lookup.
  const lr = await axios.post(
    `${E2E_URLS.gateway}/api/v1/user/auth/signup`,
    { email, password, fullName, currentLevel: 'N5', jlptGoal: 'N3' },
    { validateStatus: () => true },
  )
  // Already-exists is acceptable; we'll look up the id below.
  if (lr.status !== 201 && lr.status !== 200 && lr.status !== 400) {
    throw new Error(`createThrowawayUser signup ${lr.status}: ${JSON.stringify(lr.data)}`)
  }
  // Mark verified + active via SQL.
  const { withClient } = await import('./db')
  return await withClient('user', async (c) => {
    await c.query(
      `UPDATE users SET is_email_verified = true, is_active = true WHERE email = $1`,
      [email],
    )
    const r = await c.query<{ user_id: string }>(
      `SELECT user_id FROM users WHERE email = $1`,
      [email],
    )
    if (r.rowCount === 0) throw new Error(`user ${email} not found after signup`)
    return r.rows[0].user_id
  })
}

export async function deleteThrowawayUser(email: string): Promise<void> {
  const { withClient } = await import('./db')
  await withClient('user', async (c) => {
    await c.query(`DELETE FROM users WHERE email = $1`, [email])
  })
}

// === Vocabulary ===

export interface VocabularyItem {
  vocabId: string
  term: string
  meaning: string
  pronunciation?: string
  jlptLevel: string
  topicId?: string
  topicName?: string
}

export async function createVocabulary(
  term: string,
  meaning: string,
  topicName: string,
  jlptLevel: 'N1' | 'N2' | 'N3' | 'N4' | 'N5' = 'N5',
): Promise<VocabularyItem> {
  const api = await adminApi()
  const r = await api.post<VocabularyItem>('/api/v1/learning/admin/vocabulary', {
    term,
    meaning,
    topicName,
    jlptLevel,
  })
  if (r.status >= 400) throw new Error(`createVocabulary ${r.status}: ${JSON.stringify(r.data)}`)
  return r.data
}

export async function deleteVocabulary(vocabId: string): Promise<void> {
  // First clean up references in saved_vocabulary + flashcards so the
  // admin delete doesn't trip an FK constraint.
  const { withClient } = await import('./db')
  await withClient('learning', async (c) => {
    await c.query(`DELETE FROM saved_vocabulary WHERE vocab_id = $1`, [vocabId])
    await c.query(`DELETE FROM flashcards WHERE vocabulary_id = $1`, [vocabId])
  })
  const api = await adminApi()
  const r = await api.delete(`/api/v1/learning/admin/vocabulary/${vocabId}`)
  if (r.status >= 400 && r.status !== 404) {
    throw new Error(`deleteVocabulary ${r.status}`)
  }
}

export async function listVocabularyPage(): Promise<VocabularyItem[]> {
  const api = await adminApi()
  const r = await api.get<{ content: VocabularyItem[] }>(
    '/api/v1/learning/admin/vocabulary?page=0&size=100',
  )
  if (r.status >= 400) throw new Error(`listVocabularyPage ${r.status}`)
  return r.data.content ?? []
}

export async function deleteVocabularyByPrefix(prefix: string): Promise<void> {
  const items = await listVocabularyPage()
  for (const v of items.filter((x) => x.term.startsWith(prefix))) {
    await deleteVocabulary(v.vocabId)
  }
}
