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
 * Utility service for AI-powered features using external AI API (e.g., OpenAI).
 */
public class AIService {
    private static String apiKey;
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final int MAX_TOKENS = 1024;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties props = new Properties();
        
        // 1. Essayer de charger depuis le fichier à la racine du projet (plus fiable pour le dev)
        try (java.io.FileInputStream fis = new java.io.FileInputStream("config.properties")) {
            props.load(fis);
            apiKey = props.getProperty("openai.api.key");
            if (apiKey != null) System.out.println("Clé API chargée depuis ./config.properties (racine)");
        } catch (IOException e) {
            // Pas grave si le fichier n'est pas à la racine
        }

        // 2. Essayer de charger depuis le classpath (src/main/resources)
        if (apiKey == null) {
            try (InputStream is = AIService.class.getResourceAsStream("/config.properties")) {
                if (is != null) {
                    props.load(is);
                    apiKey = props.getProperty("openai.api.key");
                    if (apiKey != null) System.out.println("Clé API chargée depuis le classpath (/config.properties)");
                }
            } catch (IOException e) {
                System.err.println("Erreur de lecture du config.properties dans le classpath.");
            }
        }

        // 3. Fallback aux variables d'environnement
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_API_KEY_HERE")) {
            apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey != null) System.out.println("Clé API chargée depuis les variables d'environnement (OPENAI_API_KEY)");
        }
        
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_API_KEY_HERE")) {
            apiKey = System.getenv("GROQ_API_KEY");
            if (apiKey != null) System.out.println("Clé API chargée depuis les variables d'environnement (GROQ_API_KEY)");
        }
    }

    private final HttpClient client;
    private final Gson gson;

    public AIService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_API_KEY_HERE")) {
            throw new IllegalStateException("Missing API Key. Please set it in src/main/resources/config.properties or as an OPENAI_API_KEY environment variable.");
        }
        String maskedKey = apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : "invalid-key";
        System.out.println("Using API Key: " + maskedKey);
        
        // Validation simple du format
        if (API_URL.contains("groq") && !apiKey.startsWith("gsk_")) {
            System.err.println("WARNING: Using Groq URL but the key does not start with 'gsk_'. Current key starts with: " + apiKey.substring(0, Math.min(apiKey.length(), 7)));
        }
    }

    /**
     * Builds the JSON request body for the given prompt.
     */
    private JsonObject buildRequestBody(String prompt) {
        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.addProperty("max_tokens", MAX_TOKENS);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);

        body.add("messages", messages);
        return body;
    }

    /**
     * Extracts the content string from the AI API JSON response.
     */
    private String extractContent(JsonObject jsonResponse) {
        return jsonResponse.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }

    /**
     * Sends a prompt to the AI and returns the response content.
     *
     * @param prompt The prompt to send.
     * @return The AI's response as a string.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the operation is interrupted.
     */
    public String getAIResponse(String prompt) throws IOException, InterruptedException {
        validateApiKey();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(buildRequestBody(prompt))))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            return extractContent(jsonResponse);
        } else {
            throw new IOException("AI API error: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Asynchronous version of getAIResponse.
     */
    public CompletableFuture<String> getAIResponseAsync(String prompt) {
        try {
            validateApiKey();
        } catch (IllegalStateException e) {
            return CompletableFuture.failedFuture(e);
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(buildRequestBody(prompt))))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("AI API Status Code: " + response.statusCode());
                    if (response.statusCode() == 200) {
                        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                        return extractContent(jsonResponse);
                    } else {
                        System.err.println("AI API Error Body: " + response.body());
                        throw new RuntimeException("AI API error " + response.statusCode() + ": " + response.body());
                    }
                });
    }

    // Module-specific helper methods

    public String generateTeamReport(String teamName, String stats) throws IOException, InterruptedException {
        String prompt = String.format("Analyze the performance of team '%s' based on these stats: %s. " +
                "Provide a summary of strengths, weaknesses, and suggestions for improvement.", teamName, stats);
        return getAIResponse(prompt);
    }

    public String predictMatchOutcome(String teamA, String teamB, String historicalData, String location, String scores) throws IOException, InterruptedException {
        String prompt = String.format("Predict the outcome of a match between '%s' and '%s'.\n" +
                "Location: %s\n" +
                "Current Scores/Stats: %s\n" +
                "Historical Data: %s\n\n" +
                "Analyze the impact of the location (Home/Away/Neutral), consider the team history and recent scores. " +
                "Provide a detailed prediction including: \n" +
                "1. Predicted Winner\n" +
                "2. Confidence Level (percentage)\n" +
                "3. Key factors for this prediction.", teamA, teamB, location, scores, historicalData);
        return getAIResponse(prompt);
    }

    public CompletableFuture<String> predictMatchOutcomeAsync(String teamA, String teamB, String historicalData, String location, String scores) {
        String prompt = String.format("Predict the outcome of a match between '%s' and '%s'.\n" +
                "Location: %s\n" +
                "Current Scores/Stats: %s\n" +
                "Historical Data: %s\n\n" +
                "Analyze the impact of the location (Home/Away/Neutral), consider the team history and recent scores. " +
                "Provide a detailed prediction including: \n" +
                "1. Predicted Winner\n" +
                "2. Confidence Level (percentage)\n" +
                "3. Key factors for this prediction.", teamA, teamB, location, scores, historicalData);
        return getAIResponseAsync(prompt);
    }

    public String generateGameCommentary(String gameEvent) throws IOException, InterruptedException {
        String prompt = String.format("Generate exciting and professional esports commentary for this event: %s.", gameEvent);
        return getAIResponse(prompt);
    }

    public String suggestTournamentBracket(int numTeams, String teamList) throws IOException, InterruptedException {
        String prompt = String.format("Generate an optimal tournament bracket for %d teams: %s. " +
                "Suggest a fair seeding strategy.", numTeams, teamList);
        return getAIResponse(prompt);
    }
}