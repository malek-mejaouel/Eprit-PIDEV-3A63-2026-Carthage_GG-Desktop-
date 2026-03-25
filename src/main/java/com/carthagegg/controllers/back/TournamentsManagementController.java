package com.carthagegg.controllers.back;

import com.carthagegg.dao.GameDAO;
import com.carthagegg.dao.TournamentDAO;
import com.carthagegg.models.Game;
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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TournamentsManagementController {

    @FXML private TableView<Tournament> tournamentsTable;
    @FXML private TableColumn<Tournament, Integer> colId;
    @FXML private TableColumn<Tournament, String> colName;
    @FXML private TableColumn<Tournament, String> colGame;
    @FXML private TableColumn<Tournament, String> colDates;
    @FXML private TableColumn<Tournament, BigDecimal> colPrize;
    @FXML private TableColumn<Tournament, String> colLocation;
    @FXML private TableColumn<Tournament, Void> colActions;

    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private ComboBox<Game> gameComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField prizeField;
    @FXML private TextField locationField;

    private TournamentDAO tournamentDAO = new TournamentDAO();
    private GameDAO gameDAO = new GameDAO();
    private ObservableList<Tournament> tournamentsList = FXCollections.observableArrayList();
    private Tournament selectedTournament;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadTournaments();
        loadGames();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("tournamentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("tournamentName"));
        colPrize.setCellValueFactory(new PropertyValueFactory<>("prizePool"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        
        colGame.setCellValueFactory(cellData -> {
            try {
                Game g = gameDAO.findById(cellData.getValue().getGameId());
                return new SimpleStringProperty(g != null ? g.getName() : "Unknown");
            } catch (SQLException e) {
                return new SimpleStringProperty("Error");
            }
        });

        colDates.setCellValueFactory(cellData -> {
            Tournament t = cellData.getValue();
            return new SimpleStringProperty(t.getStartDate() + " - " + t.getEndDate());
        });

        colActions.setCellFactory(param -> new TableCell<Tournament, Void>() {
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

    private void loadTournaments() {
        try {
            tournamentsList.setAll(tournamentDAO.findAll());
            tournamentsTable.setItems(tournamentsList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGames() {
        try {
            gameComboBox.setItems(FXCollections.observableArrayList(gameDAO.findAll()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        String name = nameField.getText();
        Game game = gameComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        String prizeStr = prizeField.getText();
        String loc = locationField.getText();

        if (name.isEmpty() || game == null || start == null || end == null || prizeStr.isEmpty()) {
            showAlert("Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        try {
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
                tournamentsList.add(t);
            } else {
                tournamentDAO.update(t);
                tournamentsTable.refresh();
            }
            hideForm();
        } catch (Exception e) {
            showAlert("Error", "Error saving tournament: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showForm() { formPane.setVisible(true); formPane.setManaged(true); }
    @FXML private void handleHideForm() { hideForm(); }
    private void hideForm() { formPane.setVisible(false); formPane.setManaged(false); }
    private void clearForm() {
        nameField.clear(); gameComboBox.setValue(null); startDatePicker.setValue(null);
        endDatePicker.setValue(null); prizeField.clear(); locationField.clear();
    }
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
