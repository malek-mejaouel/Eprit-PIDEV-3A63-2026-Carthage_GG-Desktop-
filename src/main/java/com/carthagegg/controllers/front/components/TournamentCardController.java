package com.carthagegg.controllers.front.components;

import com.carthagegg.controllers.front.TournamentDetailsController;
import com.carthagegg.dao.GameDAO;
import com.carthagegg.models.Game;
import com.carthagegg.models.Tournament;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class TournamentCardController {

    @FXML private Label gameLabel;
    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label locationLabel;
    @FXML private Label prizeLabel;

    private Tournament tournament;
    private GameDAO gameDAO = new GameDAO();

    public void setData(Tournament t) {
        this.tournament = t;
        titleLabel.setText(t.getTournamentName());
        dateLabel.setText(t.getStartDate().format(DateTimeFormatter.ofPattern("dd MMM")) + " - " + 
                         t.getEndDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        locationLabel.setText(t.getLocation());
        prizeLabel.setText(t.getPrizePool().toString() + " USD");

        try {
            Game g = gameDAO.findById(t.getGameId());
            if (g != null) gameLabel.setText(g.getName().toUpperCase());
        } catch (SQLException e) {
            gameLabel.setText("UNKNOWN");
        }
    }

    @FXML
    private void handleViewDetails() {
        TournamentDetailsController controller = SceneNavigator.navigateTo("/com/carthagegg/fxml/front/TournamentDetails.fxml", tournament);
        if (controller != null) {
            controller.setTournament(tournament);
        }
    }
}
