# DB Validation Demo

This is a Maven-based demo project showcasing sample DB validation using Cucumber and a mock CSV data flow.

## Overview

The project demonstrates:

- loading sample CSV data
- transforming it into a mock database representation
- validating data consistency
- generating a sample validation report

## Project Structure

- `pom.xml` - Maven configuration with Cucumber dependencies
- `src/test/resources/features/db_validation.feature` - Cucumber feature file
- `src/test/java/com/example/dbvalidation/runner/CucumberTestRunner.java` - Cucumber JUnit runner
- `src/test/java/com/example/dbvalidation/steps/DbValidationSteps.java` - step definitions
- `src/test/resources/data/sample_orders.csv` - sample CSV data for validation

## Run Tests

From the project root:

```powershell
cd c:\Dev\db-validation-demo
mvn test
```

## Notes

- This is a demo project with no real database dependency.
- The mock DB is created in-memory from the CSV rows.
- Cucumber runs the validation flow as a behavior-driven scenario.
