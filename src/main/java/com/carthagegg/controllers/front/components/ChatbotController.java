package com.carthagegg.controllers.front.components;

import com.carthagegg.utils.BlazeAPIService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class ChatbotController {

    @FXML private VBox chatWindow;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox messageList;
    @FXML private TextField inputField;
    @FXML private Button toggleBtn;
    @FXML private FontIcon toggleIcon;

    private final BlazeAPIService apiService = new BlazeAPIService();
    private boolean isChatOpen = false;

    @FXML
    public void initialize() {
        // Add welcome message
        addMessage("Hello! I'm CarthageGG AI. How can I help you today?", false);
    }

    @FXML
    private void toggleChat() {
        isChatOpen = !isChatOpen;
        chatWindow.setVisible(isChatOpen);
        chatWindow.setManaged(isChatOpen);
        
        if (isChatOpen) {
            toggleIcon.setIconLiteral("fas-chevron-down");
            inputField.requestFocus();
        } else {
            toggleIcon.setIconLiteral("fas-comment-dots");
        }
    }

    @FXML
    private void handleSendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        // User message
        addMessage(message, true);
        inputField.clear();

        // AI Response (async)
        new Thread(() -> {
            String response = apiService.sendMessage(message);
            Platform.runLater(() -> addMessage(response, false));
        }).start();
    }

    private void addMessage(String text, boolean isUser) {
        HBox container = new HBox();
        container.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(250);
        
        String style = isUser ? 
            "-fx-background-color: #FFC107; -fx-text-fill: #0a0a0b; -fx-padding: 10 15; -fx-background-radius: 15 15 2 15; -fx-font-size: 13;" :
            "-fx-background-color: #141416; -fx-text-fill: #FFFFFF; -fx-padding: 10 15; -fx-background-radius: 15 15 15 2; -fx-border-color: #232326; -fx-border-width: 1; -fx-font-size: 13;";
        
        label.setStyle(style);
        container.getChildren().add(label);
        
        messageList.getChildren().add(container);
        
        // Auto scroll to bottom
        scrollPane.layout();
        scrollPane.setVvalue(1.0);
    }
}
