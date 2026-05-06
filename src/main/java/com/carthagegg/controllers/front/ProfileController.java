package com.carthagegg.controllers.front;

import com.carthagegg.models.User;
import com.carthagegg.services.VerificationService;
import com.carthagegg.utils.ImageGenerator;
import com.carthagegg.dao.EventDAO;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;

public class ProfileController {

    @FXML private ImageView avatarView;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label rolesLabel;
    @FXML private HBox badgeContainer;
    @FXML private Label badgeLabel;
    @FXML private VBox verificationBox;
    @FXML private SidebarController sidebarController;

    private EventDAO eventDAO;

    private final VerificationService verificationService = new VerificationService();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("profile");
        }
        updateUI();
    }

    private void updateUI() {

        // Initialize DAO
        eventDAO = new EventDAO();

        // Load user profile
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            usernameLabel.setText(user.getUsername());
            emailLabel.setText(user.getEmail());
            fullNameLabel.setText(user.getFirstName() + " " + user.getLastName());
            rolesLabel.setText(formatRoles(user.getRoles()));
            
            // Badge logic
            if (user.isVerified()) {
                badgeContainer.setVisible(true);
                badgeContainer.setManaged(true);
                badgeLabel.setText(user.getVerifiedRoleBadge());
                verificationBox.setVisible(false);
                verificationBox.setManaged(false);
            } else {
                badgeContainer.setVisible(false);
                badgeContainer.setManaged(false);
                verificationBox.setVisible(true);
                verificationBox.setManaged(true);
            }

            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                try {
                    String avatarPath = user.getAvatar();
                    if (avatarPath.startsWith("http") || avatarPath.startsWith("file:")) {
                        avatarView.setImage(new Image(avatarPath));
                    } else {
                        avatarView.setImage(new Image(java.nio.file.Path.of(avatarPath).toUri().toString()));
                    }
                } catch (Exception e) {
                    loadDefaultAvatar();
                }
            } else {
                loadDefaultAvatar();
            }
        }
    }

    private void loadDefaultAvatar() {
        try {
            avatarView.setImage(new Image(getClass().getResourceAsStream("/images/zz.png")));
        } catch (Exception e) {
            System.err.println("Could not load default avatar: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String formatRoles(String rolesJson) {
        if (rolesJson == null || rolesJson.isEmpty()) return "USER";
        return rolesJson.replace("[", "")
                       .replace("]", "")
                       .replace("\"", "")
                       .replace("ROLE_", "")
                       .replace(",", ", ");
    }

    @FXML private void handleNavHome() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml"); }
    @FXML private void handleNavTournaments() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Tournaments.fxml"); }
    @FXML private void handleNavTeams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Teams.fxml"); }
    @FXML private void handleNavMatches() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Matches.fxml"); }
    @FXML private void handleNavEvents() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Events.fxml"); }
    @FXML private void handleNavNews() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/News.fxml"); }
    @FXML private void handleNavShop() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Shop.fxml"); }
    @FXML private void handleNavStreams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Streams.fxml"); }

    @FXML private void handleNavEditProfile() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/EditProfile.fxml"); }

    @FXML
    private void handleVerifyAccount() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Certification Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(avatarView.getScene().getWindow());
        if (file != null) {
            try {
                User user = SessionManager.getCurrentUser();
                String role = formatRoles(user.getRoles());

                boolean success = verificationService.verifyCertification(user, file, role);
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Verification Success");
                    alert.setHeaderText("Congratulations!");
                    alert.setContentText("Your " + role + " badge has been verified and added to your profile.");
                    alert.showAndWait();
                    updateUI();
                } else {
                    showError("Verification failed. Please ensure the image contains the correct details.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showError("System error during verification.");
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }
}





