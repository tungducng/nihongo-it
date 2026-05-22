import type { Page, Locator } from '@playwright/test'
import { expect } from '@playwright/test'

export abstract class BasePage {
  constructor(protected readonly page: Page) {}

  protected abstract readonly path: string

  async goto(): Promise<void> {
    await this.page.goto(this.path)
    await this.waitForReady()
  }

  // Override in subclasses if a more specific readiness check is needed.
  async waitForReady(): Promise<void> {
    await this.page.waitForLoadState('domcontentloaded')
  }

  toastSuccess(text?: string | RegExp): Locator {
    const loc = this.page.locator('[data-sonner-toast][data-type="success"]')
    return text ? loc.filter({ hasText: text }) : loc
  }

  toastError(text?: string | RegExp): Locator {
    const loc = this.page.locator('[data-sonner-toast][data-type="error"]')
    return text ? loc.filter({ hasText: text }) : loc
  }

  async expectVisible(locator: Locator): Promise<void> {
    await expect(locator).toBeVisible()
  }
}
