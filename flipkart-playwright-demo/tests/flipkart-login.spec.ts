import { test, expect } from '@playwright/test';
import { FlipkartLoginPage } from '../src/pages/FlipkartLoginPage';

test.describe('Flipkart login demo', () => {
  test('shows an error message for invalid credentials', async ({ page }) => {
    const loginPage = new FlipkartLoginPage(page);

    await loginPage.open();
    await loginPage.closeIntroPopup();
    await loginPage.openLoginForm();
    await loginPage.login('invalid-user@example.com', 'BadPassword123');

    const errorText = await loginPage.expectLoginError();
    expect(errorText).toBeTruthy();
    expect(errorText?.toLowerCase()).toContain('enter');
  });

  test('can login with valid Flipkart credentials from environment variables', async ({ page }) => {
    const username = process.env.FLIPKART_USER;
    const password = process.env.FLIPKART_PASSWORD;
    test.skip(!username || !password, 'Set FLIPKART_USER and FLIPKART_PASSWORD environment variables to run this test.');

    const loginPage = new FlipkartLoginPage(page);

    await loginPage.open();
    await loginPage.closeIntroPopup();
    await loginPage.openLoginForm();
    await loginPage.login(username!, password!);

    await loginPage.expectLoggedIn();
  });
});
