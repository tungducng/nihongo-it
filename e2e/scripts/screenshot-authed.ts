/**
 * Screenshot helper — loads the seeded user storageState and captures
 * authenticated pages at 1440×900 into docs/mockups/02-screens-migration/.
 *
 * Run with: npx ts-node scripts/screenshot-authed.ts
 * (or via tsx — the file is plain TS executed from the e2e folder)
 */
import { chromium } from '@playwright/test'
import * as path from 'path'

const OUT_DIR = path.resolve(__dirname, '..', '..', 'docs', 'mockups', '02-screens-migration')
const USER_AUTH = path.resolve(__dirname, '..', '.auth', 'user.json')
const ADMIN_AUTH = path.resolve(__dirname, '..', '.auth', 'admin.json')

const ROUTES = [
  // user
  { role: 'user' as const, route: '/vocabulary', file: 'vocabulary.png' },
  { role: 'user' as const, route: '/flashcards/study', file: 'flashcards-study.png' },
  { role: 'user' as const, route: '/conversation', file: 'conversation.png' },
  { role: 'user' as const, route: '/statistics', file: 'statistics.png' },
  { role: 'user' as const, route: '/profile', file: 'profile.png' },
  // admin
  { role: 'admin' as const, route: '/', file: 'admin-dashboard.png', baseUrl: 'http://localhost:3001' },
  { role: 'admin' as const, route: '/users', file: 'admin-users.png', baseUrl: 'http://localhost:3001' },
  { role: 'admin' as const, route: '/vocabulary', file: 'admin-vocabulary.png', baseUrl: 'http://localhost:3001' },
]

async function main() {
  const browser = await chromium.launch()
  for (const r of ROUTES) {
    const baseUrl = r.baseUrl ?? 'http://localhost:3000'
    const storageState = r.role === 'admin' ? ADMIN_AUTH : USER_AUTH
    const context = await browser.newContext({
      viewport: { width: 1440, height: 900 },
      storageState,
    })
    const page = await context.newPage()
    try {
      await page.goto(`${baseUrl}${r.route}`, { waitUntil: 'networkidle', timeout: 30_000 })
      await page.waitForTimeout(1500)
      const outPath = path.join(OUT_DIR, r.file)
      await page.screenshot({ path: outPath, fullPage: false })
      console.log(`saved ${r.file}`)
    } catch (err) {
      console.error(`FAILED ${r.file}:`, err instanceof Error ? err.message : err)
    } finally {
      await context.close()
    }
  }
  await browser.close()
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
