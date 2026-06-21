package com.example.dbvalidation.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AwsCredentialsWatcher implements AutoCloseable, Runnable {
    private final Path envFile;
    private final long intervalMinutes;
    private final String[] notifyRecipients;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;

    public AwsCredentialsWatcher(Path envFile, long intervalMinutes, String[] notifyRecipients) {
        this.envFile = envFile;
        this.intervalMinutes = intervalMinutes;
        this.notifyRecipients = notifyRecipients;
    }

    public void start() {
        // initial immediate run then schedule
        future = scheduler.scheduleAtFixedRate(this, 0, intervalMinutes, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        try {
            Map<String, String> props = readEnvFile(envFile);
            String token = props.get("AWS_SESSION_TOKEN");
            String expiration = props.get("AWS_SESSION_EXPIRATION");

            if (token == null || token.isBlank()) {
                String subject = "AWS Credentials Missing";
                String body = "AWS session token not found in " + envFile + ".";
                EmailNotifier.sendEmail(notifyRecipients, subject, body);
                return;
            }

            if (expiration == null || expiration.isBlank()) {
                String subject = "AWS Session Expiration Missing";
                String body = "AWS session expiration not present in " + envFile + ".";
                EmailNotifier.sendEmail(notifyRecipients, subject, body);
                return;
            }

            Instant expInstant;
            try {
                expInstant = Instant.parse(expiration);
            } catch (Exception ex) {
                String subject = "AWS Session Expiration Parse Error";
                String body = "Could not parse AWS_SESSION_EXPIRATION in " + envFile + ": " + ex.getMessage();
                EmailNotifier.sendEmail(notifyRecipients, subject, body);
                return;
            }

            if (Instant.now().isAfter(expInstant)) {
                String subject = "AWS Session Token Expired";
                String body = "AWS session token in " + envFile + " expired at " + expInstant + ".";
                EmailNotifier.sendEmail(notifyRecipients, subject, body);
            }
        } catch (IOException e) {
            try {
                EmailNotifier.sendEmail(notifyRecipients, "AWS Watcher Error", "Error reading env file: " + e.getMessage());
            } catch (IOException ignored) {
            }
        }
    }

    private Map<String, String> readEnvFile(Path file) throws IOException {
        Map<String, String> map = new HashMap<>();
        if (!Files.exists(file)) return map;
        for (String line : Files.readAllLines(file)) {
            String l = line.trim();
            if (l.isEmpty() || l.startsWith("#")) continue;
            int eq = l.indexOf('=');
            if (eq <= 0) continue;
            String k = l.substring(0, eq).trim();
            String v = l.substring(eq + 1).trim();
            map.put(k, v);
        }
        return map;
    }

    @Override
    public void close() {
        if (future != null) future.cancel(true);
        scheduler.shutdownNow();
    }
}
