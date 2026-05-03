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
        navigateTo(fxmlPath, null);
    }

    public static <T> T navigateTo(String fxmlPath, Object data) {
        try {
            java.net.URL resource = SceneNavigator.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            
            T controller = loader.getController();
            
            // Wrap in StackPane to add Chatbot if it's a front-end page
            if (fxmlPath.contains("/front/") && !fxmlPath.contains("/components/")) {
                try {
                    java.net.URL chatResource = SceneNavigator.class.getResource("/com/carthagegg/fxml/front/components/Chatbot.fxml");
                    if (chatResource != null) {
                        FXMLLoader chatLoader = new FXMLLoader(chatResource);
                        javafx.scene.Node chatbot = chatLoader.load();
                        
                        javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane();
                        wrapper.getChildren().addAll(root, chatbot);
                        root = wrapper;
                    }
                } catch (IOException e) {
                    System.err.println("Could not load chatbot: " + e.getMessage());
                }
            }

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
            
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            // Show alert for debugging
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load screen: " + fxmlPath);
            alert.setContentText(e.toString());
            alert.showAndWait();
            return null;
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
