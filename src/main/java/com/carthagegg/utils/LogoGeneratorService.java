package com.carthagegg.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Service to generate team logos using Pollinations AI (Free, no API key required).
 */
public class LogoGeneratorService {
    private static final String API_URL = "https://image.pollinations.ai/prompt/";
    private final HttpClient client;

    public LogoGeneratorService() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Generates a logo for a team asynchronously using Pollinations AI.
     * @param teamName Name of the team
     * @param customPrompt Custom prompt instructions from the user
     * @return CompletableFuture with the byte array of the image
     */
    public CompletableFuture<byte[]> generateLogoAsync(String teamName, String customPrompt) {
        String finalPrompt;
        
        if (customPrompt != null && !customPrompt.trim().isEmpty()) {
            // User provided a prompt, use it as the primary instruction
            finalPrompt = customPrompt;
            
            // Add some context and quality keywords if they aren't already there
            if (!finalPrompt.toLowerCase().contains(teamName.toLowerCase())) {
                finalPrompt += " for team " + teamName;
            }
            if (!finalPrompt.toLowerCase().contains("logo")) {
                finalPrompt += ", professional esports logo";
            }
            finalPrompt += ", modern design, clean, vector style, white background";
        } else {
            // Default prompt if no custom prompt is provided
            finalPrompt = "Professional esports team logo for team " + teamName 
                + ", modern design, clean, vector style, white background";
        }
        
        String encodedPrompt = URLEncoder.encode(finalPrompt, StandardCharsets.UTF_8);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + encodedPrompt))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else {
                        throw new RuntimeException("Logo generation failed: " + response.statusCode());
                    }
                });
    }

    /**
     * Saves the generated logo to a temporary file.
     */
    public Path saveLogoLocally(byte[] imageData, String teamName) throws IOException {
        Path dir = Path.of(System.getProperty("user.home"), "CarthageGG", "temp");
        Files.createDirectories(dir);
        Path target = dir.resolve("generated_logo_" + System.currentTimeMillis() + ".png");
        Files.write(target, imageData);
        return target;
    }
}
