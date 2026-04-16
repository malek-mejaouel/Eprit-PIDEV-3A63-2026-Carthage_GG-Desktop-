package com.carthagegg.controllers.front;

import com.carthagegg.dao.TeamDAO;
import com.carthagegg.models.Team;
import com.carthagegg.utils.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TeamsController {

    @FXML private FlowPane teamsGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private SidebarController sidebarController;

    private final TeamDAO teamDAO = new TeamDAO();
    private final ObservableList<Team> allTeams = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("teams");
        }
        loadTeams();
        setupFilters();
    }

    private void loadTeams() {
        try {
            allTeams.setAll(teamDAO.findAll());
            refreshTeams();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupFilters() {
        sortComboBox.setItems(FXCollections.observableArrayList("ID Asc", "ID Desc"));
        sortComboBox.setValue("ID Asc");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshTeams());
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshTeams());
    }

    private void refreshTeams() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        Comparator<Team> comparator = Comparator.comparingInt(Team::getTeamId);
        if ("ID Desc".equals(sortComboBox.getValue())) {
            comparator = comparator.reversed();
        }

        List<Team> filteredTeams = allTeams.stream()
                .filter(team -> keyword.isEmpty()
                        || String.valueOf(team.getTeamId()).contains(keyword)
                        || safe(team.getTeamName()).contains(keyword))
                .sorted(comparator)
                .collect(Collectors.toList());

        teamsGrid.getChildren().clear();
        for (Team team : filteredTeams) {
            teamsGrid.getChildren().add(createTeamCard(team));
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
                String imageUrl = team.getLogo().startsWith("http://")
                        || team.getLogo().startsWith("https://")
                        || team.getLogo().startsWith("file:")
                        ? team.getLogo()
                        : java.nio.file.Path.of(team.getLogo()).toUri().toString();
                logo.setImage(new Image(imageUrl));
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

        card.getChildren().addAll(logo, name, viewBtn);
        return card;
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
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
