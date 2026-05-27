import { chromium } from '@playwright/test'
import * as path from 'path'

const AUTH = path.resolve(__dirname, '..', '.auth', 'user.json')
const OUT_DIR = path.resolve(__dirname, '..', '..', 'docs', 'mockups', '04-vocab-comments')
const VOCAB_ID = process.env.E2E_DEMO_VOCAB_ID ?? '63cad698-7a44-4b53-9d34-5b8811de2b8c'

async function main() {
  const browser = await chromium.launch()
  const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 }, storageState: AUTH })
  const page = await ctx.newPage()
  await page.goto(`http://localhost:3000/vocabulary/${VOCAB_ID}`, {
    waitUntil: 'domcontentloaded',
    timeout: 60_000,
  })
  await page.waitForTimeout(6000)
  await page.screenshot({ path: path.join(OUT_DIR, 'detail.png'), fullPage: true })
  console.log('saved detail.png')
  await browser.close()
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
