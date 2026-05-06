package com.carthagegg.utils;

import java.awt.Desktop;
import java.net.URI;

public class StreamPlayer {

    /**
     * Opens the stream in the system's default web browser.
     * This is the most reliable way to play Twitch/YouTube streams in a desktop environment.
     */
    public static void playStream(String platform, String channelName, String ytId) {
        try {
            String url = "";
            if ("twitch".equalsIgnoreCase(platform)) {
                // Use the clean technical login name
                String cleanName = (channelName != null) ? channelName.toLowerCase().trim().replaceAll("\\s+", "") : "";
                url = "https://www.twitch.tv/" + cleanName;
            } else if ("youtube".equalsIgnoreCase(platform)) {
                url = "https://www.youtube.com/watch?v=" + ytId;
            }

            if (!url.isEmpty()) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    System.err.println("StreamPlayer Error: Desktop browsing is not supported on this platform.");
                }
            }
        } catch (Exception e) {
            System.err.println("StreamPlayer Error: Could not open browser - " + e.getMessage());
        }
    }
}
