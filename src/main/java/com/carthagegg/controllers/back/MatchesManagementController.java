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
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private ComboBox<Game> gameComboBox;
    @FXML private ComboBox<Tournament> tournamentComboBox;
    @FXML private ComboBox<Team> teamAComboBox;
    @FXML private ComboBox<Team> teamBComboBox;
    @FXML private TextField scoreAField;
    @FXML private TextField scoreBField;
    @FXML private DatePicker datePicker;

    private MatchDAO matchDAO = new MatchDAO();
    private TeamDAO teamDAO = new TeamDAO();
    private GameDAO gameDAO = new GameDAO();
    private TournamentDAO tournamentDAO = new TournamentDAO();
    private ObservableList<Match> matchesList = FXCollections.observableArrayList();
    private Match selectedMatch;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadMatches();
        loadComboBoxes();
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
            try {
                Game g = gameDAO.findById(cellData.getValue().getGameId());
                return new SimpleStringProperty(g != null ? g.getName() : "Unknown");
            } catch (SQLException e) { return new SimpleStringProperty("Error"); }
        });

        colTournament.setCellValueFactory(cellData -> {
            try {
                List<Tournament> list = tournamentDAO.findAll();
                Tournament t = list.stream().filter(tr -> tr.getTournamentId() == cellData.getValue().getTournamentId())
                        .findFirst().orElse(null);
                return new SimpleStringProperty(t != null ? t.getTournamentName() : "No Tournament");
            } catch (SQLException e) { return new SimpleStringProperty("Error"); }
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

    private SimpleStringProperty getTeamName(int teamId) {
        try {
            List<Team> teams = teamDAO.findAll();
            Team t = teams.stream().filter(team -> team.getTeamId() == teamId).findFirst().orElse(null);
            return new SimpleStringProperty(t != null ? t.getTeamName() : "Unknown");
        } catch (SQLException e) { return new SimpleStringProperty("Error"); }
    }

    private void loadMatches() {
        try {
            matchesList.setAll(matchDAO.findAll());
            matchesTable.setItems(matchesList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadComboBoxes() {
        try {
            gameComboBox.setItems(FXCollections.observableArrayList(gameDAO.findAll()));
            tournamentComboBox.setItems(FXCollections.observableArrayList(tournamentDAO.findAll()));
            ObservableList<Team> teams = FXCollections.observableArrayList(teamDAO.findAll());
            teamAComboBox.setItems(teams);
            teamBComboBox.setItems(teams);
        } catch (SQLException e) { e.printStackTrace(); }
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
                matchesList.add(m);
            } else {
                matchDAO.update(m);
                matchesTable.refresh();
            }
            hideForm();
        } catch (Exception e) { showAlert("Error", "Check all fields: " + e.getMessage(), Alert.AlertType.ERROR); }
    }

    private void showForm() { formPane.setVisible(true); formPane.setManaged(true); }
    @FXML private void handleHideForm() { hideForm(); }
    private void hideForm() { formPane.setVisible(false); formPane.setManaged(false); }
    private void clearForm() {
        scoreAField.setText("0"); scoreBField.setText("0"); datePicker.setValue(null);
        gameComboBox.setValue(null); tournamentComboBox.setValue(null);
        teamAComboBox.setValue(null); teamBComboBox.setValue(null);
    }
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
