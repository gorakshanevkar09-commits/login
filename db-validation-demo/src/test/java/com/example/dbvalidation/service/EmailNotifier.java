package com.example.dbvalidation.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class EmailNotifier {
    public static Path sendEmail(String[] recipients, String subject, String body) throws IOException {
        Path out = Path.of("src/test/resources/data/emails").resolve("email_" + Instant.now().toEpochMilli() + ".eml");
        Files.createDirectories(out.getParent());
        StringBuilder sb = new StringBuilder();
        sb.append("To: ");
        for (int i = 0; i < recipients.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(recipients[i]);
        }
        sb.append("\nSubject: ").append(subject).append("\n\n");
        sb.append(body).append("\n");
        Files.writeString(out, sb.toString());
        return out;
    }
}
