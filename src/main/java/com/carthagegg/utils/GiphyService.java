package com.carthagegg.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GiphyService {
    private static final String API_KEY = "deTknpNogfQ0lppTqPqM5Gi6befQLuQH";
    private static final String BASE_URL = "https://api.giphy.com/v1/gifs";

    public static List<String> searchGifs(String query) {
        return fetchGifs(BASE_URL + "/search?api_key=" + API_KEY + "&q=" + query + "&limit=20&rating=g");
    }

    public static List<String> getTrendingGifs() {
        return fetchGifs(BASE_URL + "/trending?api_key=" + API_KEY + "&limit=20&rating=g");
    }

    private static List<String> fetchGifs(String urlString) {
        List<String> gifUrls = new ArrayList<>();
        try {
            URL url = new URL(urlString.replace(" ", "%20"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) {
                return gifUrls;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            JsonObject response = JsonParser.parseString(content.toString()).getAsJsonObject();
            JsonArray data = response.getAsJsonArray("data");
            System.out.println("GiphyService: Found " + data.size() + " GIFs");
            for (JsonElement element : data) {
                try {
                    JsonObject gif = element.getAsJsonObject();
                    JsonObject images = gif.getAsJsonObject("images");
                    // Use fixed_height_small for faster loading in the picker
                    String gifUrl = images.getAsJsonObject("fixed_height_small").get("url").getAsString();
                    gifUrls.add(gifUrl);
                } catch (Exception e) {
                    // Fallback to fixed_height if small is not available
                    try {
                        String gifUrl = element.getAsJsonObject().getAsJsonObject("images").getAsJsonObject("fixed_height").get("url").getAsString();
                        gifUrls.add(gifUrl);
                    } catch (Exception e2) {}
                }
            }
        } catch (Exception e) {
            System.err.println("GiphyService Error: " + e.getMessage());
        }
        return gifUrls;
    }
}
