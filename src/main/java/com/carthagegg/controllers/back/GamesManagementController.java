package com.carthagegg.controllers.back;

import com.carthagegg.dao.GameDAO;
import com.carthagegg.models.Game;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class GamesManagementController {

    @FXML private TableView<Game> gamesTable;
    @FXML private TableColumn<Game, Integer> colId;
    @FXML private TableColumn<Game, String> colName;
    @FXML private TableColumn<Game, String> colGenre;
    @FXML private TableColumn<Game, String> colDescription;
    @FXML private TableColumn<Game, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private Label nameErrorLabel;
    @FXML private TextField genreField;
    @FXML private Label genreErrorLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label descriptionErrorLabel;

    private final GameDAO gameDAO = new GameDAO();
    private final ObservableList<Game> gamesList = FXCollections.observableArrayList();
    private final FilteredList<Game> filteredGames = new FilteredList<>(gamesList, game -> true);
    private final SortedList<Game> sortedGames = new SortedList<>(filteredGames);
    private Game selectedGame;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        setupFilters();
        loadGames();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("gameId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colActions.setCellFactory(param -> new TableCell<Game, Void>() {
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

        applyFilters();
        applySort();
        gamesTable.setItems(sortedGames);
    }

    private void loadGames() {
        try {
            List<Game> games = gameDAO.findAll();
            gamesList.setAll(games);
        } catch (SQLException e) {
            showAlert("Error", "Unable to load games: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filteredGames.setPredicate(game -> {
            if (keyword.isEmpty()) {
                return true;
            }
            return String.valueOf(game.getGameId()).contains(keyword)
                    || safe(game.getName()).contains(keyword)
                    || safe(game.getGenre()).contains(keyword)
                    || safe(game.getDescription()).contains(keyword);
        });
    }

    private void applySort() {
        Comparator<Game> comparator = Comparator.comparingInt(Game::getGameId);
        if ("ID Desc".equals(sortComboBox.getValue())) {
            comparator = comparator.reversed();
        }
        sortedGames.setComparator(comparator);
    }

    @FXML private void handleShowAddForm() {
        selectedGame = null;
        formTitle.setText("ADD GAME");
        clearForm();
        showForm();
    }

    private void handleEdit(Game game) {
        selectedGame = game;
        formTitle.setText("EDIT GAME");
        nameField.setText(game.getName());
        genreField.setText(game.getGenre());
        descriptionArea.setText(game.getDescription());
        showForm();
    }

    private void handleDelete(Game game) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete game: " + game.getName() + "?");
        alert.setContentText("Warning: This may affect associated tournaments and matches.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    gameDAO.delete(game.getGameId());
                    gamesList.remove(game);
                } catch (SQLException e) {
                    showAlert("Error", "Could not delete game: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleSaveGame() {
        if (!validateForm()) {
            return;
        }

        try {
            String name = nameField.getText().trim();
            String genre = genreField.getText().trim();
            String desc = descriptionArea.getText().trim();

            if (selectedGame == null) {
                Game g = new Game();
                g.setName(name);
                g.setGenre(genre);
                g.setDescription(desc);
                gameDAO.save(g);
            } else {
                selectedGame.setName(name);
                selectedGame.setGenre(genre);
                selectedGame.setDescription(desc);
                gameDAO.update(selectedGame);
            }
            loadGames();
            hideForm();
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateForm() {
        clearErrors();
        boolean valid = true;

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText("Game name is required.");
            valid = false;
        }
        if (genreField.getText() == null || genreField.getText().trim().isEmpty()) {
            genreErrorLabel.setText("Genre is required.");
            valid = false;
        }
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            descriptionErrorLabel.setText("Description is required.");
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
        nameField.clear();
        genreField.clear();
        descriptionArea.clear();
    }

    private void clearErrors() {
        nameErrorLabel.setText("");
        genreErrorLabel.setText("");
        descriptionErrorLabel.setText("");
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
