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

public class GoogleAuthService {

    private static final Properties config = new Properties();
    static {
        try (InputStream input = GoogleAuthService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
            }
        } catch (IOException ex) {
            System.err.println("Could not load config.properties: " + ex.getMessage());
        }
    }

    private static final String DEFAULT_CLIENT_ID = config.getProperty("google.client_id", "");
    private static final String DEFAULT_CLIENT_SECRET = config.getProperty("google.client_secret", "");
    private static final String ENV_CLIENT_ID = "CARTHAGEGG_GOOGLE_CLIENT_ID";
    private static final String ENV_CLIENT_SECRET = "CARTHAGEGG_GOOGLE_CLIENT_SECRET";
    private static final int PREFERRED_LOCAL_PORT = 5317;
    private static final String CALLBACK_PATH = "/oauth2callback";
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    public interface GoogleAuthCallback {
        void onSuccess(GoogleUser user);
        void onError(String error);
    }

    public static class GoogleUser {
        public String id;
        public String email;
        public String name;
        public String givenName;
        public String familyName;
        public String picture;
    }

    public static void authenticate(GoogleAuthCallback callback) {
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
                final String[] authState = new String[1];

                String codeVerifier = generateCodeVerifier();
                String codeChallenge = generateCodeChallenge(codeVerifier);
                String state = generateState();

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
                                authState[0] = gotState;
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

                String clientId = resolveClientId();
                String clientSecret = resolveClientSecret();

                String url = AUTH_URL +
                        "?client_id=" + urlEncode(clientId) +
                        "&redirect_uri=" + urlEncode(redirectUri) +
                        "&response_type=code" +
                        "&scope=" + urlEncode("email profile") +
                        "&access_type=offline" +
                        "&code_challenge=" + urlEncode(codeChallenge) +
                        "&code_challenge_method=S256" +
                        "&state=" + urlEncode(state);

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    callback.onError("Desktop browsing is not supported. Cannot open Google Sign-In.");
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
                    if ("state_mismatch".equals(authError[0])) {
                        callback.onError("Google Sign-In failed due to an invalid callback state. Please try again.");
                    } else {
                        callback.onError("Google Auth Error: " + authError[0]);
                    }
                    return;
                }

                if (authCode[0] == null) {
                    callback.onError("Failed to retrieve authorization code.");
                    return;
                }

                HttpClient client = HttpClient.newHttpClient();
                StringBuilder requestBody = new StringBuilder();
                requestBody.append("code=").append(urlEncode(authCode[0]));
                requestBody.append("&client_id=").append(urlEncode(clientId));
                if (clientSecret != null && !clientSecret.isBlank()) {
                    requestBody.append("&client_secret=").append(urlEncode(clientSecret));
                }
                requestBody.append("&redirect_uri=").append(urlEncode(redirectUri));
                requestBody.append("&grant_type=authorization_code");
                requestBody.append("&code_verifier=").append(urlEncode(codeVerifier));

                HttpRequest tokenRequest = HttpRequest.newBuilder()
                        .uri(URI.create(TOKEN_URL))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
                JsonObject tokenJson = JsonParser.parseString(tokenResponse.body()).getAsJsonObject();

                if (!tokenJson.has("access_token")) {
                    String desc = tokenJson.has("error_description") ? tokenJson.get("error_description").getAsString() : tokenResponse.body();
                    if (desc != null && desc.toLowerCase().contains("client_secret is missing")) {
                        callback.onError(
                                "Google token exchange requires a client secret for your current OAuth client. " +
                                        "Set the environment variable CARTHAGEGG_GOOGLE_CLIENT_SECRET when launching the app, " +
                                        "or create a Desktop OAuth client ID in Google Cloud Console (recommended)."
                        );
                    } else if (port != PREFERRED_LOCAL_PORT && clientSecret != null && !clientSecret.isBlank()) {
                        callback.onError(
                                "Google token exchange failed. If you are using a Web OAuth client, configure an authorized redirect URI " +
                                        "that matches exactly: http://127.0.0.1:" + PREFERRED_LOCAL_PORT + CALLBACK_PATH + " and try again."
                        );
                    } else {
                        callback.onError("Failed to get access token: " + tokenResponse.body());
                    }
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

                GoogleUser gUser = new GoogleUser();
                gUser.id = userJson.has("id") ? userJson.get("id").getAsString() : null;
                gUser.email = userJson.has("email") ? userJson.get("email").getAsString() : null;
                gUser.name = userJson.has("name") ? userJson.get("name").getAsString() : null;
                gUser.givenName = userJson.has("given_name") ? userJson.get("given_name").getAsString() : null;
                gUser.familyName = userJson.has("family_name") ? userJson.get("family_name").getAsString() : null;
                gUser.picture = userJson.has("picture") ? userJson.get("picture").getAsString() : null;

                if (gUser.email == null || gUser.id == null) {
                    callback.onError("Failed to retrieve valid user info from Google.");
                    return;
                }

                callback.onSuccess(gUser);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Exception during Google Auth: " + e.getMessage());
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

    private static String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String generateCodeChallenge(String codeVerifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private static String generateState() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String resolveClientId() {
        String env = System.getenv(ENV_CLIENT_ID);
        if (env != null && !env.isBlank()) return env.trim();
        return DEFAULT_CLIENT_ID;
    }

    private static String resolveClientSecret() {
        String env = System.getenv(ENV_CLIENT_SECRET);
        if (env != null && !env.isBlank()) return env.trim();
        return DEFAULT_CLIENT_SECRET;
    }
}