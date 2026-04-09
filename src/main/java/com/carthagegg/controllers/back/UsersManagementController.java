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

    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Guard check for admin
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadUsers();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roles"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        
        // Actions column with buttons
        colActions.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button deleteBtn = new Button("Delete");
            private final Button editBtn = new Button("Edit");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                editBtn.setStyle("-fx-background-color: #00f0ff; -fx-text-fill: black;");
                
                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
                
                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadUsers() {
        try {
            List<User> users = userDAO.findAll();
            usersList.setAll(users);
            usersTable.setItems(usersList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete user " + user.getUsername() + "?");
        alert.setContentText("This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete logic
                usersList.remove(user);
            }
        });
    }

    private void handleEditUser(User user) {
        // Open edit modal
    }

    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
