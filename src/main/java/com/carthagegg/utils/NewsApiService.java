package com.carthagegg.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.carthagegg.models.News;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsApiService {
    private static final String API_KEY = "1feef38ec0fb4b539dd58273437d09d4";
    private static final String API_URL = "https://newsapi.org/v2/everything?q=esports&language=en&sortBy=publishedAt&apiKey=" + API_KEY;

    private final HttpClient httpClient;
    private final Gson gson;
    private final GeminiService geminiService;

    public NewsApiService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.gson = new Gson();
        this.geminiService = new GeminiService();
    }

    public CompletableFuture<List<News>> fetchEsportsNewsAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        System.err.println("NewsAPI Error (" + response.statusCode() + "): " + response.body());
                        return new ArrayList<News>();
                    }

                    List<News> newsList = new ArrayList<>();
                    try {
                        JsonObject resObj = gson.fromJson(response.body(), JsonObject.class);
                        JsonArray articles = resObj.getAsJsonArray("articles");

                        for (int i = 0; i < articles.size(); i++) {
                            JsonObject art = articles.get(i).getAsJsonObject();
                            
                            News news = new News();
                            news.setTitle(art.get("title").isJsonNull() ? "No Title" : art.get("title").getAsString());
                            
                            String description = art.get("description").isJsonNull() ? "" : art.get("description").getAsString();
                            String content = art.get("content").isJsonNull() ? "" : art.get("content").getAsString();
                            
                            // Combine description and content for a better preview, or just use content if it's longer
                            String fullPreview = description;
                            if (content.length() > description.length()) {
                                fullPreview = content;
                            }
                            news.setContent(fullPreview);
                            
                            news.setImage(art.get("urlToImage").isJsonNull() ? "" : art.get("urlToImage").getAsString());
                            news.setUrl(art.get("url").isJsonNull() ? "" : art.get("url").getAsString());
                            news.setCategory("ESPORTS");
                            
                            // Parse date
                            if (!art.get("publishedAt").isJsonNull()) {
                                try {
                                    ZonedDateTime zdt = ZonedDateTime.parse(art.get("publishedAt").getAsString());
                                    news.setPublishedAt(zdt.toLocalDateTime());
                                } catch (Exception e) {
                                    news.setPublishedAt(LocalDateTime.now());
                                }
                            } else {
                                news.setPublishedAt(LocalDateTime.now());
                            }
                            
                            // For external news, we can use the URL as a detail link or store it in a field if available
                            // Since our News model might not have a URL field, we'll stick to the content
                            
                            newsList.add(news);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return newsList;
                });
    }

    public CompletableFuture<String> fetchFullContentAsync(String url, String title) {
        if (url == null || url.isEmpty()) {
            return CompletableFuture.completedFuture("Unable to fetch full content: No URL provided.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() != 200) {
                        return CompletableFuture.completedFuture("Unable to fetch full content from source.");
                    }

                    String html = response.body();
                    // Basic HTML cleaning to remove script and style tags
                    String cleanHtml = html.replaceAll("(?s)<script.*?>.*?</script>", "")
                                         .replaceAll("(?s)<style.*?>.*?</style>", "")
                                         .replaceAll("<[^>]*>", " ")
                                         .replaceAll("\\s+", " ")
                                         .trim();

                    // Truncate to avoid context window issues if necessary, but Groq can handle a lot
                    if (cleanHtml.length() > 15000) {
                        cleanHtml = cleanHtml.substring(0, 15000);
                    }

                    String prompt = "I have fetched the raw text from a news article URL. Please extract and reconstruct the FULL original news article content from this text. Ignore navigation menus, ads, and footer text. Focus on the actual news story. \n\nTitle: " + title + "\n\nRaw Text:\n" + cleanHtml;

                    return geminiService.callGroqAsync(prompt, "You are a professional journalist. Your task is to extract the main news article content from raw web text and present it as a clean, full-length article.");
                });
    }
}
