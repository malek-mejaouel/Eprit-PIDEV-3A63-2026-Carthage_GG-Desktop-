package com.carthagegg.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Unified AI service supporting both Groq (OpenAI-compatible) and Gemini APIs.
 */
public class AIService {

    // ─── Groq / OpenAI-compatible config ────────────────────────────────────
    private static String groqApiKey;
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL    = "llama-3.3-70b-versatile";
    private static final int    MAX_TOKENS    = 1024;

    // ─── Gemini config ───────────────────────────────────────────────────────
    private static final String GEMINI_API_KEY = ConfigManager.get("gemini.api.key");
    private static final String[] GEMINI_MODELS = {
            "gemini-2.5-flash",
            "gemini-3-flash-preview",
            "gemini-1.5-flash",
            "gemini-1.5-flash-latest",
            "gemini-1.5-pro",
            "gemini-1.0-pro"
    };
    private static final String GEMINI_BASE_URL =
            "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=";

    static {
        loadGroqConfig();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Groq config loading
    // ════════════════════════════════════════════════════════════════════════

    private static void loadGroqConfig() {
        Properties props = new Properties();

        // 1. Root-level config.properties
        try (java.io.FileInputStream fis = new java.io.FileInputStream("config.properties")) {
            props.load(fis);
            groqApiKey = props.getProperty("openai.api.key");
            if (groqApiKey != null) System.out.println("Groq key loaded from ./config.properties");
        } catch (IOException ignored) {}

        // 2. Classpath (src/main/resources)
        if (groqApiKey == null) {
            try (InputStream is = AIService.class.getResourceAsStream("/config.properties")) {
                if (is != null) {
                    props.load(is);
                    groqApiKey = props.getProperty("openai.api.key");
                    if (groqApiKey != null) System.out.println("Groq key loaded from classpath");
                }
            } catch (IOException e) {
                System.err.println("Error reading classpath config.properties");
            }
        }

        // 3. Environment variables
        if (isBlankOrPlaceholder(groqApiKey)) {
            groqApiKey = System.getenv("OPENAI_API_KEY");
            if (groqApiKey != null) System.out.println("Groq key loaded from OPENAI_API_KEY env var");
        }
        if (isBlankOrPlaceholder(groqApiKey)) {
            groqApiKey = System.getenv("GROQ_API_KEY");
            if (groqApiKey != null) System.out.println("Groq key loaded from GROQ_API_KEY env var");
        }
    }

    private static boolean isBlankOrPlaceholder(String key) {
        return key == null || key.isBlank() || key.equals("YOUR_API_KEY_HERE");
    }

    // ════════════════════════════════════════════════════════════════════════
    // Instance fields
    // ════════════════════════════════════════════════════════════════════════

    private final HttpClient client;
    private final Gson gson;

    public AIService() {
        this.client = HttpClient.newHttpClient();
        this.gson   = new Gson();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Groq — core request helpers
    // ════════════════════════════════════════════════════════════════════════

    private void validateGroqKey() {
        if (isBlankOrPlaceholder(groqApiKey)) {
            throw new IllegalStateException(
                    "Missing Groq API key. Set it in config.properties (openai.api.key) " +
                            "or via GROQ_API_KEY / OPENAI_API_KEY environment variables.");
        }
        String masked = groqApiKey.length() > 10 ? groqApiKey.substring(0, 10) + "..." : "invalid-key";
        System.out.println("Using Groq API key: " + masked);

        if (!groqApiKey.startsWith("gsk_")) {
            System.err.println("WARNING: Key does not start with 'gsk_'. " +
                    "Starts with: " + groqApiKey.substring(0, Math.min(groqApiKey.length(), 7)));
        }
    }

    private JsonObject buildGroqRequestBody(String prompt) {
        JsonObject body = new JsonObject();
        body.addProperty("model", GROQ_MODEL);
        body.addProperty("max_tokens", MAX_TOKENS);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);

        body.add("messages", messages);
        return body;
    }

    private String extractGroqContent(JsonObject jsonResponse) {
        return jsonResponse.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Groq — public API
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Sends a prompt to Groq and returns the response (blocking).
     */
    public String getAIResponse(String prompt) throws IOException, InterruptedException {
        validateGroqKey();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + groqApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(buildGroqRequestBody(prompt))))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return extractGroqContent(gson.fromJson(response.body(), JsonObject.class));
        }
        throw new IOException("Groq API error " + response.statusCode() + ": " + response.body());
    }

    /**
     * Sends a prompt to Groq asynchronously.
     */
    public CompletableFuture<String> getAIResponseAsync(String prompt) {
        try {
            validateGroqKey();
        } catch (IllegalStateException e) {
            return CompletableFuture.failedFuture(e);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + groqApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(buildGroqRequestBody(prompt))))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Groq status: " + response.statusCode());
                    if (response.statusCode() == 200) {
                        return extractGroqContent(gson.fromJson(response.body(), JsonObject.class));
                    }
                    System.err.println("Groq error body: " + response.body());
                    throw new RuntimeException("Groq API error " + response.statusCode() + ": " + response.body());
                });
    }

    // ════════════════════════════════════════════════════════════════════════
    // Groq — domain-specific helpers
    // ════════════════════════════════════════════════════════════════════════

    public String generateTeamReport(String teamName, String stats)
            throws IOException, InterruptedException {
        return getAIResponse(String.format(
                "Analyze the performance of team '%s' based on these stats: %s. " +
                        "Provide a summary of strengths, weaknesses, and suggestions for improvement.",
                teamName, stats));
    }

    public String predictMatchOutcome(String teamA, String teamB,
                                      String historicalData, String location, String scores)
            throws IOException, InterruptedException {
        return getAIResponse(buildMatchPrompt(teamA, teamB, historicalData, location, scores));
    }

    public CompletableFuture<String> predictMatchOutcomeAsync(String teamA, String teamB,
                                                              String historicalData, String location, String scores) {
        return getAIResponseAsync(buildMatchPrompt(teamA, teamB, historicalData, location, scores));
    }

    private String buildMatchPrompt(String teamA, String teamB,
                                    String historicalData, String location, String scores) {
        return String.format(
                "Predict the outcome of a match between '%s' and '%s'.\n" +
                        "Location: %s\nCurrent Scores/Stats: %s\nHistorical Data: %s\n\n" +
                        "Analyze the impact of the location (Home/Away/Neutral), consider the team history and recent scores. " +
                        "Provide a detailed prediction including:\n" +
                        "1. Predicted Winner\n2. Confidence Level (percentage)\n3. Key factors for this prediction.",
                teamA, teamB, location, scores, historicalData);
    }

    public String generateGameCommentary(String gameEvent)
            throws IOException, InterruptedException {
        return getAIResponse(String.format(
                "Generate exciting and professional esports commentary for this event: %s.", gameEvent));
    }

    public String suggestTournamentBracket(int numTeams, String teamList)
            throws IOException, InterruptedException {
        return getAIResponse(String.format(
                "Generate an optimal tournament bracket for %d teams: %s. " +
                        "Suggest a fair seeding strategy.", numTeams, teamList));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Gemini — public API
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Generates a gaming-themed product description using the Gemini API,
     * falling back through multiple models automatically.
     */
    public static CompletableFuture<String> generateProductDescription(
            String productName, String category) {

        if (GEMINI_API_KEY == null || GEMINI_API_KEY.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new Exception("Gemini API key missing. Add gemini.api.key to config.properties."));
        }

        String prompt = String.format(
                "Write a short, catchy, and professional gaming-themed product description for a '%s' " +
                        "in the category '%s'. Make it sound exciting for gamers. Keep it under 200 characters.",
                productName, category);

        JsonObject body    = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonArray parts    = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);
        JsonObject contentObj = new JsonObject();
        contentObj.add("parts", parts);
        contents.add(contentObj);
        body.add("contents", contents);

        return tryNextGeminiModel(HttpClient.newHttpClient(), body, 0);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Gemini — internal helpers
    // ════════════════════════════════════════════════════════════════════════

    private static CompletableFuture<String> tryNextGeminiModel(
            HttpClient client, JsonObject body, int index) {

        if (index >= GEMINI_MODELS.length) {
            return CompletableFuture.completedFuture("AI Error: All Gemini models failed or are unavailable.");
        }

        String modelName = GEMINI_MODELS[index];
        String url       = String.format(GEMINI_BASE_URL, modelName);

        return sendGeminiRequest(client, url, body)
                .thenCompose(response -> {
                    if (response.statusCode() == 404 || response.statusCode() == 400) {
                        String bodyStr = response.body();
                        if (bodyStr.contains("not found") || bodyStr.contains("unsupported")) {
                            System.out.println("Gemini model " + modelName + " not found, trying next…");
                            return tryNextGeminiModel(client, body, index + 1);
                        }
                    }
                    if (response.statusCode() != 200) {
                        System.err.println("Gemini error (" + modelName + ", " +
                                response.statusCode() + "): " + response.body());
                        return CompletableFuture.completedFuture(
                                extractGeminiError(response.body()));
                    }
                    return CompletableFuture.completedFuture(
                            extractGeminiText(response.body()));
                })
                .exceptionally(ex -> {
                    System.err.println("Gemini request failed for " + modelName + ": " + ex.getMessage());
                    return "AI Error: Connection failed. " + ex.getMessage();
                });
    }

    private static CompletableFuture<HttpResponse<String>> sendGeminiRequest(
            HttpClient client, String url, JsonObject body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + GEMINI_API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String extractGeminiError(String responseBody) {
        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            if (json.has("error")) {
                String message = json.getAsJsonObject("error").get("message").getAsString();
                if (message.contains("not found for API version v1beta")) {
                    return "API Error: Model mismatch — use the 'v1' stable API.";
                }
                return "API Error: " + message;
            }
            return "API Error: " + responseBody;
        } catch (Exception e) {
            return "API Error: Failed to parse error response.";
        }
    }

    private static String extractGeminiText(String responseBody) {
        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            if (json.has("error")) return extractGeminiError(responseBody);

            if (!json.has("candidates") || json.getAsJsonArray("candidates").isEmpty()) {
                return "AI Error: No response generated (safety filters may have blocked it).";
            }

            JsonObject candidate = json.getAsJsonArray("candidates").get(0).getAsJsonObject();

            if (candidate.has("finishReason") &&
                    !"STOP".equals(candidate.get("finishReason").getAsString())) {
                String reason = candidate.get("finishReason").getAsString();
                if ("SAFETY".equals(reason))    return "AI Error: Blocked by safety filters.";
                if ("RECITATION".equals(reason)) return "AI Error: Blocked by recitation policy.";
            }

            return candidate.getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString().trim();
        } catch (Exception e) {
            return "AI Error: Failed to parse Gemini response. " + e.getMessage();
        }
    }
}