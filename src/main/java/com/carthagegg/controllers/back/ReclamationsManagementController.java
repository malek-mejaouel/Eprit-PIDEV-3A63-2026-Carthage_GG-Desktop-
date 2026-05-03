package com.carthagegg.controllers.back;

import com.carthagegg.dao.ReclamationDAO;
import com.carthagegg.dao.ReclamationMessageDAO;
import com.carthagegg.models.Reclamation;
import com.carthagegg.models.ReclamationMessage;
import com.carthagegg.dao.UserDAO;
import com.carthagegg.models.User;
import com.carthagegg.utils.ImageGenerator;
import com.carthagegg.utils.SceneNavigator;
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

public class ReclamationsManagementController {

    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colUsername;
    @FXML private TableColumn<Reclamation, String> colTitle;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatus;
    @FXML private TableColumn<Reclamation, String> colPriority;
    @FXML private TableColumn<Reclamation, Void> colActions;
    @FXML private ComboBox<String> statusFilter;

    private ReclamationDAO reclamationDAO = new ReclamationDAO();
    private ReclamationMessageDAO messageDAO = new ReclamationMessageDAO();
    private UserDAO userDAO = new UserDAO();
    private ObservableList<Reclamation> reclamationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupFilters();
        setupTable();
        loadReclamations();
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "PENDING", "IN_PROGRESS", "RESOLVED"));
        statusFilter.setValue("All");
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadReclamations());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        
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

        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colPriority.setCellFactory(column -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    if ("high".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;"); // Red for High Priority
                    } else {
                        setStyle("-fx-text-fill: #94a3b8;"); // Dim for normal
                    }
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<Reclamation, Void>() {
            private final Button chatBtn = new Button("Chat");
            private final Button progressBtn = new Button("Progress");
            private final Button resolveBtn = new Button("Resolve");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, chatBtn, progressBtn, resolveBtn, deleteBtn);

            {
                chatBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 10;");
                progressBtn.setStyle("-fx-background-color: #00f0ff; -fx-text-fill: black; -fx-font-size: 10;");
                resolveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 10;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10;");

                chatBtn.setOnAction(event -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    openChatWindow(r);
                });

                resolveBtn.setOnAction(event -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(r, "resolved");
                });

                deleteBtn.setOnAction(event -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    handleDelete(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadReclamations() {
        try {
            List<Reclamation> all = reclamationDAO.findAll();
            String filter = statusFilter.getValue();
            
            if (filter != null && !filter.equals("All")) {
                all = all.stream()
                        .filter(r -> r.getStatus().equalsIgnoreCase(filter))
                        .toList();
            }
            
            reclamationList.setAll(all);
            reclamationTable.setItems(reclamationList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleUpdateStatus(Reclamation r, String status) {
        try {
            reclamationDAO.updateStatus(r.getId(), status);
            loadReclamations();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Reclamation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete this reclamation?");
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

    private void openChatWindow(Reclamation r) {
        Stage stage = new Stage();
        stage.setTitle("Reclamation Chat - #" + r.getId());

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
        inputField.setPromptText("Type your reply...");
        inputField.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-border-color: #00f0ff;");

        Button sendBtn = new Button("Send Reply");
        sendBtn.setStyle("-fx-background-color: #00f0ff; -fx-text-fill: black;");
        sendBtn.setMaxWidth(Double.MAX_VALUE);

        Button genBadgeBtn = new Button("Generate Verification Badge");
        genBadgeBtn.setStyle("-fx-background-color: #fbbf24; -fx-text-fill: black; -fx-font-weight: bold;");
        genBadgeBtn.setMaxWidth(Double.MAX_VALUE);

        Runnable refreshMessages = () -> {
            try {
                List<ReclamationMessage> messages = messageDAO.findByReclamationId(r.getId());
                Platform.runLater(() -> {
                    messageList.getChildren().clear();
                    for (ReclamationMessage m : messages) {
                        VBox msgContainer = new VBox(2);
                        Label senderLbl = new Label(m.getSenderUsername() + " • " + m.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")));
                        senderLbl.setStyle("-fx-font-size: 10; -fx-text-fill: #94a3b8;");
                        
                        Label contentLbl = new Label(m.getMessage());
                        contentLbl.setWrapText(true);
                        contentLbl.setStyle("-fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 5;");
                        
                        if (m.getSenderUsername().equals(r.getUsername())) {
                            msgContainer.setAlignment(Pos.CENTER_LEFT);
                            contentLbl.setStyle(contentLbl.getStyle() + "-fx-background-color: #1e1e2e;");
                        } else {
                            msgContainer.setAlignment(Pos.CENTER_RIGHT);
                            contentLbl.setStyle(contentLbl.getStyle() + "-fx-background-color: #00f0ff; -fx-text-fill: black;");
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

        genBadgeBtn.setOnAction(e -> {
            try {
                User targetUser = userDAO.findById(r.getUserId());
                if (targetUser != null) {
                    String role = formatRoles(targetUser.getRoles());
                    String path = ImageGenerator.generateCertification(targetUser.getEmail(), role);
                    
                    if (path != null) {
                        // Automatically send a message to the user with the badge path
                        ReclamationMessage msg = new ReclamationMessage();
                        msg.setReclamationId(r.getId());
                        msg.setSenderId(SessionManager.getCurrentUser().getUserId());
                        msg.setMessage("Hello! I have generated your verification badge. You can download it here and upload it to your profile to verify your account:\n" + path);
                        messageDAO.save(msg);
                        
                        refreshMessages.run();
                        
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Badge Generated");
                        alert.setHeaderText("Verification badge successfully generated!");
                        alert.setContentText("The user has been notified in the chat with the file path.");
                        alert.showAndWait();
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        chatBox.getChildren().addAll(new Label("Ticket: " + r.getTitle()), scrollPane, inputField, sendBtn, genBadgeBtn);
        
        Scene scene = new Scene(chatBox, 400, 600);
        stage.setScene(scene);
        refreshMessages.run();
        stage.show();
    }

    private String formatRoles(String rolesJson) {
        if (rolesJson == null || rolesJson.isEmpty()) return "USER";
        return rolesJson.replace("[", "")
                       .replace("]", "")
                       .replace("\"", "")
                       .replace("ROLE_", "")
                       .replace(",", ", ");
    }

    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
