package com.carthagegg.controllers.back;

import com.carthagegg.dao.GameDAO;
import com.carthagegg.dao.TournamentDAO;
import com.carthagegg.models.Game;
import com.carthagegg.models.Tournament;
import com.carthagegg.utils.AIService;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TournamentsManagementController {

    @FXML private TableView<Tournament> tournamentsTable;
    @FXML private TableColumn<Tournament, Integer> colId;
    @FXML private TableColumn<Tournament, String> colName;
    @FXML private TableColumn<Tournament, String> colGame;
    @FXML private TableColumn<Tournament, String> colDates;
    @FXML private TableColumn<Tournament, BigDecimal> colPrize;
    @FXML private TableColumn<Tournament, String> colLocation;
    @FXML private TableColumn<Tournament, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private Label nameErrorLabel;
    @FXML private ComboBox<Game> gameComboBox;
    @FXML private Label gameErrorLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private Label startDateErrorLabel;
    @FXML private DatePicker endDatePicker;
    @FXML private Label endDateErrorLabel;
    @FXML private TextField prizeField;
    @FXML private Label prizeErrorLabel;
    @FXML private TextField locationField;
    @FXML private Label locationErrorLabel;

    private final TournamentDAO tournamentDAO = new TournamentDAO();
    private final GameDAO gameDAO = new GameDAO();
    private final AIService aiService = new AIService();
    private final ObservableList<Tournament> tournamentsList = FXCollections.observableArrayList();
    private final FilteredList<Tournament> filteredTournaments = new FilteredList<>(tournamentsList, tournament -> true);
    private final SortedList<Tournament> sortedTournaments = new SortedList<>(filteredTournaments);
    private final Map<Integer, Game> gamesById = new HashMap<>();
    private Tournament selectedTournament;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadGames();
        setupFilters();
        loadTournaments();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("tournamentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("tournamentName"));
        colPrize.setCellValueFactory(new PropertyValueFactory<>("prizePool"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        
        colGame.setCellValueFactory(cellData -> {
            Game game = gamesById.get(cellData.getValue().getGameId());
            return new SimpleStringProperty(game != null ? game.getName() : "Unknown");
        });

        colDates.setCellValueFactory(cellData -> {
            Tournament t = cellData.getValue();
            return new SimpleStringProperty(t.getStartDate() + " - " + t.getEndDate());
        });

        colActions.setCellFactory(param -> new TableCell<Tournament, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button aiBtn = new Button("AI Recap");
            private final Button bracketBtn = new Button("AI Bracket");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, editBtn, deleteBtn, aiBtn, bracketBtn);

            {
                editBtn.getStyleClass().add("btn-gold");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                aiBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");
                bracketBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");
                editBtn.setOnAction(e -> {
                    Tournament t = getTableView().getItems().get(getIndex());
                    if (t != null) handleEdit(t);
                });
                deleteBtn.setOnAction(e -> {
                    Tournament t = getTableView().getItems().get(getIndex());
                    if (t != null) handleDelete(t);
                });
                aiBtn.setOnAction(e -> {
                    Tournament t = getTableView().getItems().get(getIndex());
                    if (t != null) handleAIRecap(t);
                });
                bracketBtn.setOnAction(e -> {
                    Tournament t = getTableView().getItems().get(getIndex());
                    if (t != null) handleAIBracketSuggestion(t);
                });
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

        applyFilters();
        applySort();
        tournamentsTable.setItems(sortedTournaments);
    }

    private void loadTournaments() {
        try {
            tournamentsList.setAll(tournamentDAO.findAll());
        } catch (SQLException e) {
            showAlert("Error", "Unable to load tournaments: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadGames() {
        try {
            List<Game> games = gameDAO.findAll();
            gamesById.clear();
            for (Game game : games) {
                gamesById.put(game.getGameId(), game);
            }
            gameComboBox.setItems(FXCollections.observableArrayList(games));
        } catch (SQLException e) {
            showAlert("Error", "Unable to load games: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filteredTournaments.setPredicate(tournament -> {
            if (keyword.isEmpty()) {
                return true;
            }
            Game game = gamesById.get(tournament.getGameId());
            String dates = tournament.getStartDate() + " - " + tournament.getEndDate();
            return String.valueOf(tournament.getTournamentId()).contains(keyword)
                    || safe(tournament.getTournamentName()).contains(keyword)
                    || safe(game == null ? "" : game.getName()).contains(keyword)
                    || safe(dates).contains(keyword)
                    || safe(tournament.getLocation()).contains(keyword)
                    || safe(tournament.getPrizePool() == null ? "" : tournament.getPrizePool().toPlainString()).contains(keyword);
        });
    }

    private void applySort() {
        Comparator<Tournament> comparator = Comparator.comparingInt(Tournament::getTournamentId);
        if ("ID Desc".equals(sortComboBox.getValue())) {
            comparator = comparator.reversed();
        }
        sortedTournaments.setComparator(comparator);
    }

    @FXML private void handleShowAddForm() {
        selectedTournament = null;
        formTitle.setText("ADD TOURNAMENT");
        clearForm();
        showForm();
    }

    private void handleEdit(Tournament t) {
        selectedTournament = t;
        formTitle.setText("EDIT TOURNAMENT");
        nameField.setText(t.getTournamentName());
        startDatePicker.setValue(t.getStartDate());
        endDatePicker.setValue(t.getEndDate());
        prizeField.setText(t.getPrizePool().toString());
        locationField.setText(t.getLocation());
        
        // Find and select the game in ComboBox
        for (Game g : gameComboBox.getItems()) {
            if (g.getGameId() == t.getGameId()) {
                gameComboBox.setValue(g);
                break;
            }
        }
        showForm();
    }

    private void handleDelete(Tournament t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete tournament: " + t.getTournamentName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    tournamentDAO.delete(t.getTournamentId());
                    tournamentsList.remove(t);
                } catch (SQLException e) {
                    showAlert("Error", "Could not delete tournament: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleSaveTournament() {
        if (!validateForm()) {
            return;
        }

        try {
            String name = nameField.getText().trim();
            Game game = gameComboBox.getValue();
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            String prizeStr = prizeField.getText().trim();
            String loc = locationField.getText().trim();

            Tournament t = (selectedTournament == null) ? new Tournament() : selectedTournament;
            t.setTournamentName(name);
            t.setGameId(game.getGameId());
            t.setStartDate(start);
            t.setEndDate(end);
            t.setPrizePool(new BigDecimal(prizeStr));
            t.setLocation(loc);
            t.setUserId(SessionManager.getCurrentUser().getUserId());

            if (selectedTournament == null) {
                tournamentDAO.save(t);
            } else {
                tournamentDAO.update(t);
            }
            loadTournaments();
            hideForm();
        } catch (Exception e) {
            showAlert("Error", "Error saving tournament: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateForm() {
        clearErrors();
        boolean valid = true;

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText("Tournament name is required.");
            valid = false;
        }
        if (gameComboBox.getValue() == null) {
            gameErrorLabel.setText("Game selection is required.");
            valid = false;
        }
        if (startDatePicker.getValue() == null) {
            startDateErrorLabel.setText("Start date is required.");
            valid = false;
        }
        if (endDatePicker.getValue() == null) {
            endDateErrorLabel.setText("End date is required.");
            valid = false;
        }
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null
                && endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            endDateErrorLabel.setText("End date must be after start date.");
            valid = false;
        }
        if (prizeField.getText() == null || prizeField.getText().trim().isEmpty()) {
            prizeErrorLabel.setText("Prize pool is required.");
            valid = false;
        } else {
            try {
                BigDecimal prize = new BigDecimal(prizeField.getText().trim());
                if (prize.compareTo(BigDecimal.ZERO) < 0) {
                    prizeErrorLabel.setText("Prize pool must be positive.");
                    valid = false;
                }
            } catch (NumberFormatException exception) {
                prizeErrorLabel.setText("Prize pool must be numeric.");
                valid = false;
            }
        }
        if (locationField.getText() == null || locationField.getText().trim().isEmpty()) {
            locationErrorLabel.setText("Location is required.");
            valid = false;
        }

        return valid;
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
        nameField.clear(); gameComboBox.setValue(null); startDatePicker.setValue(null);
        endDatePicker.setValue(null); prizeField.clear(); locationField.clear();
    }

    private void clearErrors() {
        nameErrorLabel.setText("");
        gameErrorLabel.setText("");
        startDateErrorLabel.setText("");
        endDateErrorLabel.setText("");
        prizeErrorLabel.setText("");
        locationErrorLabel.setText("");
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private void handleAIRecap(Tournament t) {
        Game game = gamesById.get(t.getGameId());
        String gameName = game != null ? game.getName() : "this game";

        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("AI Recap");
        loadingAlert.setHeaderText("Generating AI Recap for " + t.getTournamentName() + "...");
        loadingAlert.setContentText("Please wait while we consult the AI...");
        loadingAlert.show();

        String prompt = String.format("Generate a comprehensive recap report for the tournament '%s' for the game %s. " +
                "Start Date: %s, End Date: %s, Prize Pool: %s, Location: %s. " +
                "Suggest an optimal bracket and scheduling for future editions.", 
                t.getTournamentName(), gameName, t.getStartDate(), t.getEndDate(), t.getPrizePool(), t.getLocation());

        aiService.getAIResponseAsync(prompt)
            .thenAccept(response -> Platform.runLater(() -> {
                loadingAlert.close();
                showAIDialog("AI Tournament Recap: " + t.getTournamentName(), response);
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    loadingAlert.close();
                    showAlert("AI Error", "Failed to get AI response: " + ex.getMessage(), Alert.AlertType.ERROR);
                });
                return null;
            });
    }

    private void showAIDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(400);
        textArea.setPrefWidth(600);
        
        alert.getDialogPane().setContent(textArea);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleAIBracketSuggestion(Tournament t) {
        Game game = gamesById.get(t.getGameId());
        String gameName = game != null ? game.getName() : "this game";

        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("AI Bracket Suggestion");
        loadingAlert.setHeaderText("Generating AI Bracket for " + t.getTournamentName() + "...");
        loadingAlert.setContentText("Please wait while we consult the AI...");
        loadingAlert.show();

        String prompt = String.format("Generate a detailed tournament bracket and scheduling suggestion for the tournament '%s' for the game %s. " +
                        "Start Date: %s, End Date: %s, Prize Pool: %s, Location: %s. " +
                        "Provide a clear structure for matches and rounds.",
                t.getTournamentName(), gameName, t.getStartDate(), t.getEndDate(), t.getPrizePool(), t.getLocation());

        aiService.getAIResponseAsync(prompt)
                .thenAccept(response -> Platform.runLater(() -> {
                    loadingAlert.close();
                    showAIDialog("AI Bracket Suggestion: " + t.getTournamentName(), response);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        loadingAlert.close();
                        showAlert("AI Error", "Failed to get AI response: " + ex.getMessage(), Alert.AlertType.ERROR);
                    });
                    return null;
                });
    }
    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
