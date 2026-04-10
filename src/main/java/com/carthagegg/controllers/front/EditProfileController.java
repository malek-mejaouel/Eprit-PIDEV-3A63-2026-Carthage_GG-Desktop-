package com.carthagegg.controllers.front;

import com.carthagegg.dao.UserDAO;
import com.carthagegg.models.User;
import com.carthagegg.utils.FileStorage;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class EditProfileController {

    @FXML private ImageView avatarPreview;
    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private SidebarController sidebarController;

    private UserDAO userDAO = new UserDAO();
    private File selectedImageFile;

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("profile");
        }
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            usernameField.setText(user.getUsername());
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            emailField.setText(user.getEmail());
            
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                try {
                    String avatarPath = user.getAvatar();
                    if (avatarPath.startsWith("http") || avatarPath.startsWith("file:")) {
                        avatarPreview.setImage(new Image(avatarPath));
                    } else {
                        avatarPreview.setImage(new Image(java.nio.file.Path.of(avatarPath).toUri().toString()));
                    }
                } catch (Exception e) {
                    System.err.println("Could not load avatar: " + e.getMessage());
                    avatarPreview.setImage(new Image(getClass().getResourceAsStream("/images/zz.png")));
                }
            } else {
                avatarPreview.setImage(new Image(getClass().getResourceAsStream("/images/zz.png")));
            }
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Profile Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );
        File file = chooser.showOpenDialog(usernameField.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            avatarPreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSave() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (username.isEmpty()) {
            showAlert("Error", "Username cannot be empty.", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (selectedImageFile != null) {
                try {
                    String avatarPath = FileStorage.saveAvatar(selectedImageFile);
                    user.setAvatar(avatarPath);
                } catch (IOException e) {
                    showAlert("Error", "Could not save profile image: " + e.getMessage(), Alert.AlertType.ERROR);
                    return;
                }
            }

            user.setUsername(username);
            user.setFirstName(firstName);
            user.setLastName(lastName);

            userDAO.update(user);
            showAlert("Success", "Profile updated successfully!", Alert.AlertType.INFORMATION);
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Profile.fxml");

        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Profile.fxml");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
