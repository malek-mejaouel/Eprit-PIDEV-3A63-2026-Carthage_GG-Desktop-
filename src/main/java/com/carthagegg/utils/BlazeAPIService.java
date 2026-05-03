package com.carthagegg.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class BlazeAPIService {
    private static final String API_URL = "https://blazeai.boxu.dev/api/chat/completions";
    private static final String API_KEY = "blz_RbZ6Y07HMZn7qq92CCfw1KVE3lD5hxd4rwFGGrItrys";
    private static final String DEFAULT_MODEL = "qwen3.6-plus";
    
    private final HttpClient httpClient;
    private final Gson gson;
    private final List<Map<String, String>> chatHistory;

    public BlazeAPIService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.chatHistory = new ArrayList<>();
        setupSystemPrompt();
    }

    
    private void setupSystemPrompt() {
        String systemPrompt = "You are the CarthageGG AI Assistant, a specialized helper for the CarthageGG esports platform.\n\n" +
                "**Restricted Topics:** You ONLY discuss the following:\n" +
                "1. **App Theme:** CarthageGG uses a 'Yellow & Black' premium gaming theme.\n" +
                "   - Primary Yellow: #FFC107\n" +
                "   - Background: #0a0a0b\n" +
                "   - Surface/Cards: #141416\n" +
                "   - Accent Gold: #D4AF37\n" +
                "2. **Database Structure:** You can explain how the platform is organized based on these tables:\n" +
                "   - `users`: Member profiles, roles, and status.\n" +
                "   - `games`: Games supported (e.g., League of Legends, Valorant).\n" +
                "   - `tournaments`: Competitive events with prize pools.\n" +
                "   - `teams`: Player organizations.\n" +
                "   - `matches`: Individual game results and schedules.\n" +
                "   - `news` & `comments`: Platform updates and community discussion.\n" +
                "   - `event`: Physical or digital gatherings and reservations.\n" +
                "   - `products` & `orders`: The CarthageGG Shop where users buy gaming gear.\n" +
                "   - `streams`: Live gaming content.\n" +
                "   - `reclamation`: Support tickets and help requests.\n" +
                "3. **Platform Items:** We offer various products like gaming peripherals, jerseys, and digital items in our Shop.\n\n" +
                "**Guidelines:**\n" +
                "- If a user asks about unrelated topics (politics, general knowledge, other apps), politely say: \"I am CarthageGG AI, specialized only in our platform's theme, items, and features. I cannot assist with that topic.\"\n" +
                "- Be professional, helpful, and use a tone that fits an esports community.\n" +
                "- Keep responses concise.";
        
        chatHistory.add(Map.of("role", "system", "content", systemPrompt));
    }

    public String sendMessage(String userMessage) {
        chatHistory.add(Map.of("role", "user", "content", userMessage));
        
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("model", DEFAULT_MODEL);
            
            JsonArray messages = new JsonArray();
            for (Map<String, String> msg : chatHistory) {
                JsonObject m = new JsonObject();
                m.addProperty("role", msg.get("role"));
                m.addProperty("content", msg.get("content"));
                messages.add(m);
            }
            payload.add("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject responseBody = gson.fromJson(response.body(), JsonObject.class);
                String aiResponse = responseBody.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();
                
                chatHistory.add(Map.of("role", "assistant", "content", aiResponse));
                return aiResponse;
            } else {
                return "Error: " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    
    public void clearHistory() {
        chatHistory.clear();
        setupSystemPrompt();
    }
}
