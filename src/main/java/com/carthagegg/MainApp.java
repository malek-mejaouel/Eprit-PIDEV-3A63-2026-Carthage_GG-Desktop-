package com.carthagegg;

import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.TwitchService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainApp extends Application {

    private ScheduledExecutorService twitchUpdater;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set user agent to avoid potential HTTP 403 errors when fetching resources
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");

        SceneNavigator.setPrimaryStage(primaryStage);
        primaryStage.setTitle("CarthageGG");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(750);
        primaryStage.setResizable(true);

        // Start background tasks (Twitch auto-updates)
        startBackgroundTasks();

        // Load app icon if exists
        try {
            java.io.InputStream iconStream = getClass().getResourceAsStream("/images/zz.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            // Ignore icon errors silently or log them
        }

        // Start at Sign In
        SceneNavigator.navigateTo("/com/carthagegg/fxml/auth/SignIn.fxml");
        primaryStage.show();

        // Maximize the window after showing
        Platform.runLater(() -> primaryStage.setMaximized(true));
    }

    /**
     * Initializes the background executor for periodic tasks.
     */
    private void startBackgroundTasks() {
        twitchUpdater = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // Ensure thread dies when app closes
            return t;
        });

        // Update Twitch streams every 5 minutes
        twitchUpdater.scheduleAtFixedRate(() -> {
            System.out.println("MainApp: Auto-updating Twitch live data...");
            try {
                TwitchService.updateAllLiveStreams();
            } catch (Exception e) {
                System.err.println("Failed to update Twitch streams: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    public void stop() {
        // Properly shutdown the executor service when the application exits
        if (twitchUpdater != null) {
            twitchUpdater.shutdown();
            try {
                if (!twitchUpdater.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    twitchUpdater.shutdownNow();
                }
            } catch (InterruptedException e) {
                twitchUpdater.shutdownNow();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}