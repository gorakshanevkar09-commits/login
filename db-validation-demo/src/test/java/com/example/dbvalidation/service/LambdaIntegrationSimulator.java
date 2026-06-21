package com.example.dbvalidation.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

public class LambdaIntegrationSimulator {
    public static Path invoke(Map<String, Object> payload) throws IOException {
        Path out = Path.of("src/test/resources/data/downstream_integration.json");
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"timestamp\": \"").append(Instant.now().toString()).append("\",\n");
        int i = 0;
        for (Map.Entry<String, Object> e : payload.entrySet()) {
            i++;
            sb.append("  \"").append(e.getKey()).append("\": ");
            Object v = e.getValue();
            if (v instanceof Number || v instanceof Boolean) {
                sb.append(v.toString());
            } else {
                sb.append("\"").append(String.valueOf(v).replace("\"","\\\"")).append("\"");
            }
            if (i < payload.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("}\n");
        Files.createDirectories(out.getParent());
        Files.writeString(out, sb.toString());
        return out;
    }
}
