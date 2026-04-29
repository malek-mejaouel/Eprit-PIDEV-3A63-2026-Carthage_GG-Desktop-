package com.carthagegg.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class EmailService {
    private static final Properties config = new Properties();
    private static final String WEBHOOK_URL;

    static {
        try (InputStream input = EmailService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
            }
        } catch (IOException ex) {
            System.err.println("Could not load config.properties: " + ex.getMessage());
        }
        WEBHOOK_URL = config.getProperty("n8n.signup_webhook", "");
    }

    public static void sendWelcomeEmail(String email, String name) {
        if (WEBHOOK_URL.isEmpty()) {
            System.err.println("n8n webhook URL not configured in config.properties");
            return;
        }

        new Thread(() -> {
            try {
                String json = String.format("{\"email\": \"%s\", \"name\": \"%s\"}", email, name);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(WEBHOOK_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    System.out.println("Welcome email trigger sent successfully to n8n");
                } else {
                    System.err.println("Failed to trigger n8n webhook. Status: " + response.statusCode());
                    System.err.println("Response: " + response.body());
                }
            } catch (Exception e) {
                System.err.println("Error triggering welcome email: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
