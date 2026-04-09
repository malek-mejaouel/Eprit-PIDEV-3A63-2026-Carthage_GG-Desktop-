package com.carthagegg.controllers.front;

import com.carthagegg.dao.StatsDAO;
import com.carthagegg.models.User;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class HomeController {

    @FXML private ImageView heroLogo;
    @FXML private FlowPane featuredStreams;
    @FXML private Label statUsers;
    @FXML private Label statTournaments;
    @FXML private Label statPrize;
    @FXML private Label statTeams;
    @FXML private SidebarController sidebarController;

    private StatsDAO statsDAO = new StatsDAO();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("home");
        }
        
        // Load hero logo
        try {
            heroLogo.setImage(new Image(getClass().getResourceAsStream("/images/zz.png")));
        } catch (Exception e) {
            System.err.println("Could not load hero logo");
        }

        loadRealStats();
    }

    private void loadRealStats() {
        try {
            statUsers.setText(String.format("%,d+", statsDAO.countUsers()));
            statTournaments.setText(String.valueOf(statsDAO.countTournaments()));
            statPrize.setText(String.format("$%,.0f+", statsDAO.getTournamentPrizePool()));
            statTeams.setText(String.valueOf(statsDAO.countTeams()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleNavTournaments() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Tournaments.fxml"); }
    @FXML private void handleNavTeams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Teams.fxml"); }
    @FXML private void handleNavMatches() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Matches.fxml"); }
    @FXML private void handleNavEvents() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Events.fxml"); }
    @FXML private void handleNavNews() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/News.fxml"); }
    @FXML private void handleNavShop() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Shop.fxml"); }
    @FXML private void handleNavStreams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Streams.fxml"); }
    @FXML private void handleNavProfile() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Profile.fxml"); }
    
    @FXML 
    private void handleSignOut() {
        SessionManager.logout();
        SceneNavigator.navigateTo("/com/carthagegg/fxml/auth/SignIn.fxml");
    }
}
