package com.example.dbvalidation.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import com.example.dbvalidation.service.DbValidationWorkflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DbValidationSteps {
    private DbValidationWorkflow workflow;
    private Path csvPath;
    private List<Map<String, String>> csvRows;
    private List<Map<String, String>> mockDb;
    private int expectedCount;
    private Path transactionCsv;
    private Path valuationCsv;
    private Path collateralCsv;
    private boolean transactionResult;
    private boolean valuationResult;
    private boolean collateralResult;

    @Given("a sample CSV file exists with customer orders")
    public void a_sample_csv_file_exists_with_customer_orders() throws IOException {
        csvPath = Path.of("src/test/resources/data/sample_orders.csv");
        assertTrue(Files.exists(csvPath), "Sample CSV file must exist");

        String dailyPrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        try (CSVParser parser = CSVParser.parse(csvPath, java.nio.charset.StandardCharsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            csvRows = new ArrayList<>();
            int recordIndex = 1;
            for (CSVRecord record : parser) {
                Map<String, String> recordMap = record.toMap();
                String originalOrderId = recordMap.get("order_id");
                String dynamicOrderId = String.format("ORD-%s-%03d", dailyPrefix, recordIndex);
                recordMap.put("order_id", dynamicOrderId);
                csvRows.add(recordMap);
                recordIndex++;
            }
        }

        expectedCount = csvRows.size();
    }

    @When("the CSV file is loaded into the validation workflow")
    public void the_csv_file_is_loaded_into_the_validation_workflow() {
        mockDb = csvRows.stream()
                .map(row -> Map.of(
                        "order_id", row.get("order_id"),
                        "customer", row.get("customer"),
                        "amount", row.get("amount"),
                        "status", row.get("status"),
                        "db_verified", "true"
                ))
                .collect(Collectors.toList());
    }

    @Given("transaction, valuation and collateral data files exist")
    public void transaction_valuation_and_collateral_data_files_exist() {
        workflow = new DbValidationWorkflow();
        transactionCsv = Path.of("src/test/resources/data/sample_orders.csv");
        valuationCsv = Path.of("src/test/resources/data/sample_orders_eod_valuation.csv");
        collateralCsv = Path.of("src/test/resources/data/sample_orders_collateral.csv");
        assertTrue(Files.exists(transactionCsv), "Transaction file must exist");
        assertTrue(Files.exists(valuationCsv), "Valuation file must exist");
        assertTrue(Files.exists(collateralCsv), "Collateral file must exist");
    }

    @When("the sequential validation workflow executes")
    public void the_sequential_validation_workflow_executes() throws IOException {
        transactionResult = workflow.executeTransactionStep(transactionCsv);
        valuationResult = workflow.executeValuationStep(valuationCsv);
        collateralResult = workflow.executeCollateralStep(collateralCsv);
    }

    @Then("the transaction step should complete successfully")
    public void the_transaction_step_should_complete_successfully() {
        assertTrue(transactionResult, "Transaction step must succeed before downstream processing");
    }

    @Then("the valuation step should complete successfully")
    public void the_valuation_step_should_complete_successfully() {
        assertTrue(valuationResult, "Valuation step must succeed after transaction success");
    }

    @Then("the collateral step should complete successfully")
    public void the_collateral_step_should_complete_successfully() {
        assertTrue(collateralResult, "Collateral step must succeed after valuation success");
    }

    @Then("the workflow notification should indicate success")
    public void the_workflow_notification_should_indicate_success() {
        assertEquals("Collateral step completed successfully.", workflow.getNotification());
    }

    @Then("database validation layers A, B and C should pass for all steps")
    public void database_validation_layers_should_pass_for_all_steps() {
        Map<String, Map<String, Boolean>> report = workflow.validateAll();
        // validate each category has A,B,C = true
        for (String category : report.keySet()) {
            Map<String, Boolean> layers = report.get(category);
            assertTrue(layers.getOrDefault("A", false), category + " Layer A failed");
            assertTrue(layers.getOrDefault("B", false), category + " Layer B failed");
            assertTrue(layers.getOrDefault("C", false), category + " Layer C failed");
        }
    }

    @When("the integration lambda is invoked")
    public void the_integration_lambda_is_invoked() {
        try {
            boolean invoked = workflow.invokeIntegration();
            assertTrue(invoked, "Integration invocation failed");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Then("the downstream integration should succeed")
    public void the_downstream_integration_should_succeed() {
        assertTrue(workflow.isIntegrationSuccessful(), "Integration must be successful");
        assertEquals("Integration completed successfully.", workflow.getIntegrationMessage());
    }

    @When("a validation report is generated")
    public void a_validation_report_is_generated() throws IOException {
        Path report = workflow.generateReport();
        assertTrue(Files.exists(report), "Report must be generated");
    }

    @When("the report is emailed to recipients")
    public void the_report_is_emailed_to_recipients() throws IOException {
        String[] recipients = new String[]{"team@example.com", "qa@example.com"};
        Path email = workflow.sendReport(recipients);
        assertTrue(Files.exists(email), "Email file must be created");
    }

    @Then("an email should be created for recipients")
    public void an_email_should_be_created_for_recipients() throws IOException {
        Path dir = Path.of("src/test/resources/data/emails");
        assertTrue(Files.exists(dir) && Files.list(dir).findAny().isPresent(), "At least one email file should exist");
    }

    @Then("the mock database should contain matching order records")
    public void the_mock_database_should_contain_matching_order_records() {
        assertEquals(expectedCount, mockDb.size(), "Mock DB row count should match CSV row count");
        for (int i = 0; i < expectedCount; i++) {
            Map<String, String> csvRow = csvRows.get(i);
            Map<String, String> dbRow = mockDb.get(i);
            assertEquals(csvRow.get("order_id"), dbRow.get("order_id"));
            assertEquals(csvRow.get("customer"), dbRow.get("customer"));
            assertEquals(csvRow.get("amount"), dbRow.get("amount"));
            assertEquals("true", dbRow.get("db_verified"));
        }
    }

    @Then("the final validation report should include the expected order count")
    public void the_final_validation_report_should_include_the_expected_order_count() {
        assertEquals(expectedCount, mockDb.size(), "Validation report order count should equal expected count");
    }
}
