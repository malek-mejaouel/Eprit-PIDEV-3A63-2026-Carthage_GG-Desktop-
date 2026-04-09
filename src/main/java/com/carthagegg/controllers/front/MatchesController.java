package com.carthagegg.controllers.front;

import com.carthagegg.dao.MatchDAO;
import com.carthagegg.models.Match;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;

public class MatchesController {

    @FXML private VBox matchesContainer;
    @FXML private SidebarController sidebarController;

    private MatchDAO matchDAO = new MatchDAO();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("matches");
        }
        loadMatches();
    }

    private void loadMatches() {
        try {
            List<Match> matches = matchDAO.findAll();
            matchesContainer.getChildren().clear();
            
            for (Match match : matches) {
                matchesContainer.getChildren().add(createMatchRow(match));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createMatchRow(Match match) {
        HBox row = new HBox(30);
        row.getStyleClass().add("card");
        row.setAlignment(javafx.geometry.Pos.CENTER);
        row.setPadding(new javafx.geometry.Insets(20));

        Label team1 = new Label(match.getTeamAName() != null ? match.getTeamAName() : "TBD");
        team1.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");
        team1.setPrefWidth(200);
        team1.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Label score = new Label(match.getScoreTeamA() + " - " + match.getScoreTeamB());
        score.getStyleClass().add("neon-label");
        score.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        Label team2 = new Label(match.getTeamBName() != null ? match.getTeamBName() : "TBD");
        team2.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");
        team2.setPrefWidth(200);
        team2.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox info = new VBox(5);
        info.getChildren().addAll(
            new Label(match.getTournamentName() != null ? match.getTournamentName() : "Exhibition"),
            new Label(match.getStatus() != null ? match.getStatus() : "SCHEDULED")
        );
        info.setStyle("-fx-text-fill: #71717a;");

        row.getChildren().addAll(team1, score, team2, info);
        return row;
    }

    @FXML private void handleNavHome() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml"); }
    @FXML private void handleNavTournaments() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Tournaments.fxml"); }
    @FXML private void handleNavTeams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Teams.fxml"); }
    @FXML private void handleNavMatches() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Matches.fxml"); }
    @FXML private void handleNavEvents() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Events.fxml"); }
    @FXML private void handleNavNews() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/News.fxml"); }
    @FXML private void handleNavShop() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Shop.fxml"); }
    @FXML private void handleNavStreams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Streams.fxml"); }
}
