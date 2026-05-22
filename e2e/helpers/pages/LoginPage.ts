import type { Page } from '@playwright/test'
import { expect } from '@playwright/test'
import { BasePage } from './BasePage'
import { SEL } from '../selectors'

export class LoginPage extends BasePage {
  protected readonly path = '/login'

  readonly emailInput = this.page.locator(SEL.auth.emailInput)
  readonly passwordInput = this.page.locator(SEL.auth.passwordInput)
  readonly submitBtn = this.page.locator(SEL.auth.submitBtn)

  constructor(page: Page) {
    super(page)
  }

  async fillCredentials(email: string, password: string): Promise<void> {
    await this.emailInput.fill(email)
    await this.passwordInput.fill(password)
  }

  async submit(): Promise<void> {
    await this.submitBtn.click()
  }

  async login(email: string, password: string): Promise<void> {
    await this.goto()
    await this.fillCredentials(email, password)
    await this.submit()
    // Successful login redirects away from /login. Wait for navigation.
    await this.page.waitForURL((url) => !url.pathname.endsWith('/login'), {
      timeout: 15_000,
    })
  }

  async expectLoginFormVisible(): Promise<void> {
    await expect(this.emailInput).toBeVisible()
    await expect(this.passwordInput).toBeVisible()
    await expect(this.submitBtn).toBeVisible()
  }
}
