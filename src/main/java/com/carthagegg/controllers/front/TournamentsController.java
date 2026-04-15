package com.carthagegg.controllers.front;

import com.carthagegg.controllers.front.components.TournamentCardController;
import com.carthagegg.dao.GameDAO;
import com.carthagegg.dao.TournamentDAO;
import com.carthagegg.models.Game;
import com.carthagegg.models.Tournament;
import com.carthagegg.utils.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TournamentsController {

    @FXML private FlowPane tournamentsGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<Game> gameFilter;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private SidebarController sidebarController;

    private final TournamentDAO tournamentDAO = new TournamentDAO();
    private final GameDAO gameDAO = new GameDAO();
    private final ObservableList<Tournament> allTournaments = FXCollections.observableArrayList();
    private final Map<Integer, Game> gamesById = new HashMap<>();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("tournaments");
        }
        loadGames();
        setupFilters();
        loadTournaments();
    }

    private void loadTournaments() {
        try {
            allTournaments.setAll(tournamentDAO.findAll());
            refreshTournaments();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGames() {
        try {
            List<Game> games = gameDAO.findAll();
            gamesById.clear();
            for (Game game : games) {
                gamesById.put(game.getGameId(), game);
            }
            gameFilter.setItems(FXCollections.observableArrayList(games));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupFilters() {
        sortComboBox.setItems(FXCollections.observableArrayList("ID Asc", "ID Desc"));
        sortComboBox.setValue("ID Asc");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshTournaments());
        gameFilter.valueProperty().addListener((observable, oldValue, newValue) -> refreshTournaments());
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshTournaments());
    }

    private void refreshTournaments() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        Game selectedGame = gameFilter.getValue();

        Comparator<Tournament> comparator = Comparator.comparingInt(Tournament::getTournamentId);
        if ("ID Desc".equals(sortComboBox.getValue())) {
            comparator = comparator.reversed();
        }

        List<Tournament> tournaments = allTournaments.stream()
                .filter(tournament -> {
                    Game game = gamesById.get(tournament.getGameId());
                    boolean matchesKeyword = keyword.isEmpty()
                            || String.valueOf(tournament.getTournamentId()).contains(keyword)
                            || safe(tournament.getTournamentName()).contains(keyword)
                            || safe(tournament.getLocation()).contains(keyword)
                            || safe(game == null ? "" : game.getName()).contains(keyword);
                    boolean matchesGame = selectedGame == null || tournament.getGameId() == selectedGame.getGameId();
                    return matchesKeyword && matchesGame;
                })
                .sorted(comparator)
                .collect(Collectors.toList());

        tournamentsGrid.getChildren().clear();
        for (Tournament tournament : tournaments) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/carthagegg/fxml/front/components/TournamentCard.fxml"));
                Parent card = loader.load();
                TournamentCardController controller = loader.getController();
                controller.setData(tournament);
                tournamentsGrid.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    @FXML private void handleNavHome() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml"); }
}
