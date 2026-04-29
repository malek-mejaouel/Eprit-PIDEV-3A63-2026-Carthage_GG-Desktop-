package com.carthagegg.controllers.front;

import com.carthagegg.dao.GameDAO;
import com.carthagegg.models.Game;
import com.carthagegg.models.Tournament;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class TournamentDetailsController {

    @FXML private SidebarController sidebarController;
    
    @FXML private Label tournamentNameLabel;
    @FXML private Label dateLabel;
    @FXML private Label prizePoolLabel;
    @FXML private Label locationLabel;
    
    @FXML private Label gameNameLabel;
    @FXML private Label gameGenreLabel;
    @FXML private Label gameDescriptionLabel;

    private GameDAO gameDAO = new GameDAO();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("tournaments");
        }
    }

    public void setTournament(Tournament t) {
        tournamentNameLabel.setText(t.getTournamentName());
        dateLabel.setText(t.getStartDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " - " + 
                         t.getEndDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        prizePoolLabel.setText(t.getPrizePool().toString() + " TND");
        locationLabel.setText(t.getLocation());

        try {
            Game g = gameDAO.findById(t.getGameId());
            if (g != null) {
                gameNameLabel.setText(g.getName());
                gameGenreLabel.setText(g.getGenre());
                gameDescriptionLabel.setText(g.getDescription());
            } else {
                gameNameLabel.setText("Unknown Game");
                gameGenreLabel.setText("N/A");
                gameDescriptionLabel.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Tournaments.fxml");
    }
}
