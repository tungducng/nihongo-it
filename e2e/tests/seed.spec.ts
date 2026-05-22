import { test as setup, expect } from '@playwright/test'
import * as fs from 'fs'
import * as http from 'http'
import * as path from 'path'
import { URL } from 'url'
import { E2E_USERS, E2E_URLS } from '@helpers/feature-flags'
import { upsertUser } from '@helpers/db'

const userAuthFile = path.resolve(__dirname, '..', '.auth', 'user.json')
const adminAuthFile = path.resolve(__dirname, '..', '.auth', 'admin.json')

// Raw Node http POST that tolerates the BE's half-closed chunked response.
// Returns just the status + Set-Cookie headers — that's all we need to build
// storageState. Body is intentionally drained-and-discarded.
function rawLogin(email: string, password: string): Promise<{ status: number; setCookie: string[] }> {
  return new Promise((resolve, reject) => {
    const body = JSON.stringify({ email, password })
    const u = new URL(`${E2E_URLS.gateway}/api/v1/user/auth/login`)
    const req = http.request(
      {
        method: 'POST',
        hostname: u.hostname,
        port: u.port,
        path: u.pathname,
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(body),
        },
      },
      (res) => {
        const setCookie = (res.headers['set-cookie'] ?? []) as string[]
        // Drain and ignore body; the BE half-closes after writing the cookie.
        res.on('data', () => {})
        res.on('end', () => resolve({ status: res.statusCode ?? 0, setCookie }))
        res.on('error', () => resolve({ status: res.statusCode ?? 0, setCookie }))
      },
    )
    req.on('error', reject)
    req.write(body)
    req.end()
  })
}

function parseRefreshToken(setCookie: string[]): string | null {
  for (const c of setCookie) {
    const m = c.match(/refresh_token=([^;]+)/)
    if (m) return m[1]
  }
  return null
}

function writeStorageState(filePath: string, refreshToken: string): void {
  const u = new URL(E2E_URLS.gateway)
  const state = {
    cookies: [
      {
        name: 'refresh_token',
        value: refreshToken,
        domain: u.hostname,
        path: '/api/v1/user/auth',
        expires: Math.floor(Date.now() / 1000) + 14 * 24 * 3600,
        httpOnly: true,
        secure: false,
        sameSite: 'Lax' as const,
      },
    ],
    origins: [],
  }
  fs.mkdirSync(path.dirname(filePath), { recursive: true })
  fs.writeFileSync(filePath, JSON.stringify(state, null, 2))
}

setup.describe.configure({ mode: 'serial' })

async function seedAndLogin(opts: {
  email: string
  password: string
  fullName: string
  isAdmin: boolean
  authFile: string
}): Promise<void> {
  await upsertUser(opts)
  const { status, setCookie } = await rawLogin(opts.email, opts.password)
  expect(status).toBe(200)
  const refreshToken = parseRefreshToken(setCookie)
  expect(refreshToken).toBeTruthy()
  writeStorageState(opts.authFile, refreshToken!)
}

setup('seed: ensure normal user exists + storageState', async () => {
  const { email, password, fullName } = E2E_USERS.user
  await seedAndLogin({ email, password, fullName, isAdmin: false, authFile: userAuthFile })
})

setup('seed: ensure admin user exists + storageState', async () => {
  const { email, password, fullName } = E2E_USERS.admin
  await seedAndLogin({ email, password, fullName, isAdmin: true, authFile: adminAuthFile })
})

setup('seed: log credentials for debugging', async () => {
  console.log(`[seed] user:  ${E2E_USERS.user.email} / ${E2E_USERS.user.password}`)
  console.log(`[seed] admin: ${E2E_USERS.admin.email} / ${E2E_USERS.admin.password}`)
})
