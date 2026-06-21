package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AmazonLoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final String SIGNIN_URL = "https://www.amazon.com/ap/signin";
    private final By emailField = By.id("ap_email");
    private final By continueButton = By.id("continue");
    private final By passwordField = By.id("ap_password");
    private final By signInSubmit = By.id("signInSubmit");
    private final By errorMessage = By.cssSelector("#auth-error-message-box .a-list-item");

    public AmazonLoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public AmazonLoginPage open() {
        driver.get(SIGNIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        return this;
    }

    public AmazonLoginPage enterEmail(String email) {
        wait.until(ExpectedConditions.elementToBeClickable(emailField)).clear();
        wait.until(ExpectedConditions.elementToBeClickable(emailField)).sendKeys(email);
        return this;
    }

    public AmazonLoginPage clickContinue() {
        wait.until(ExpectedConditions.elementToBeClickable(continueButton)).click();
        return this;
    }

    public AmazonLoginPage enterPassword(String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).clear();
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).sendKeys(password);
        return this;
    }

    public AmazonLoginPage clickSignIn() {
        wait.until(ExpectedConditions.elementToBeClickable(signInSubmit)).click();
        return this;
    }

    public String getErrorMessage() {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        return error.getText().trim();
    }

    public boolean isSignedIn() {
        return !driver.getCurrentUrl().contains("signin") && driver.findElements(passwordField).isEmpty();
    }
}
