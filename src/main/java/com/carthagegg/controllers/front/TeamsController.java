package com.carthagegg.controllers.front;

import com.carthagegg.dao.TeamDAO;
import com.carthagegg.models.Team;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.sql.SQLException;
import java.util.List;

public class TeamsController {

    @FXML private FlowPane teamsGrid;
    @FXML private TextField searchField;
    @FXML private SidebarController sidebarController;

    private TeamDAO teamDAO = new TeamDAO();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("teams");
        }
        loadTeams();
    }

    private void loadTeams() {
        try {
            List<Team> teams = teamDAO.findAll();
            teamsGrid.getChildren().clear();
            
            for (Team team : teams) {
                VBox card = createTeamCard(team);
                teamsGrid.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createTeamCard(Team team) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(250);
        card.setPadding(new javafx.geometry.Insets(15));
        card.setAlignment(javafx.geometry.Pos.CENTER);

        ImageView logo = new ImageView();
        logo.setFitWidth(100);
        logo.setFitHeight(100);
        logo.setPreserveRatio(true);
        if (team.getLogo() != null && !team.getLogo().isEmpty()) {
            try {
                logo.setImage(new Image(team.getLogo()));
            } catch (Exception e) {
                // Fallback to default logo
            }
        }

        Label name = new Label(team.getTeamName());
        name.getStyleClass().add("neon-label");
        name.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        // Removed tag as it's not in the Team model
        // Label tag = new Label("[" + team.getTag() + "]");
        // tag.setStyle("-fx-text-fill: #71717a;");

        Button viewBtn = new Button("VIEW TEAM");
        viewBtn.getStyleClass().add("btn-primary");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setOnAction(e -> handleViewTeam(team));

        card.getChildren().addAll(logo, name, viewBtn);
        return card;
    }

    private void handleViewTeam(Team team) {
        TeamDetailsController controller = SceneNavigator.navigateTo("/com/carthagegg/fxml/front/TeamDetails.fxml", team);
        if (controller != null) {
            controller.setTeam(team);
        }
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
