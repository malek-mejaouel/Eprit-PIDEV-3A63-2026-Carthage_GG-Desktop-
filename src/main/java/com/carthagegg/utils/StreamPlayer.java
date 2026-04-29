package com.carthagegg.utils;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class StreamPlayer {

    public static void playStream(String platform, String channelName, String ytId) {
        Stage stage = new Stage();
        stage.setTitle("Watching: " + channelName);
        stage.setMinWidth(960);
        stage.setMinHeight(540);

        WebView webView = new WebView();
        String embedUrl = "";

        if ("twitch".equalsIgnoreCase(platform)) {
            // Twitch embed URL with parent parameter (localhost for development)
            embedUrl = "https://player.twitch.tv/?channel=" + channelName + "&parent=localhost&autoplay=true";
        } else if ("youtube".equalsIgnoreCase(platform)) {
            // YouTube embed URL
            embedUrl = "https://www.youtube.com/embed/" + ytId + "?autoplay=1";
        }

        if (!embedUrl.isEmpty()) {
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

        // Stop the webview when the window is closed
        stage.setOnCloseRequest(e -> {
            webView.getEngine().load("about:blank");
        });

        stage.show();
    }
}
