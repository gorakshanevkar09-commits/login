package com.example.tests;

import com.example.pages.AmazonLoginPage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AmazonLoginTest extends BaseTest {

    @Test
    void negativeLoginShowsError() {
        AmazonLoginPage loginPage = new AmazonLoginPage(driver).open();

        loginPage.enterEmail("invalid@example.com")
                 .clickContinue()
                 .enterPassword("wrong-password")
                 .clickSignIn();

        String error = loginPage.getErrorMessage();
        assertFalse(error.isBlank(), "Expected an error message for invalid login");
        assertTrue(error.toLowerCase().contains("password") || error.toLowerCase().contains("email") || error.toLowerCase().contains("problem"),
                "Expected a recognizable Amazon sign-in error message");
    }

    @Test
    void positiveLoginWithEnvironmentCredentials() {
        String email = System.getenv("AMAZON_USERNAME");
        String password = System.getenv("AMAZON_PASSWORD");
        Assumptions.assumeTrue(email != null && !email.isBlank() && password != null && !password.isBlank(),
                "Set AMAZON_USERNAME and AMAZON_PASSWORD environment variables to run this test");

        AmazonLoginPage loginPage = new AmazonLoginPage(driver).open();
        loginPage.enterEmail(email)
                 .clickContinue()
                 .enterPassword(password)
                 .clickSignIn();

        assertTrue(loginPage.isSignedIn(), "Expected Amazon login to succeed with valid credentials");
    }
}
