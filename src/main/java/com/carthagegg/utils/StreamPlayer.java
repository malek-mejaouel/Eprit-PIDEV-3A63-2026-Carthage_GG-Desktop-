package com.carthagegg.utils;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class StreamPlayer {

    public static void playStream(String platform, String channelName, String ytId) {
        Stage stage = new Stage();
        stage.setTitle("Watching: " + (channelName != null ? channelName : "Stream"));
        stage.setMinWidth(960);
        stage.setMinHeight(540);

        WebView webView = new WebView();
        // Set a modern User-Agent to prevent Twitch from blocking the embedded player
        webView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        
        if ("twitch".equalsIgnoreCase(platform)) {
            // Clean the channel name
            String cleanName = channelName.toLowerCase().trim().replaceAll("\\s+", "");
            
            // Twitch's player requires a 'parent' domain to work. 
            // Since we are in a desktop app (no real domain), we use a trick:
            // We use 'twitch.tv' as a parent and load the page through a simple redirection.
            String embedUrl = "https://player.twitch.tv/?channel=" + cleanName + "&parent=twitch.tv&autoplay=true&muted=false";
            webView.getEngine().load(embedUrl);
        } else if ("youtube".equalsIgnoreCase(platform)) {
            String embedUrl = "https://www.youtube.com/embed/" + ytId + "?autoplay=1";
            webView.getEngine().load(embedUrl);
        }

        BorderPane root = new BorderPane(webView);
        Scene scene = new Scene(root, 1280, 720);
        
        stage.setScene(scene);
        
        // Try to load app icon
        try {
            java.io.InputStream iconStream = StreamPlayer.class.getResourceAsStream("/images/zz.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {}

        // Stop the webview when the window is closed to stop audio/video
        stage.setOnCloseRequest(e -> {
            webView.getEngine().load("about:blank");
        });

        stage.show();
    }
}
