package com.carthagegg.controllers.back;

import com.carthagegg.dao.UserDAO;
import com.carthagegg.models.User;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UsersManagementController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Boolean> colActive;
    @FXML private TableColumn<User, Void> colActions;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> activeFilter;
    @FXML private ComboBox<String> sortOrder;

    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private ObservableList<User> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Guard check for admin
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupFilters();
        setupTable();
        loadUsers();
    }

    private void setupFilters() {
        activeFilter.setItems(FXCollections.observableArrayList("All Status", "Active", "Inactive"));
        activeFilter.setValue("All Status");
        
        sortOrder.setItems(FXCollections.observableArrayList("ID Ascending", "ID Descending"));
        sortOrder.setValue("ID Descending");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        activeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        sortOrder.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        // Custom Role display
        colRole.setCellValueFactory(new PropertyValueFactory<>("roles"));
        colRole.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String roles, boolean empty) {
                super.updateItem(roles, empty);
                if (empty || roles == null) {
                    setText(null);
                } else {
                    setText(formatRoles(roles));
                }
            }
        });

        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        
        // Actions column with buttons
        colActions.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button deleteBtn = new Button("Delete");
            private final Button banBtn = new Button("Ban");
            private final HBox pane = new HBox(5, banBtn, deleteBtn);

            {
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                banBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white;");
                
                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });

                banBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleBanUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
                        banBtn.setText("Unban");
                        banBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");
                    } else {
                        banBtn.setText("Ban");
                        banBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white;");
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    private String formatRoles(String rolesJson) {
        if (rolesJson == null || rolesJson.isEmpty()) return "USER";
        // Simple regex to clean up ["ROLE_ADMIN"] into ADMIN
        return rolesJson.replace("[", "")
                       .replace("]", "")
                       .replace("\"", "")
                       .replace("ROLE_", "")
                       .replace(",", ", ");
    }

    private void loadUsers() {
        try {
            List<User> users = userDAO.findAll();
            usersList.setAll(users);
            
            // Dynamically populate roles filter
            java.util.Set<String> roles = new java.util.HashSet<>();
            roles.add("All Roles");
            for (User u : users) {
                String formatted = formatRoles(u.getRoles());
                for (String r : formatted.split(", ")) {
                    if (!r.isEmpty()) roles.add(r);
                }
            }
            roleFilter.setItems(FXCollections.observableArrayList(roles).sorted());
            roleFilter.setValue("All Roles");

            applyFilters();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String selectedRole = roleFilter.getValue();
        String selectedStatus = activeFilter.getValue();
        String selectedSort = sortOrder.getValue();

        List<User> filtered = usersList.stream()
            .filter(user -> {
                boolean matchesSearch = user.getUsername().toLowerCase().contains(searchText) || 
                                      user.getEmail().toLowerCase().contains(searchText);
                
                boolean matchesRole = "All Roles".equals(selectedRole) || 
                                    (user.getRoles() != null && user.getRoles().contains(selectedRole.toUpperCase())) ||
                                    (user.getRoles() != null && user.getRoles().contains("ROLE_" + selectedRole.toUpperCase()));
                
                boolean matchesStatus = selectedStatus.equals("All Status") || 
                                      (selectedStatus.equals("Active") && user.isActive()) ||
                                      (selectedStatus.equals("Inactive") && !user.isActive());
                
                return matchesSearch && matchesRole && matchesStatus;
            })
            .sorted((u1, u2) -> {
                if ("ID Ascending".equals(selectedSort)) {
                    return Integer.compare(u1.getUserId(), u2.getUserId());
                } else {
                    return Integer.compare(u2.getUserId(), u1.getUserId());
                }
            })
            .toList();

        filteredList.setAll(filtered);
        usersTable.setItems(filteredList);
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete user " + user.getUsername() + "?");
        alert.setContentText("This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userDAO.delete(user.getUserId());
                    usersList.remove(user);
                    applyFilters();
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setHeaderText(null);
                    success.setContentText("User deleted successfully!");
                    success.show();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Could not delete user");
                    error.setContentText("Database error: " + e.getMessage());
                    error.show();
                }
            }
        });
    }

    private void handleBanUser(User user) {
        if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
            // Unban logic
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Unban");
            alert.setHeaderText("Unban user " + user.getUsername() + "?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        userDAO.banUser(user.getUserId(), LocalDateTime.now().minusDays(1), "Unbanned by admin");
                        loadUsers(); // Refresh
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            return;
        }

        // Ban logic with Dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ban User: " + user.getUsername());
        dialog.setHeaderText("Specify ban duration and reason");

        ButtonType banButtonType = new ButtonType("Ban", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(banButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        ComboBox<String> durationBox = new ComboBox<>(FXCollections.observableArrayList(
            "1 Hour", "24 Hours", "3 Days", "7 Days", "30 Days", "Permanent"
        ));
        durationBox.setValue("24 Hours");
        
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Reason for ban...");
        reasonArea.setPrefRowCount(3);

        content.getChildren().addAll(new Label("Duration:"), durationBox, new Label("Reason:"), reasonArea);
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(response -> {
            if (response == banButtonType) {
                String duration = durationBox.getValue();
                String reason = reasonArea.getText().trim();
                if (reason.isEmpty()) reason = "No reason provided";

                LocalDateTime until = switch (duration) {
                    case "1 Hour" -> LocalDateTime.now().plusHours(1);
                    case "24 Hours" -> LocalDateTime.now().plusDays(1);
                    case "3 Days" -> LocalDateTime.now().plusDays(3);
                    case "7 Days" -> LocalDateTime.now().plusDays(7);
                    case "30 Days" -> LocalDateTime.now().plusDays(30);
                    case "Permanent" -> LocalDateTime.now().plusYears(100);
                    default -> LocalDateTime.now().plusDays(1);
                };

                try {
                    userDAO.banUser(user.getUserId(), until, reason);
                    loadUsers(); // Refresh
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
