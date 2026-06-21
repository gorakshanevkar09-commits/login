# Amazon Login Demo

This is a Maven-based Java project that verifies Amazon login using Selenium WebDriver and the Page Object Model (POM) pattern.

## Requirements

- Java 17 or later
- Maven
- Google Chrome browser

## Project Structure

- `pom.xml` - Maven configuration and dependencies
- `src/main/java/com/example/pages/AmazonLoginPage.java` - Amazon login page object
- `src/test/java/com/example/tests/BaseTest.java` - WebDriver setup and teardown
- `src/test/java/com/example/tests/AmazonLoginTest.java` - Positive and negative login tests

## Run Tests

1. Open a new terminal in `c:\Dev`
2. To run all tests:

```powershell
mvn test
```

## Positive Login Test

The positive login test uses environment variables for credentials.

Set these before running tests:

```powershell
$env:AMAZON_USERNAME = 'your-amazon-email'
$env:AMAZON_PASSWORD = 'your-amazon-password'
```

Then run:

```powershell
mvn test
```

If the variables are not set, the positive login test is skipped.

## Negative Login Test

The negative login test uses invalid credentials and validates that Amazon shows an error message.

## Notes

- WebDriverManager downloads the ChromeDriver binary automatically.
- The tests open a real Chrome browser and interact with Amazon's sign-in page.
- Make sure the browser is up to date and the network allows access to `https://www.amazon.com`.

## GitHub Actions Pipeline

A CI workflow is included in `.github/workflows/maven.yml`.

The pipeline:

- runs on `push` and `pull_request` targeting `main`
- checks out the repository
- sets up JDK 17 using `actions/setup-java`
- runs `mvn test`

To use the pipeline:

1. Push this repository to GitHub.
2. Ensure `.github/workflows/maven.yml` exists.
3. GitHub Actions will automatically trigger on your next push.

## Playwright Pipeline

A Playwright workflow is included in `.github/workflows/playwright.yml`.

This workflow:

- runs on `push` and `pull_request` targeting `main`
- checks out the repository
- sets up Node.js 20 using `actions/setup-node`
- installs dependencies and Playwright browsers in `flipkart-playwright-demo`
- runs `npm test`

To use the Playwright pipeline:

1. Push this repository to GitHub.
2. Ensure `.github/workflows/playwright.yml` exists.
3. GitHub Actions will automatically trigger on your next push.
