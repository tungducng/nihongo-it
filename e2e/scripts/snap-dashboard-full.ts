import { chromium } from '@playwright/test'
import * as path from 'path'

const OUT = path.resolve(__dirname, '..', '..', 'docs', 'mockups', '03-dashboard', 'dashboard-full.png')
const AUTH = path.resolve(__dirname, '..', '.auth', 'user.json')

async function main() {
  const browser = await chromium.launch()
  const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 }, storageState: AUTH })
  const page = await ctx.newPage()
  await page.goto('http://localhost:3000/dashboard', { waitUntil: 'networkidle', timeout: 30_000 })
  await page.waitForTimeout(2500)
  await page.screenshot({ path: OUT, fullPage: true })
  console.log('saved', OUT)
  await browser.close()
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
