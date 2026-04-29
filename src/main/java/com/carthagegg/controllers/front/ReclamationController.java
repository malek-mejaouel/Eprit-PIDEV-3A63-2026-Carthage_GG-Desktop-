package com.carthagegg.controllers.front;

import com.carthagegg.dao.ReclamationDAO;
import com.carthagegg.dao.ReclamationMessageDAO;
import com.carthagegg.models.Reclamation;
import com.carthagegg.models.ReclamationMessage;
import com.carthagegg.models.User;
import com.carthagegg.utils.SentimentAnalyzer;
import com.carthagegg.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReclamationController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;
    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, String> colDate;
    @FXML private TableColumn<Reclamation, String> colTitle;
    @FXML private TableColumn<Reclamation, String> colStatus;
    @FXML private TableColumn<Reclamation, Void> colActions;
    @FXML private SidebarController sidebarController;

    private ReclamationDAO reclamationDAO = new ReclamationDAO();
    private ReclamationMessageDAO messageDAO = new ReclamationMessageDAO();
    private ObservableList<Reclamation> reclamationList = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("reclamation");
        }
        setupTable();
        loadReclamations();
    }

    private void setupTable() {
        colDate.setCellValueFactory(cellData -> {
            Reclamation r = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(r.getCreatedAt().format(formatter));
        });
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    switch (item.toLowerCase()) {
                        case "pending": setStyle("-fx-text-fill: #fbbf24;"); break;
                        case "in_progress": setStyle("-fx-text-fill: #00f0ff;"); break;
                        case "resolved": setStyle("-fx-text-fill: #10b981;"); break;
                        default: setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<Reclamation, Void>() {
            private final Button chatBtn = new Button("View Chat");
            
            {
                chatBtn.setStyle("-fx-background-color: #00f0ff; -fx-text-fill: black; -fx-font-size: 10;");
                chatBtn.setOnAction(event -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    openChatWindow(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : chatBtn);
            }
        });
    }

    private void openChatWindow(Reclamation r) {
        Stage stage = new Stage();
        stage.setTitle("Chat with Support - Ticket #" + r.getId());

        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(15));
        chatBox.setStyle("-fx-background-color: #0d0d15;");

        ScrollPane scrollPane = new ScrollPane();
        VBox messageList = new VBox(10);
        messageList.setPadding(new Insets(10));
        messageList.setStyle("-fx-background-color: transparent;");
        scrollPane.setContent(messageList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: #0d0d15; -fx-background-color: transparent;");

        TextField inputField = new TextField();
        inputField.setPromptText("Type your message...");
        inputField.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-border-color: #00f0ff;");

        Button sendBtn = new Button("Send Message");
        sendBtn.setStyle("-fx-background-color: #00f0ff; -fx-text-fill: black;");
        sendBtn.setMaxWidth(Double.MAX_VALUE);

        Runnable refreshMessages = () -> {
            try {
                List<ReclamationMessage> messages = messageDAO.findByReclamationId(r.getId());
                Platform.runLater(() -> {
                    messageList.getChildren().clear();
                    
                    // Original description as first message
                    VBox descContainer = new VBox(2);
                    descContainer.setAlignment(Pos.CENTER_LEFT);
                    Label descHeader = new Label("Original Ticket Description");
                    descHeader.setStyle("-fx-font-size: 10; -fx-text-fill: #94a3b8;");
                    Label descContent = new Label(r.getDescription());
                    descContent.setWrapText(true);
                    descContent.setStyle("-fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 5; -fx-background-color: #1e1e2e;");
                    descContainer.getChildren().addAll(descHeader, descContent);
                    messageList.getChildren().add(descContainer);

                    for (ReclamationMessage m : messages) {
                        VBox msgContainer = new VBox(2);
                        Label senderLbl = new Label(m.getSenderUsername() + " • " + m.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")));
                        senderLbl.setStyle("-fx-font-size: 10; -fx-text-fill: #94a3b8;");
                        
                        Label contentLbl = new Label(m.getMessage());
                        contentLbl.setWrapText(true);
                        contentLbl.setStyle("-fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 5;");
                        
                        if (m.getSenderId() == SessionManager.getCurrentUser().getUserId()) {
                            msgContainer.setAlignment(Pos.CENTER_RIGHT);
                            contentLbl.setStyle(contentLbl.getStyle() + "-fx-background-color: #00f0ff; -fx-text-fill: black;");
                        } else {
                            msgContainer.setAlignment(Pos.CENTER_LEFT);
                            contentLbl.setStyle(contentLbl.getStyle() + "-fx-background-color: #1e1e2e;");
                        }
                        
                        msgContainer.getChildren().addAll(senderLbl, contentLbl);
                        messageList.getChildren().add(msgContainer);
                    }
                    scrollPane.setVvalue(1.0);
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };

        sendBtn.setOnAction(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                try {
                    ReclamationMessage msg = new ReclamationMessage();
                    msg.setReclamationId(r.getId());
                    msg.setSenderId(SessionManager.getCurrentUser().getUserId());
                    msg.setMessage(text);
                    messageDAO.save(msg);
                    inputField.clear();
                    refreshMessages.run();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        chatBox.getChildren().addAll(new Label("Status: " + r.getStatus().toUpperCase()), scrollPane, inputField, sendBtn);
        
        Scene scene = new Scene(chatBox, 400, 550);
        stage.setScene(scene);
        refreshMessages.run();
        stage.show();
    }

    private void loadReclamations() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            try {
                List<Reclamation> list = reclamationDAO.findByUserId(user.getUserId());
                reclamationList.setAll(list);
                reclamationTable.setItems(reclamationList);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSubmit() {
        clearError();
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        User user = SessionManager.getCurrentUser();

        if (title.isEmpty() || description.isEmpty()) {
            showError("Please fill in both title and description");
            return;
        }

        if (user != null) {
            try {
                Reclamation r = new Reclamation();
                r.setUserId(user.getUserId());
                r.setTitle(title);
                r.setDescription(description);
                
                // AI/Keyword Sentiment & Urgency Analysis
                String priority = SentimentAnalyzer.analyzePriority(description);
                r.setPriority(priority);
                
                reclamationDAO.save(r);
                
                titleField.clear();
                descriptionArea.clear();
                loadReclamations();
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Reclamation submitted successfully!");
                success.show();
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleDelete(Reclamation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete this reclamation?");
        alert.setContentText("This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reclamationDAO.delete(r.getId());
                    loadReclamations();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
