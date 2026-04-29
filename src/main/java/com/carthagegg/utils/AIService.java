package com.carthagegg.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class AIService {
    private static String GEMINI_API_KEY;
    // We will try these models in order until one works
    private static final String[] MODELS = {
        "gemini-2.5-flash",
        "gemini-3-flash-preview",
        "gemini-1.5-flash",
        "gemini-1.5-flash-latest",
        "gemini-1.5-pro",
        "gemini-1.0-pro"
    };
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=";

    static {
        try {
            Properties props = new Properties();
            // Try to load from project root
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                props.load(fis);
                GEMINI_API_KEY = props.getProperty("gemini.api.key");
                if (GEMINI_API_KEY != null && !GEMINI_API_KEY.isEmpty()) {
                    System.out.println("AIService: Gemini API Key loaded successfully.");
                } else {
                    System.err.println("AIService: gemini.api.key is empty in config.properties");
                }
            }
        } catch (Exception e) {
            System.err.println("AIService: Could not load config.properties - " + e.getMessage());
        }
    }

    public static CompletableFuture<String> generateProductDescription(String productName, String category) {
        if (GEMINI_API_KEY == null || GEMINI_API_KEY.isEmpty()) {
            return CompletableFuture.failedFuture(new Exception("API Key missing. Please add gemini.api.key to config.properties"));
        }

        String prompt = String.format(
            "Write a short, catchy, and professional gaming-themed product description for a '%s' in the category '%s'. " +
            "Make it sound exciting for gamers. Keep it under 200 characters.", 
            productName, category
        );

        // Prepare request body
        var body = new JsonObject();
        var contents = new com.google.gson.JsonArray();
        var parts = new com.google.gson.JsonArray();
        var textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);
        var contentObj = new JsonObject();
        contentObj.add("parts", parts);
        contents.add(contentObj);
        body.add("contents", contents);

        HttpClient client = HttpClient.newHttpClient();
        return tryNextModel(client, body, 0);
    }

    private static CompletableFuture<String> tryNextModel(HttpClient client, JsonObject body, int modelIndex) {
        if (modelIndex >= MODELS.length) {
            return CompletableFuture.completedFuture("AI Error: All models failed or are unavailable.");
        }

        String modelName = MODELS[modelIndex];
        String url = String.format(BASE_URL, modelName);

        return sendRequest(client, url, body)
                .thenCompose(response -> {
                    if (response.statusCode() == 404 || response.statusCode() == 400) {
                        // 400 can sometimes mean "model not found" or "invalid argument" depending on the error message
                        String bodyStr = response.body();
                        if (bodyStr.contains("not found") || bodyStr.contains("unsupported")) {
                            System.out.println("AIService: Model " + modelName + " not found, trying next...");
                            return tryNextModel(client, body, modelIndex + 1);
                        }
                    }
                    
                    if (response.statusCode() != 200) {
                        System.err.println("Gemini API Error (" + modelName + ", Status " + response.statusCode() + "): " + response.body());
                        return CompletableFuture.completedFuture(extractErrorFromResponse(response.body()));
                    }

                    return CompletableFuture.completedFuture(extractTextFromResponse(response.body()));
                })
                .exceptionally(ex -> {
                    System.err.println("AIService: Request failed for " + modelName + ": " + ex.getMessage());
                    return "AI Error: Connection failed. " + ex.getMessage();
                });
    }

    private static CompletableFuture<HttpResponse<String>> sendRequest(HttpClient client, String url, JsonObject body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + GEMINI_API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String extractErrorFromResponse(String responseBody) {
        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            if (json.has("error")) {
                JsonObject error = json.getAsJsonObject("error");
                String message = error.get("message").getAsString();
                if (message.contains("not found for API version v1beta")) {
                    return "API Error: Model mismatch. Please ensure you are using the 'v1' stable API.";
                }
                return "API Error: " + message;
            }
            return "API Error: Status " + responseBody;
        } catch (Exception e) {
            return "API Error: Failed to parse error response.";
        }
    }

    private static String extractTextFromResponse(String responseBody) {
        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            
            if (json.has("error")) {
                return extractErrorFromResponse(responseBody);
            }

            if (!json.has("candidates") || json.getAsJsonArray("candidates").isEmpty()) {
                return "AI Error: No response generated (Safety filters might have blocked it).";
            }

            JsonObject candidate = json.getAsJsonArray("candidates").get(0).getAsJsonObject();
            
            if (candidate.has("finishReason") && !"STOP".equals(candidate.get("finishReason").getAsString())) {
                String reason = candidate.get("finishReason").getAsString();
                if ("SAFETY".equals(reason)) return "AI Error: Blocked by safety filters.";
                if ("RECITATION".equals(reason)) return "AI Error: Blocked by recitation policy.";
            }

            return candidate.getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString().trim();
        } catch (Exception e) {
            return "AI Error: Failed to parse response. " + e.getMessage();
        }
    }
}
