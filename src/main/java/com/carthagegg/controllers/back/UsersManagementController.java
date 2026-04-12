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

import java.sql.SQLException;
import java.util.List;

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
            private final HBox pane = new HBox(5, deleteBtn);

            {
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                
                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
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

    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
