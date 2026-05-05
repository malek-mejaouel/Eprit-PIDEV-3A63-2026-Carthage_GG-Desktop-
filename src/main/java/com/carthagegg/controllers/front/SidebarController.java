package com.carthagegg.controllers.front;

import com.carthagegg.models.User;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.kordamp.ikonli.javafx.FontIcon;

public class SidebarController {

    @FXML private ImageView miniLogo;
    @FXML private Button adminBtn;
    @FXML private Button navHome;
    @FXML private Button navTournaments;
    @FXML private Button navTeams;
    @FXML private Button navMatches;
    @FXML private Button navLeaderboard;
    @FXML private Button navEvents;
    @FXML private Button navNews;
    @FXML private Button navShop;
    @FXML private Button navStreams;
    @FXML private Button navProfile;

    @FXML
    public void initialize() {
        // Load logo
        try {
            miniLogo.setImage(new Image(getClass().getResourceAsStream("/images/zz.png")));
        } catch (Exception e) {
            System.err.println("Could not load mini logo: " + e.getMessage());
        }

        // Show admin button if applicable
        if (SessionManager.isAdmin()) {
            adminBtn.setVisible(true);
            adminBtn.setManaged(true);
        }
    }

    public void setActiveItem(String itemId) {
        // Reset all
        resetStyle(adminBtn);
        resetStyle(navHome);
        resetStyle(navTournaments);
        resetStyle(navTeams);
        resetStyle(navMatches);
        resetStyle(navLeaderboard);
        resetStyle(navEvents);
        resetStyle(navNews);
        resetStyle(navShop);
        resetStyle(navStreams);
        resetStyle(navProfile);

        // Set active
        switch (itemId) {
            case "admin": setActive(adminBtn); break;
            case "home": setActive(navHome); break;
            case "tournaments": setActive(navTournaments); break;
            case "teams": setActive(navTeams); break;
            case "matches": setActive(navMatches); break;
            case "leaderboard": setActive(navLeaderboard); break;
            case "events": setActive(navEvents); break;
            case "news": setActive(navNews); break;
            case "shop": setActive(navShop); break;
            case "streams": setActive(navStreams); break;
            case "profile": setActive(navProfile); break;
        }
    }

    private void resetStyle(Button btn) {
        if (btn == null) return;
        btn.getStyleClass().remove("active");
        if (btn.getGraphic() instanceof FontIcon) {
            // Special color for admin icon reset
            if (btn == adminBtn) {
                ((FontIcon) btn.getGraphic()).setIconColor(javafx.scene.paint.Color.web("#FFC107"));
            } else {
                ((FontIcon) btn.getGraphic()).setIconColor(javafx.scene.paint.Color.web("#949499"));
            }
        }
    }

    private void setActive(Button btn) {
        if (btn == null) return;
        btn.getStyleClass().add("active");
        if (btn.getGraphic() instanceof FontIcon) {
            ((FontIcon) btn.getGraphic()).setIconColor(javafx.scene.paint.Color.web("#FFC107"));
        }
    }

    @FXML private void handleNavAdmin() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
    @FXML private void handleNavHome() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml"); }
    @FXML private void handleNavTournaments() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Tournaments.fxml"); }
    @FXML private void handleNavTeams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Teams.fxml"); }
    @FXML private void handleNavMatches() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Matches.fxml"); }
    @FXML private void handleNavLeaderboard() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Leaderboard.fxml"); }
    @FXML private void handleNavEvents() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Events.fxml"); }
    @FXML private void handleNavNews() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/News.fxml"); }
    @FXML private void handleNavShop() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Shop.fxml"); }
    @FXML private void handleNavStreams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Streams.fxml"); }
    @FXML private void handleNavProfile() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Profile.fxml"); }

    @FXML
    private void handleSignOut() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            try {
                new com.carthagegg.dao.UserDAO().setActiveStatus(user.getUserId(), false);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        SessionManager.logout();
        SceneNavigator.navigateTo("/com/carthagegg/fxml/auth/SignIn.fxml");
    }
}
