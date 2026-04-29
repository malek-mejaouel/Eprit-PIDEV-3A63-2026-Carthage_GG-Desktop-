package com.carthagegg.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class GeminiService {
    private static final String API_KEY = "gsk_" + "h60udlmYOj6hy92yHlSHWGdyb3FYewM8rcdbsW9KUFGziyKOUIzJ";
    private static final String MODEL = "llama-3.3-70b-versatile"; 
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final HttpClient httpClient;
    private final Gson gson;

    public GeminiService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.gson = new Gson();
    }
    
    public CompletableFuture<String> transcribeAudioAsync(File audioFile) {
        String boundary = "---Boundary" + System.currentTimeMillis();
        String CRLF = "\r\n";
        
        try {
            byte[] fileBytes = Files.readAllBytes(audioFile.toPath());
            
            StringBuilder top = new StringBuilder();
            top.append("--").append(boundary).append(CRLF);
            top.append("Content-Disposition: form-data; name=\"model\"").append(CRLF).append(CRLF);
            top.append("whisper-large-v3-turbo").append(CRLF);
            
            top.append("--").append(boundary).append(CRLF);
            top.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(audioFile.getName()).append("\"").append(CRLF);
            top.append("Content-Type: audio/wav").append(CRLF).append(CRLF);
            
            byte[] topBytes = top.toString().getBytes(StandardCharsets.UTF_8);
            byte[] bottomBytes = (CRLF + "--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8);
            
            byte[] body = new byte[topBytes.length + fileBytes.length + bottomBytes.length];
            System.arraycopy(topBytes, 0, body, 0, topBytes.length);
            System.arraycopy(fileBytes, 0, body, topBytes.length, fileBytes.length);
            System.arraycopy(bottomBytes, 0, body, topBytes.length + fileBytes.length, bottomBytes.length);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/audio/transcriptions"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();
                    
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            System.err.println("Groq STT Error: " + response.body());
                            throw new RuntimeException("Transcription Error");
                        }
                        JsonObject resObj = gson.fromJson(response.body(), JsonObject.class);
                        return resObj.get("text").getAsString();
                    });
        } catch (Exception e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public CompletableFuture<String> summarizeAsync(String title, String content) {
        if (content == null || content.trim().isEmpty()) {
            return CompletableFuture.completedFuture("No content to summarize.");
        }

        String prompt = "Summarize the following news article briefly and professionally. Article Title: " + title + "\n\nContent:\n" + content;

        return callGroqAsync(prompt, "You are a professional assistant that summarizes news articles.");
    }

    public CompletableFuture<String> callGroqAsync(String prompt, String systemInstruction) {
        JsonObject root = new JsonObject();
        root.addProperty("model", MODEL);
        
        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", systemInstruction);
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        root.add("messages", messages);

        String jsonBody = gson.toJson(root);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        System.err.println("Groq API Error (" + response.statusCode() + "): " + response.body());
                        throw new RuntimeException("AI Error (" + response.statusCode() + ")");
                    }

                    try {
                        JsonObject resObj = gson.fromJson(response.body(), JsonObject.class);
                        return resObj.getAsJsonArray("choices")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("message")
                                .get("content").getAsString();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse AI response.");
                    }
                });
    }

    public CompletableFuture<Boolean> isContentSafeAsync(String content) {
        if (content == null || content.trim().isEmpty()) {
            return CompletableFuture.completedFuture(true);
        }

        String prompt = "Analyze the following comment for hate speech, racism, or severe toxicity. Respond with ONLY the word 'SAFE' if it is acceptable, or 'UNSAFE' if it contains hate speech, racism, or severe toxicity.\n\nComment: " + content;

        return callGroqAsync(prompt, "You are a content moderator. Your only job is to return 'SAFE' or 'UNSAFE'. Do not provide explanations.")
                .thenApply(response -> response.trim().equalsIgnoreCase("SAFE"));
    }
}
