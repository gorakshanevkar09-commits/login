import { expect, type Locator, type Page } from '@playwright/test';

export class FlipkartLoginPage {
  readonly page: Page;
  readonly closePopup: Locator;
  readonly loginButton: Locator;
  readonly emailField: Locator;
  readonly passwordField: Locator;
  readonly submitButton: Locator;
  readonly errorMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.closePopup = page.locator('button._2KpZ6l._2doB4z');
    this.loginButton = page.locator('a._1_3w1N, a[href*="/account/login"]');
    this.emailField = page.locator('input[type="text"]');
    this.passwordField = page.locator('input[type="password"]');
    this.submitButton = page.locator('button[type="submit"]');
    this.errorMessage = page.locator('span._2YULOR, div._2YULOR, div._3vhnxf');
  }

  async open() {
    await this.page.goto('/');
    await this.page.waitForLoadState('networkidle');
    return this;
  }

  async closeIntroPopup() {
    if (await this.closePopup.isVisible()) {
      await this.closePopup.click();
    }
    return this;
  }

  async openLoginForm() {
    if (await this.loginButton.isVisible()) {
      await this.loginButton.first().click();
    }
    return this;
  }

  async login(email: string, password: string) {
    await this.emailField.fill(email);
    await this.passwordField.fill(password);
    await this.submitButton.click();
    return this;
  }

  async expectLoginError() {
    await expect(this.errorMessage.first()).toBeVisible({ timeout: 15000 });
    return await this.errorMessage.first().textContent();
  }

  async expectLoggedIn() {
    await expect(this.page.locator('input[title="Search for products, brands and more"]')).toBeVisible({ timeout: 15000 });
  }
}
