package com.carthagegg.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitchService {
    private static final String CLIENT_ID = ConfigManager.get("twitch.client.id");
    private static final String CLIENT_SECRET = ConfigManager.get("twitch.client.secret");
    private static String accessToken = null;

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static synchronized String getAccessToken() throws Exception {
        if (accessToken != null) return accessToken;

        if (CLIENT_ID == null || CLIENT_SECRET == null || CLIENT_ID.isEmpty() || CLIENT_SECRET.isEmpty()) {
            System.err.println("TwitchService: CLIENT_ID or CLIENT_SECRET is missing!");
            throw new Exception("Twitch API credentials missing in config.properties");
        }

        System.out.println("TwitchService: Fetching new access token...");
        
        String url = "https://id.twitch.tv/oauth2/token";
        String body = String.format("client_id=%s&client_secret=%s&grant_type=client_credentials",
                CLIENT_ID, CLIENT_SECRET);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println("Twitch Auth Error: " + response.body());
            throw new Exception("Failed to get Twitch access token. Check your Client ID and Secret.");
        }
        
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        accessToken = json.get("access_token").getAsString();
        return accessToken;
    }

    /**
     * Fetches stream info (if live) and user info (for description/thumbnail).
     */
    public static CompletableFuture<StreamData> getStreamInfo(String channelName) {
        return getStreamInfoInternal(channelName, true);
    }

    private static CompletableFuture<StreamData> getStreamInfoInternal(String channelName, boolean retryOnAuthError) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String token = getAccessToken();
                
                // 1. Get Stream Info (Check if Live)
                String streamUrl = "https://api.twitch.tv/helix/streams?user_login=" + channelName;
                HttpRequest streamRequest = HttpRequest.newBuilder()
                        .uri(URI.create(streamUrl))
                        .header("Client-ID", CLIENT_ID)
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build();

                HttpResponse<String> streamResponse = httpClient.send(streamRequest, HttpResponse.BodyHandlers.ofString());
                
                if (streamResponse.statusCode() == 401 && retryOnAuthError) {
                    System.out.println("TwitchService: Token expired, retrying...");
                    accessToken = null;
                    return getStreamInfoInternal(channelName, false).join();
                }

                JsonObject streamJson = JsonParser.parseString(streamResponse.body()).getAsJsonObject();
                JsonArray streamDataArray = streamJson.getAsJsonArray("data");

                // 2. Get User Info (For Description and Profile Image)
                String userUrl = "https://api.twitch.tv/helix/users?login=" + channelName;
                HttpRequest userRequest = HttpRequest.newBuilder()
                        .uri(URI.create(userUrl))
                        .header("Client-ID", CLIENT_ID)
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build();

                HttpResponse<String> userResponse = httpClient.send(userRequest, HttpResponse.BodyHandlers.ofString());
                if (userResponse.statusCode() != 200) {
                    System.err.println("Twitch API User Error (" + userResponse.statusCode() + "): " + userResponse.body());
                    return null;
                }
                
                JsonObject userJson = JsonParser.parseString(userResponse.body()).getAsJsonObject();
                JsonArray userDataArray = userJson.getAsJsonArray("data");

                if (userDataArray == null || userDataArray.size() == 0) {
                    System.err.println("TwitchService: No user data found for " + channelName);
                    return null; // User not found
                }

                JsonObject user = userDataArray.get(0).getAsJsonObject();
                String loginName = user.get("login").getAsString(); // Technical name (e.g. "shroud")
                String displayName = user.get("display_name").getAsString(); // Display name (e.g. "Shroud")
                String description = user.get("description").getAsString();
                String profileImage = user.get("profile_image_url").getAsString();

                if (streamDataArray != null && streamDataArray.size() > 0) {
                    // STREAM IS LIVE
                    JsonObject stream = streamDataArray.get(0).getAsJsonObject();
                    return new StreamData(
                            stream.get("title").getAsString(),
                            loginName, // Use login name for technical tasks
                            stream.get("thumbnail_url").getAsString().replace("{width}", "1280").replace("{height}", "720"),
                            true,
                            stream.get("viewer_count").getAsInt(),
                            description
                    );
                } else {
                    // STREAM IS OFFLINE
                    return new StreamData(
                            displayName + "'s Stream",
                            loginName, // Use login name for technical tasks
                            profileImage,
                            false,
                            0,
                            description
                    );
                }
            } catch (Exception e) {
                System.err.println("TwitchService Error for " + channelName + ": " + e.getMessage());
                return null;
            }
        });
    }

    public static String extractChannelName(String url) {
        if (url == null || url.isEmpty()) return null;
        
        url = url.trim();
        
        // Improved Regex: Handle URLs with query params, trailing slashes, etc.
        // Group 1: The username
        String pattern = "^(?:https?://)?(?:www\\.)?twitch\\.tv/([a-zA-Z0-9_]{4,25})(?:[/?#].*)?$";
        Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(url);
        
        if (m.find()) {
            return m.group(1);
        }
        
        // If it's just a username
        if (url.matches("^[a-zA-Z0-9_]{4,25}$")) {
            return url;
        }
        
        return null;
    }

    /**
     * Periodically called to update all Twitch streams in the database with real-time stats.
     */
    public static CompletableFuture<Void> updateAllLiveStreams() {
        return CompletableFuture.runAsync(() -> {
            try {
                com.carthagegg.dao.StreamDAO dao = new com.carthagegg.dao.StreamDAO();
                List<com.carthagegg.models.Stream> streams = dao.findAll();
                for (com.carthagegg.models.Stream s : streams) {
                    if ("twitch".equalsIgnoreCase(s.getPlatform())) {
                        StreamData data = getStreamInfo(s.getChannelName()).join();
                        if (data != null) {
                            dao.updateLiveStats(s.getStreamId(), data.isLive, data.viewerCount, data.title, data.thumbnail);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("TwitchService: Background update failed - " + e.getMessage());
            }
        });
    }

    public static class StreamData {
        public final String title;
        public final String channelName;
        public final String thumbnail;
        public final boolean isLive;
        public final int viewerCount;
        public final String description;

        public StreamData(String title, String channelName, String thumbnail, boolean isLive, int viewerCount, String description) {
            this.title = title;
            this.channelName = channelName;
            this.thumbnail = thumbnail;
            this.isLive = isLive;
            this.viewerCount = viewerCount;
            this.description = description;
        }
    }
}
