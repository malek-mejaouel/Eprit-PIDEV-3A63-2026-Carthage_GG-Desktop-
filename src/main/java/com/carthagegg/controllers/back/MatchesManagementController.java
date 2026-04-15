package com.carthagegg.controllers.back;

import com.carthagegg.dao.GameDAO;
import com.carthagegg.dao.MatchDAO;
import com.carthagegg.dao.TeamDAO;
import com.carthagegg.dao.TournamentDAO;
import com.carthagegg.models.Game;
import com.carthagegg.models.Match;
import com.carthagegg.models.Team;
import com.carthagegg.models.Tournament;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchesManagementController {

    @FXML private TableView<Match> matchesTable;
    @FXML private TableColumn<Match, Integer> colId;
    @FXML private TableColumn<Match, String> colDate;
    @FXML private TableColumn<Match, String> colTeamA;
    @FXML private TableColumn<Match, String> colScore;
    @FXML private TableColumn<Match, String> colTeamB;
    @FXML private TableColumn<Match, String> colGame;
    @FXML private TableColumn<Match, String> colTournament;
    @FXML private TableColumn<Match, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private ComboBox<Game> gameComboBox;
    @FXML private Label gameErrorLabel;
    @FXML private ComboBox<Tournament> tournamentComboBox;
    @FXML private Label tournamentErrorLabel;
    @FXML private ComboBox<Team> teamAComboBox;
    @FXML private Label teamAErrorLabel;
    @FXML private ComboBox<Team> teamBComboBox;
    @FXML private Label teamBErrorLabel;
    @FXML private TextField scoreAField;
    @FXML private Label scoreAErrorLabel;
    @FXML private TextField scoreBField;
    @FXML private Label scoreBErrorLabel;
    @FXML private DatePicker datePicker;
    @FXML private Label dateErrorLabel;

    private final MatchDAO matchDAO = new MatchDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final GameDAO gameDAO = new GameDAO();
    private final TournamentDAO tournamentDAO = new TournamentDAO();
    private final ObservableList<Match> matchesList = FXCollections.observableArrayList();
    private final FilteredList<Match> filteredMatches = new FilteredList<>(matchesList, match -> true);
    private final SortedList<Match> sortedMatches = new SortedList<>(filteredMatches);
    private final Map<Integer, Team> teamsById = new HashMap<>();
    private final Map<Integer, Game> gamesById = new HashMap<>();
    private final Map<Integer, Tournament> tournamentsById = new HashMap<>();
    private final ObservableList<Tournament> allTournaments = FXCollections.observableArrayList();
    private Match selectedMatch;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadComboBoxes();
        setupFilters();
        loadMatches();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("matchId"));
        
        colDate.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getMatchDate();
            if (date == null) {
                return new SimpleStringProperty("TBD");
            }
            return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        });

        colTeamA.setCellValueFactory(cellData -> getTeamName(cellData.getValue().getTeamAId()));
        colTeamB.setCellValueFactory(cellData -> getTeamName(cellData.getValue().getTeamBId()));
        colScore.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getScoreTeamA() + " - " + cellData.getValue().getScoreTeamB()));
        
        colGame.setCellValueFactory(cellData -> {
            Game game = gamesById.get(cellData.getValue().getGameId());
            return new SimpleStringProperty(game != null ? game.getName() : "Unknown");
        });

        colTournament.setCellValueFactory(cellData -> {
            Tournament tournament = tournamentsById.get(cellData.getValue().getTournamentId());
            return new SimpleStringProperty(tournament != null ? tournament.getTournamentName() : "No Tournament");
        });

        colActions.setCellFactory(param -> new TableCell<Match, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-gold");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupFilters() {
        sortComboBox.setItems(FXCollections.observableArrayList("ID Asc", "ID Desc"));
        sortComboBox.setValue("ID Asc");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applySort());
        gameComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateTournamentOptionsForSelectedGame());

        applyFilters();
        applySort();
        matchesTable.setItems(sortedMatches);
    }

    private SimpleStringProperty getTeamName(int teamId) {
        Team team = teamsById.get(teamId);
        return new SimpleStringProperty(team != null ? team.getTeamName() : "Unknown");
    }

    private void loadMatches() {
        try {
            matchesList.setAll(matchDAO.findAll());
        } catch (SQLException e) {
            showAlert("Error", "Unable to load matches: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadComboBoxes() {
        try {
            List<Game> games = gameDAO.findAll();
            gamesById.clear();
            for (Game game : games) {
                gamesById.put(game.getGameId(), game);
            }
            gameComboBox.setItems(FXCollections.observableArrayList(games));

            List<Tournament> tournaments = tournamentDAO.findAll();
            tournamentsById.clear();
            for (Tournament tournament : tournaments) {
                tournamentsById.put(tournament.getTournamentId(), tournament);
            }
            allTournaments.setAll(tournaments);
            tournamentComboBox.setItems(FXCollections.observableArrayList(allTournaments));

            ObservableList<Team> teams = FXCollections.observableArrayList(teamDAO.findAll());
            teamsById.clear();
            for (Team team : teams) {
                teamsById.put(team.getTeamId(), team);
            }
            teamAComboBox.setItems(teams);
            teamBComboBox.setItems(teams);
        } catch (SQLException e) {
            showAlert("Error", "Unable to load match form data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filteredMatches.setPredicate(match -> {
            if (keyword.isEmpty()) {
                return true;
            }
            Team teamA = teamsById.get(match.getTeamAId());
            Team teamB = teamsById.get(match.getTeamBId());
            Game game = gamesById.get(match.getGameId());
            Tournament tournament = tournamentsById.get(match.getTournamentId());
            String date = match.getMatchDate() == null ? "" : match.getMatchDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
            String score = match.getScoreTeamA() + " - " + match.getScoreTeamB();

            return String.valueOf(match.getMatchId()).contains(keyword)
                    || safe(teamA == null ? match.getTeamAName() : teamA.getTeamName()).contains(keyword)
                    || safe(teamB == null ? match.getTeamBName() : teamB.getTeamName()).contains(keyword)
                    || safe(game == null ? "" : game.getName()).contains(keyword)
                    || safe(tournament == null ? match.getTournamentName() : tournament.getTournamentName()).contains(keyword)
                    || safe(score).contains(keyword)
                    || safe(date).contains(keyword);
        });
    }

    private void applySort() {
        Comparator<Match> comparator = Comparator.comparingInt(Match::getMatchId);
        if ("ID Desc".equals(sortComboBox.getValue())) {
            comparator = comparator.reversed();
        }
        sortedMatches.setComparator(comparator);
    }

    private void updateTournamentOptionsForSelectedGame() {
        Game selectedGame = gameComboBox.getValue();
        if (selectedGame == null) {
            tournamentComboBox.setItems(FXCollections.observableArrayList(allTournaments));
            return;
        }

        ObservableList<Tournament> filtered = allTournaments.filtered(
                tournament -> tournament.getGameId() == selectedGame.getGameId()
        );
        tournamentComboBox.setItems(filtered);

        Tournament selectedTournament = tournamentComboBox.getValue();
        if (selectedTournament != null && selectedTournament.getGameId() != selectedGame.getGameId()) {
            tournamentComboBox.setValue(null);
        }
    }

    @FXML private void handleShowAddForm() {
        selectedMatch = null;
        formTitle.setText("ADD MATCH");
        clearForm();
        showForm();
    }

    private void handleEdit(Match m) {
        selectedMatch = m;
        formTitle.setText("EDIT MATCH");
        scoreAField.setText(String.valueOf(m.getScoreTeamA()));
        scoreBField.setText(String.valueOf(m.getScoreTeamB()));
        if (m.getMatchDate() != null) {
            datePicker.setValue(m.getMatchDate().toLocalDate());
        } else {
            datePicker.setValue(null);
        }
        
        // ComboBox selections
        for (Game g : gameComboBox.getItems()) if (g.getGameId() == m.getGameId()) { gameComboBox.setValue(g); break; }
        for (Tournament t : tournamentComboBox.getItems()) if (t.getTournamentId() == m.getTournamentId()) { tournamentComboBox.setValue(t); break; }
        for (Team t : teamAComboBox.getItems()) if (t.getTeamId() == m.getTeamAId()) { teamAComboBox.setValue(t); break; }
        for (Team t : teamBComboBox.getItems()) if (t.getTeamId() == m.getTeamBId()) { teamBComboBox.setValue(t); break; }
        
        showForm();
    }

    private void handleDelete(Match m) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete match ID " + m.getMatchId() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    matchDAO.delete(m.getMatchId());
                    matchesList.remove(m);
                } catch (SQLException e) { showAlert("Error", "Could not delete match", Alert.AlertType.ERROR); }
            }
        });
    }

    @FXML
    private void handleSaveMatch() {
        try {
            if (!validateForm()) {
                return;
            }

            Match m = (selectedMatch == null) ? new Match() : selectedMatch;
            m.setGameId(gameComboBox.getValue().getGameId());
            m.setTournamentId(tournamentComboBox.getValue().getTournamentId());
            m.setTeamAId(teamAComboBox.getValue().getTeamId());
            m.setTeamBId(teamBComboBox.getValue().getTeamId());
            m.setScoreTeamA(Integer.parseInt(scoreAField.getText()));
            m.setScoreTeamB(Integer.parseInt(scoreBField.getText()));
            if (datePicker.getValue() != null) {
                m.setMatchDate(datePicker.getValue().atStartOfDay()); // Simplified time
            } else {
                m.setMatchDate(null);
            }

            if (selectedMatch == null) {
                matchDAO.save(m);
            } else {
                matchDAO.update(m);
            }
            loadMatches();
            hideForm();
        } catch (Exception e) {
            showAlert("Error", "Check all fields: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateForm() {
        clearErrors();
        boolean valid = true;

        if (gameComboBox.getValue() == null) {
            gameErrorLabel.setText("Game is required.");
            valid = false;
        }
        if (tournamentComboBox.getValue() == null) {
            tournamentErrorLabel.setText("Tournament is required.");
            valid = false;
        }
        if (teamAComboBox.getValue() == null) {
            teamAErrorLabel.setText("Team A is required.");
            valid = false;
        }
        if (teamBComboBox.getValue() == null) {
            teamBErrorLabel.setText("Team B is required.");
            valid = false;
        }
        if (teamAComboBox.getValue() != null && teamBComboBox.getValue() != null
                && teamAComboBox.getValue().getTeamId() == teamBComboBox.getValue().getTeamId()) {
            teamBErrorLabel.setText("Team B must be different from Team A.");
            valid = false;
        }
        if (!isNonNegativeInteger(scoreAField.getText())) {
            scoreAErrorLabel.setText("Score A must be a non-negative number.");
            valid = false;
        }
        if (!isNonNegativeInteger(scoreBField.getText())) {
            scoreBErrorLabel.setText("Score B must be a non-negative number.");
            valid = false;
        }
        if (datePicker.getValue() == null) {
            dateErrorLabel.setText("Match date is required.");
            valid = false;
        }

        return valid;
    }

    private boolean isNonNegativeInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            return Integer.parseInt(value.trim()) >= 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void showForm() {
        clearErrors();
        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    @FXML private void handleHideForm() { hideForm(); }

    private void hideForm() {
        clearForm();
        clearErrors();
        formPane.setVisible(false);
        formPane.setManaged(false);
    }

    private void clearForm() {
        scoreAField.setText("0"); scoreBField.setText("0"); datePicker.setValue(null);
        gameComboBox.setValue(null); tournamentComboBox.setValue(null);
        teamAComboBox.setValue(null); teamBComboBox.setValue(null);
        updateTournamentOptionsForSelectedGame();
    }

    private void clearErrors() {
        gameErrorLabel.setText("");
        tournamentErrorLabel.setText("");
        teamAErrorLabel.setText("");
        teamBErrorLabel.setText("");
        scoreAErrorLabel.setText("");
        scoreBErrorLabel.setText("");
        dateErrorLabel.setText("");
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
