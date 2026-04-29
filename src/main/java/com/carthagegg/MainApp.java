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

public class
MainApp extends Application {

    private ScheduledExecutorService twitchUpdater;

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneNavigator.setPrimaryStage(primaryStage);
        primaryStage.setTitle("CarthageGG");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(750);
        primaryStage.setResizable(true);

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
        Platform.runLater(() -> primaryStage.setMaximized(true));
    }

    private void startBackgroundTasks() {
        twitchUpdater = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // Ensure thread dies when app closes
            return t;
        });

        // Update Twitch streams every 5 minutes
        twitchUpdater.scheduleAtFixedRate(() -> {
            System.out.println("MainApp: Auto-updating Twitch live data...");
            TwitchService.updateAllLiveStreams();
        }, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    public void stop() {
        if (twitchUpdater != null) {
            twitchUpdater.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
