package com.example.dbvalidation.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;

public class DbValidationWorkflow {
    private List<Map<String, String>> transactionData = new ArrayList<>();
    private List<Map<String, String>> valuationData = new ArrayList<>();
    private List<Map<String, String>> collateralData = new ArrayList<>();
    private String notification;
    private boolean transactionSuccessful;
    private boolean valuationSuccessful;
    private boolean collateralSuccessful;
    private boolean integrationSuccessful;
    private String integrationMessage;

    public boolean executeTransactionStep(Path csvPath) throws IOException {
        transactionData = parseCsv(csvPath);
        transactionSuccessful = !transactionData.isEmpty();
        if (transactionSuccessful) {
            notification = "Transaction step completed successfully.";
        } else {
            notification = "Transaction step failed: no transaction records found.";
        }
        return transactionSuccessful;
    }

    public boolean executeValuationStep(Path valuationPath) throws IOException {
        if (!transactionSuccessful) {
            valuationSuccessful = false;
            notification = "Valuation skipped because transaction step failed.";
            return false;
        }

        valuationData = parseCsv(valuationPath);
        valuationSuccessful = !valuationData.isEmpty() && valuationData.size() == transactionData.size();

        if (valuationSuccessful) {
            notification = "Valuation step completed successfully.";
        } else {
            notification = "Valuation step failed: valuation record count mismatch or no records.";
        }
        return valuationSuccessful;
    }

    public boolean executeCollateralStep(Path collateralPath) throws IOException {
        if (!transactionSuccessful) {
            collateralSuccessful = false;
            notification = "Collateral skipped because transaction step failed.";
            return false;
        }
        if (!valuationSuccessful) {
            collateralSuccessful = false;
            notification = "Collateral skipped because valuation step failed.";
            return false;
        }

        collateralData = parseCsv(collateralPath);
        collateralSuccessful = !collateralData.isEmpty() && collateralData.size() == transactionData.size();

        if (collateralSuccessful) {
            notification = "Collateral step completed successfully.";
        } else {
            notification = "Collateral step failed: collateral record count mismatch or no records.";
        }
        return collateralSuccessful;
    }

    public String getNotification() {
        return notification;
    }

    public List<Map<String, String>> getTransactionData() {
        return transactionData;
    }

    public List<Map<String, String>> getValuationData() {
        return valuationData;
    }

    public List<Map<String, String>> getCollateralData() {
        return collateralData;
    }

    public Map<String, Map<String, Boolean>> validateAll() {
        Map<String, Map<String, Boolean>> report = new java.util.HashMap<>();
        report.put("transaction", validateLayersFor("transaction"));
        report.put("valuation", validateLayersFor("valuation"));
        report.put("collateral", validateLayersFor("collateral"));
        return report;
    }

    public Map<String, Boolean> validateLayersFor(String category) {
        List<Map<String, String>> data;
        switch (category.toLowerCase()) {
            case "valuation":
                data = this.valuationData;
                break;
            case "collateral":
                data = this.collateralData;
                break;
            default:
                data = this.transactionData;
        }

        Map<String, Boolean> results = new java.util.HashMap<>();
        results.put("A", checkLayerA(data));
        results.put("B", checkLayerB(data));
        results.put("C", checkLayerC(data));
        return results;
    }

    private boolean checkLayerA(List<Map<String, String>> data) {
        // Layer A: basic presence checks (order_id exists)
        if (data == null || data.isEmpty()) return false;
        return data.stream().allMatch(r -> r.containsKey("order_id") && !r.get("order_id").isBlank());
    }

    private boolean checkLayerB(List<Map<String, String>> data) {
        // Layer B: numeric fields exist (amount or order_value)
        if (data == null || data.isEmpty()) return false;
        return data.stream().allMatch(r -> {
            String a = r.getOrDefault("amount", r.getOrDefault("order_value", "0"));
            try {
                Double.parseDouble(a);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    private boolean checkLayerC(List<Map<String, String>> data) {
        // Layer C: status/verification flags present
        if (data == null || data.isEmpty()) return false;
        return data.stream().allMatch(r -> r.containsKey("status") || r.containsKey("eod_status") || r.containsKey("collateral_status") || r.containsKey("db_verified"));
    }

    public boolean invokeIntegration() {
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("transactionCount", this.transactionData == null ? 0 : this.transactionData.size());
            payload.put("valuationCount", this.valuationData == null ? 0 : this.valuationData.size());
            payload.put("collateralCount", this.collateralData == null ? 0 : this.collateralData.size());
            // call the simulator
            Path out = LambdaIntegrationSimulator.invoke(payload);
            this.integrationSuccessful = out != null && java.nio.file.Files.exists(out);
            this.integrationMessage = this.integrationSuccessful ? "Integration completed successfully." : "Integration failed: output not created.";
            return this.integrationSuccessful;
        } catch (Exception e) {
            this.integrationSuccessful = false;
            this.integrationMessage = "Integration failed: " + e.getMessage();
            return false;
        }
    }

    public boolean isIntegrationSuccessful() {
        return integrationSuccessful;
    }

    public String getIntegrationMessage() {
        return integrationMessage;
    }

    public Path generateReport() throws IOException {
        Path reportPath = Path.of("src/test/resources/data/reports/validation_report_" + System.currentTimeMillis() + ".txt");
        Files.createDirectories(reportPath.getParent());
        StringBuilder sb = new StringBuilder();
        sb.append("Validation Report\n");
        sb.append("=================\n");
        sb.append("transactionCount: ").append(transactionData == null ? 0 : transactionData.size()).append("\n");
        sb.append("valuationCount: ").append(valuationData == null ? 0 : valuationData.size()).append("\n");
        sb.append("collateralCount: ").append(collateralData == null ? 0 : collateralData.size()).append("\n\n");
        sb.append("Layered Validation:\n");
        Map<String, Map<String, Boolean>> all = validateAll();
        all.forEach((k, v) -> {
            sb.append(k).append(": A=").append(v.getOrDefault("A", false)).append(", B=").append(v.getOrDefault("B", false)).append(", C=").append(v.getOrDefault("C", false)).append("\n");
        });
        Files.writeString(reportPath, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return reportPath;
    }

    public Path sendReport(String[] recipients) throws IOException {
        Path report = generateReport();
        String subject = "DB Validation Report";
        String body = "Please find the validation report at: " + report.toAbsolutePath();
        Path email = EmailNotifier.sendEmail(recipients, subject, body);
        this.integrationMessage = "Report emailed to recipients.";
        return email;
    }

    private List<Map<String, String>> parseCsv(Path path) throws IOException {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (CSVParser parser = CSVParser.parse(path, java.nio.charset.StandardCharsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            List<Map<String, String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                Map<String, String> row = new HashMap<>();
                record.toMap().forEach(row::put);
                rows.add(row);
            }
            return rows;
        }
    }
}
