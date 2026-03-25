package com.carthagegg.controllers.back;

import com.carthagegg.dao.StatsDAO;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;

public class AdminDashboardController {

    @FXML private ImageView miniLogo;
    @FXML private Label totalUsers;
    @FXML private Label totalTournaments;
    @FXML private Label totalRevenue;
    private StatsDAO statsDAO = new StatsDAO();

    @FXML
    public void initialize() {
        // Load logo
        try {
            miniLogo.setImage(new Image(getClass().getResourceAsStream("/images/zz.png")));
        } catch (Exception e) {
            System.err.println("Could not load mini logo: " + e.getMessage());
        }

        // Guard check for admin
        if (!SessionManager.isAdmin()) {
            System.err.println("Access denied: Not an admin");
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        loadDashboardStats();
    }

    private void loadDashboardStats() {
        try {
            totalUsers.setText(String.valueOf(statsDAO.countUsers()));
            totalTournaments.setText(String.valueOf(statsDAO.countTournaments()));
            totalRevenue.setText(String.format("%.2f TND", statsDAO.getTotalRevenue()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleNavUsers() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/UsersManagement.fxml"); }
    @FXML private void handleNavGames() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/GamesManagement.fxml"); }
    @FXML private void handleNavTournaments() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/TournamentsManagement.fxml"); }
    @FXML private void handleNavTeams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/TeamsManagement.fxml"); }
    @FXML private void handleNavMatches() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/MatchesManagement.fxml"); }
    @FXML private void handleNavEvents() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/EventsManagement.fxml"); }
    @FXML private void handleNavNews() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/NewsManagement.fxml"); }
    @FXML private void handleNavCategories() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/CategoriesManagement.fxml"); }
    @FXML private void handleNavProducts() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/ProductsManagement.fxml"); }
    @FXML private void handleNavStreams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/StreamsManagement.fxml"); }
    
    @FXML private void handleNavFront() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml"); }
    
    @FXML 
    private void handleSignOut() {
        SessionManager.logout();
        SceneNavigator.navigateTo("/com/carthagegg/fxml/auth/SignIn.fxml");
    }
}
