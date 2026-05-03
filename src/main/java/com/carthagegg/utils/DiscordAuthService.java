package com.carthagegg.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.io.InputStream;
import java.util.Properties;

public class DiscordAuthService {

    private static final Properties config = new Properties();
    static {
        try (InputStream input = DiscordAuthService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
            }
        } catch (IOException ex) {
            System.err.println("Could not load config.properties: " + ex.getMessage());
        }
    }

    private static final String DEFAULT_CLIENT_ID = config.getProperty("discord.client_id", "");
    private static final String DEFAULT_CLIENT_SECRET = config.getProperty("discord.client_secret", "");
    private static final int PREFERRED_LOCAL_PORT = 5318; // Different port from Google
    private static final String CALLBACK_PATH = "/discord/callback";
    private static final String AUTH_URL = "https://discord.com/api/oauth2/authorize";
    private static final String TOKEN_URL = "https://discord.com/api/oauth2/token";
    private static final String USER_INFO_URL = "https://discord.com/api/users/@me";

    public interface DiscordAuthCallback {
        void onSuccess(DiscordUser user);
        void onError(String error);
    }

    public static class DiscordUser {
        public String id;
        public String email;
        public String username;
        public String avatar;
        public String discriminator;
    }

    public static void authenticate(DiscordAuthCallback callback) {
        new Thread(() -> {
            try {
                HttpServer server;
                try {
                    server = HttpServer.create(new InetSocketAddress("127.0.0.1", PREFERRED_LOCAL_PORT), 0);
                } catch (IOException e) {
                    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
                }
                int port = server.getAddress().getPort();
                String redirectUri = "http://127.0.0.1:" + port + CALLBACK_PATH;

                CountDownLatch latch = new CountDownLatch(1);
                final String[] authCode = new String[1];
                final String[] authError = new String[1];
                final String state = generateState();

                server.createContext(CALLBACK_PATH, new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        String query = exchange.getRequestURI().getQuery();
                        String responseText = "Authentication failed. You can close this window.";
                        String code = getQueryParam(query, "code");
                        String error = getQueryParam(query, "error");
                        String gotState = getQueryParam(query, "state");
                        
                        if (code != null && !code.isBlank()) {
                            if (gotState != null && gotState.equals(state)) {
                                authCode[0] = code;
                                responseText = "Authentication successful! You can close this window and return to CarthageGG.";
                            } else {
                                authError[0] = "state_mismatch";
                            }
                        } else if (error != null && !error.isBlank()) {
                            authError[0] = error;
                        }

                        exchange.sendResponseHeaders(200, responseText.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(responseText.getBytes());
                        }
                        latch.countDown();
                    }
                });

                server.start();

                String url = AUTH_URL +
                        "?client_id=" + urlEncode(DEFAULT_CLIENT_ID) +
                        "&redirect_uri=" + urlEncode(redirectUri) +
                        "&response_type=code" +
                        "&scope=" + urlEncode("identify email") +
                        "&state=" + urlEncode(state);

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    callback.onError("Desktop browsing is not supported.");
                    server.stop(0);
                    return;
                }

                boolean completed = latch.await(2, TimeUnit.MINUTES);
                server.stop(0);

                if (!completed) {
                    callback.onError("Authentication timed out.");
                    return;
                }

                if (authError[0] != null) {
                    callback.onError("Discord Auth Error: " + authError[0]);
                    return;
                }

                if (authCode[0] == null) {
                    callback.onError("Failed to retrieve authorization code.");
                    return;
                }

                HttpClient client = HttpClient.newHttpClient();
                String requestBody = "client_id=" + urlEncode(DEFAULT_CLIENT_ID) +
                        "&client_secret=" + urlEncode(DEFAULT_CLIENT_SECRET) +
                        "&grant_type=authorization_code" +
                        "&code=" + urlEncode(authCode[0]) +
                        "&redirect_uri=" + urlEncode(redirectUri);

                HttpRequest tokenRequest = HttpRequest.newBuilder()
                        .uri(URI.create(TOKEN_URL))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
                JsonObject tokenJson = JsonParser.parseString(tokenResponse.body()).getAsJsonObject();

                if (!tokenJson.has("access_token")) {
                    callback.onError("Failed to get access token: " + tokenResponse.body());
                    return;
                }

                String accessToken = tokenJson.get("access_token").getAsString();

                HttpRequest userInfoRequest = HttpRequest.newBuilder()
                        .uri(URI.create(USER_INFO_URL))
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build();

                HttpResponse<String> userInfoResponse = client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());
                JsonObject userJson = JsonParser.parseString(userInfoResponse.body()).getAsJsonObject();

                DiscordUser dUser = new DiscordUser();
                dUser.id = userJson.get("id").getAsString();
                dUser.email = userJson.has("email") ? userJson.get("email").getAsString() : null;
                dUser.username = userJson.get("username").getAsString();
                dUser.avatar = userJson.has("avatar") && !userJson.get("avatar").isJsonNull() ? 
                        "https://cdn.discordapp.com/avatars/" + dUser.id + "/" + userJson.get("avatar").getAsString() + ".png" : null;
                dUser.discriminator = userJson.get("discriminator").getAsString();

                if (dUser.id == null) {
                    callback.onError("Failed to retrieve valid user info from Discord.");
                    return;
                }

                callback.onSuccess(dUser);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Exception during Discord Auth: " + e.getMessage());
            }
        }).start();
    }

    private static String getQueryParam(String query, String key) {
        if (query == null || key == null) return null;
        String[] parts = query.split("&");
        for (String part : parts) {
            int idx = part.indexOf('=');
            if (idx <= 0) continue;
            String k = urlDecode(part.substring(0, idx));
            if (!key.equals(k)) continue;
            return urlDecode(part.substring(idx + 1));
        }
        return null;
    }

    private static String urlDecode(String value) {
        if (value == null) return null;
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String urlEncode(String value) {
        if (value == null) return "";
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String generateState() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
