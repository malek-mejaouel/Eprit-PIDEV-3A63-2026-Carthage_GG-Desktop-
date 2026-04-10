package com.carthagegg.controllers.front;

import com.carthagegg.controllers.front.components.TournamentCardController;
import com.carthagegg.dao.GameDAO;
import com.carthagegg.dao.TournamentDAO;
import com.carthagegg.models.Game;
import com.carthagegg.models.Tournament;
import com.carthagegg.utils.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class TournamentsController {

    @FXML private FlowPane tournamentsGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<Game> gameFilter;
    @FXML private SidebarController sidebarController;

    private TournamentDAO tournamentDAO = new TournamentDAO();
    private GameDAO gameDAO = new GameDAO();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("tournaments");
        }
        loadTournaments();
        loadGames();
    }

    private void loadTournaments() {
        try {
            List<Tournament> tournaments = tournamentDAO.findAll();
            tournamentsGrid.getChildren().clear();
            
            for (Tournament t : tournaments) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/carthagegg/fxml/front/components/TournamentCard.fxml"));
                    Parent card = loader.load();
                    TournamentCardController controller = loader.getController();
                    controller.setData(t);
                    tournamentsGrid.getChildren().add(card);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGames() {
        try {
            gameFilter.setItems(FXCollections.observableArrayList(gameDAO.findAll()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleNavHome() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml"); }
}
