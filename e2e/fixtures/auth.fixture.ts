import { test as base, expect } from '@playwright/test'
import { LoginPage } from '@pages/LoginPage'

// Re-export Playwright primitives plus our POM fixtures. Tests import from this
// module instead of '@playwright/test' so all custom fixtures stay discoverable.

type Fixtures = {
  loginPage: LoginPage
}

export const test = base.extend<Fixtures>({
  loginPage: async ({ page }, use) => {
    await use(new LoginPage(page))
  },
})

export { expect }
