package com.carthagegg.utils;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class SceneNavigator {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void navigateTo(String fxmlPath) {
        try {
            java.net.URL resource = SceneNavigator.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            if (root instanceof Region region) {
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }
            Scene scene = new Scene(root);

            // Add theme CSS
            java.net.URL cssResource = SceneNavigator.class.getResource("/com/carthagegg/css/theme.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            primaryStage.setScene(scene);

            // Apply FadeIn transition
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (Exception e) {
            e.printStackTrace();
            // Show alert for debugging
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load screen: " + fxmlPath);
            alert.setContentText(e.toString());
            alert.showAndWait();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
