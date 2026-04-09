package com.carthagegg.controllers.back;

import com.carthagegg.dao.GameDAO;
import com.carthagegg.models.Game;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.util.List;

public class GamesManagementController {

    @FXML private TableView<Game> gamesTable;
    @FXML private TableColumn<Game, Integer> colId;
    @FXML private TableColumn<Game, String> colName;
    @FXML private TableColumn<Game, String> colGenre;
    @FXML private TableColumn<Game, String> colDescription;
    @FXML private TableColumn<Game, Void> colActions;

    @FXML private TextField searchField;
    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private TextField genreField;
    @FXML private TextArea descriptionArea;

    private GameDAO gameDAO = new GameDAO();
    private ObservableList<Game> gamesList = FXCollections.observableArrayList();
    private Game selectedGame;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadGames();
        setupSearch();
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

    private void loadGames() {
        try {
            List<Game> games = gameDAO.findAll();
            gamesList.setAll(games);
            gamesTable.setItems(gamesList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        FilteredList<Game> filteredData = new FilteredList<>(gamesList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(game -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return game.getName().toLowerCase().contains(lowerCaseFilter) ||
                       game.getGenre().toLowerCase().contains(lowerCaseFilter);
            });
        });
        gamesTable.setItems(filteredData);
    }

    @FXML private void handleShowAddForm() {
        selectedGame = null;
        formTitle.setText("ADD GAME");
        nameField.clear();
        genreField.clear();
        descriptionArea.clear();
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
        String name = nameField.getText();
        String genre = genreField.getText();
        String desc = descriptionArea.getText();

        if (name.isEmpty() || genre.isEmpty()) {
            showAlert("Error", "Name and Genre are required!", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (selectedGame == null) {
                Game g = new Game();
                g.setName(name);
                g.setGenre(genre);
                g.setDescription(desc);
                gameDAO.save(g);
                gamesList.add(g);
            } else {
                selectedGame.setName(name);
                selectedGame.setGenre(genre);
                selectedGame.setDescription(desc);
                gameDAO.update(selectedGame);
                gamesTable.refresh();
            }
            hideForm();
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showForm() { formPane.setVisible(true); formPane.setManaged(true); }
    @FXML private void handleHideForm() { hideForm(); }
    private void hideForm() { formPane.setVisible(false); formPane.setManaged(false); }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
