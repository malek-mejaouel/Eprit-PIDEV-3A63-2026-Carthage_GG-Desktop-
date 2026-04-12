package com.carthagegg.controllers.front;

import com.carthagegg.models.User;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class ProfileController {

    @FXML private ImageView avatarView;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label rolesLabel;
    @FXML private SidebarController sidebarController;

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("profile");
        }
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            usernameLabel.setText(user.getUsername());
            emailLabel.setText(user.getEmail());
            fullNameLabel.setText(user.getFirstName() + " " + user.getLastName());
            rolesLabel.setText(formatRoles(user.getRoles()));
            
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
}
