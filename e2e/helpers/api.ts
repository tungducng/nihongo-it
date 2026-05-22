import axios, { AxiosError, AxiosInstance } from 'axios'
import { E2E_URLS } from './feature-flags'

export interface LoginResponse {
  token: string
  userId: string
  email: string
  role?: string
  refreshToken?: string | null
}

export function createApi(token?: string): AxiosInstance {
  const api = axios.create({
    baseURL: E2E_URLS.gateway,
    timeout: 15_000,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    // Don't throw on 4xx — let test code inspect status itself.
    validateStatus: () => true,
  })
  return api
}

export async function signup(
  email: string,
  password: string,
  fullName: string,
): Promise<number> {
  const api = createApi()
  // Send currentLevel/jlptGoal explicitly — Jackson can't resolve the Kotlin
  // default values after the @param:property annotation flag change.
  const res = await api.post('/api/v1/user/auth/signup', {
    email,
    password,
    fullName,
    currentLevel: 'N5',
    jlptGoal: 'N3',
  })
  return res.status
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const api = createApi()
  const res = await api.post<LoginResponse>('/api/v1/user/auth/login', {
    email,
    password,
  })
  if (res.status !== 200) {
    throw new Error(
      `Login failed (${res.status}) for ${email}: ${JSON.stringify(res.data)}`,
    )
  }
  return res.data
}

export function summarizeError(err: unknown): string {
  if (err instanceof AxiosError) {
    return `HTTP ${err.response?.status ?? '?'} — ${JSON.stringify(err.response?.data ?? err.message)}`
  }
  return String(err)
}
