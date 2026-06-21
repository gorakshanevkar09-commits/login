package com.example.dbvalidation.service;

import java.nio.file.Path;

public class AwsWatcherDemo {
    public static void main(String[] args) throws Exception {
        Path env = Path.of("env/DEV.env");
        String[] recipients = new String[]{"ops@example.com"};
        // For demo use 1 minute interval. Production should use 60.
        try (AwsCredentialsWatcher watcher = new AwsCredentialsWatcher(env, 1, recipients)) {
            watcher.start();
            System.out.println("AwsCredentialsWatcher started. Running for 2 minutes demo...");
            Thread.sleep(120_000);
            System.out.println("Demo finished. Stopping watcher.");
        }
    }
}
