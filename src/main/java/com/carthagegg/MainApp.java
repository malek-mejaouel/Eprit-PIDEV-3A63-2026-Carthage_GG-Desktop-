package com.carthagegg;

import com.carthagegg.utils.SceneNavigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class
MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneNavigator.setPrimaryStage(primaryStage);
        primaryStage.setTitle("CarthageGG");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(750);
        primaryStage.setResizable(true);

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

    public static void main(String[] args) {
        launch(args);
    }
}
