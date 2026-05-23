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
        const status = res.statusCode ?? 0
        // BE half-closes the chunked response without a terminator, so we
        // treat 'aborted' the same as 'end' — at this point Set-Cookie was
        // already received in the headers (which is all we need).
        const settle = () => resolve({ status, setCookie })
        res.on('data', () => {})
        res.on('end', settle)
        res.on('aborted', settle)
        res.on('error', settle)
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

// Hybrid login: rawLogin (Node http, tolerant of BE's half-closed chunked
// response) gets the refresh_token cookie value. We then inject it as a real
// browser cookie via context.addCookies. The browser's fetch is tolerant of
// half-closed responses, so the FE's /auth/refresh-token call on app init
// succeeds normally and populates the auth store.
async function seedAndCaptureBrowserState(
  browser: import('@playwright/test').Browser,
  opts: {
    email: string
    password: string
    fullName: string
    isAdmin: boolean
    appUrl: string
    authFile: string
  },
): Promise<void> {
  await upsertUser(opts)

  // Get a real refresh_token from the BE via tolerant Node http.
  const { status, setCookie } = await rawLogin(opts.email, opts.password)
  expect(status).toBe(200)
  const refreshToken = parseRefreshToken(setCookie)
  expect(refreshToken).toBeTruthy()

  // Build a browser context with the cookie pre-set on the gateway origin.
  // Cookies on localhost are shared across ports, so :8080 cookie is visible
  // to XHRs from :3000 / :3001 when withCredentials is enabled.
  const ctx = await browser.newContext({
    storageState: { cookies: [], origins: [] },
    baseURL: opts.appUrl,
  })
  // Single cookie at path '/' covers BOTH the FE proxy.ts gate (which only
  // checks presence of `refresh_token`) AND the /auth/refresh-token XHR call
  // that browser sends to gateway:8080. Duplicating the cookie at multiple
  // paths sends two Cookie headers which can confuse middleware that picks
  // the "wrong" one.
  const expires = Math.floor(Date.now() / 1000) + 14 * 24 * 3600
  await ctx.addCookies([
    {
      name: 'refresh_token',
      value: refreshToken!,
      domain: 'localhost',
      path: '/',
      httpOnly: true,
      secure: false,
      sameSite: 'Lax',
      expires,
    },
  ])

  const page = await ctx.newPage()
  try {
    // Visit the app root — the FE will fire /auth/refresh-token, the BE will
    // return a fresh access token, and the auth store populates. We wait for
    // an indicator that initialization is complete: the page is NOT at /login.
    await page.goto('/')
    await page.waitForURL((url) => !url.pathname.endsWith('/login'), {
      timeout: 20_000,
    })
    await ctx.storageState({ path: opts.authFile })
  } finally {
    await ctx.close()
  }
}

setup('seed: ensure normal user exists + storageState', async ({ browser }) => {
  const { email, password, fullName } = E2E_USERS.user
  await seedAndCaptureBrowserState(browser, {
    email,
    password,
    fullName,
    isAdmin: false,
    appUrl: E2E_URLS.userApp,
    authFile: userAuthFile,
  })
})

setup('seed: ensure admin user exists + storageState', async ({ browser }) => {
  const { email, password, fullName } = E2E_USERS.admin
  await seedAndCaptureBrowserState(browser, {
    email,
    password,
    fullName,
    isAdmin: true,
    appUrl: E2E_URLS.adminApp,
    authFile: adminAuthFile,
  })
})

setup('seed: log credentials for debugging', async () => {
  console.log(`[seed] user:  ${E2E_USERS.user.email} / ${E2E_USERS.user.password}`)
  console.log(`[seed] admin: ${E2E_USERS.admin.email} / ${E2E_USERS.admin.password}`)
})
