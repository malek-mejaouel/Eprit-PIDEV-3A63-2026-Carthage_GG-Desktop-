package com.carthagegg.controllers.front;

import com.carthagegg.dao.MatchDAO;
import com.carthagegg.models.Match;
import com.carthagegg.utils.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MatchesController {

    @FXML private VBox matchesContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private SidebarController sidebarController;

    private final MatchDAO matchDAO = new MatchDAO();
    private final ObservableList<Match> allMatches = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("matches");
        }
        loadMatches();
        setupFilters();
    }

    private void loadMatches() {
        try {
            allMatches.setAll(matchDAO.findAll());
            refreshMatches();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupFilters() {
        sortComboBox.setItems(FXCollections.observableArrayList("ID Asc", "ID Desc"));
        sortComboBox.setValue("ID Asc");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshMatches());
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshMatches());
    }

    private void refreshMatches() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        Comparator<Match> comparator = Comparator.comparingInt(Match::getMatchId);
        if ("ID Desc".equals(sortComboBox.getValue())) {
            comparator = comparator.reversed();
        }

        List<Match> matches = allMatches.stream()
                .filter(match -> keyword.isEmpty()
                        || String.valueOf(match.getMatchId()).contains(keyword)
                        || safe(match.getTeamAName()).contains(keyword)
                        || safe(match.getTeamBName()).contains(keyword)
                        || safe(match.getTournamentName()).contains(keyword))
                .sorted(comparator)
                .collect(Collectors.toList());

        matchesContainer.getChildren().clear();
        for (Match match : matches) {
            matchesContainer.getChildren().add(createMatchRow(match));
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

        Label rivalryBadge = new Label("");
        if (match.isRivalry()) {
            rivalryBadge.setText("🔥");
            rivalryBadge.setStyle("-fx-font-size: 26px;");
            rivalryBadge.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.ORANGERED));
            com.carthagegg.utils.RivalryDetector detector = new com.carthagegg.utils.RivalryDetector();
            String summary = detector.getRivalrySummary(
                match.getTeamAId(), 
                match.getTeamBId(), 
                match.getTeamAName() != null ? match.getTeamAName() : "Team A", 
                match.getTeamBName() != null ? match.getTeamBName() : "Team B"
            );
            Tooltip tooltip = new Tooltip(summary);
            tooltip.setShowDelay(javafx.util.Duration.millis(100));
            Tooltip.install(rivalryBadge, tooltip);
        }

        VBox info = new VBox(5);
        Label tournamentLabel = new Label(match.getTournamentName() != null ? match.getTournamentName() : "Exhibition");
        tournamentLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
        Label statusLabel = new Label(match.getStatus() != null ? match.getStatus() : "SCHEDULED");
        statusLabel.setStyle("-fx-text-fill: #949499;");
        info.getChildren().addAll(tournamentLabel, statusLabel);

        row.getChildren().addAll(team1, score, team2, rivalryBadge, info);
        return row;
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
