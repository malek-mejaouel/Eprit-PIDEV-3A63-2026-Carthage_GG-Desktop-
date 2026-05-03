package com.carthagegg.controllers.front;

import com.carthagegg.models.Team;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.format.DateTimeFormatter;

public class TeamDetailsController {

    @FXML private SidebarController sidebarController;
    
    @FXML private Label teamNameLabel;
    @FXML private Label creationDateLabel;
    @FXML private ImageView teamLogo;

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("teams");
        }
    }

    public void setTeam(Team t) {
        teamNameLabel.setText(t.getTeamName());
        if (t.getCreationDate() != null) {
            creationDateLabel.setText("Created on: " + t.getCreationDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }

        if (t.getLogo() != null && !t.getLogo().isEmpty()) {
            try {
                teamLogo.setImage(new Image(t.getLogo()));
            } catch (Exception e) {
                // Fallback to default or leave as is
            }
        }
    }

    @FXML
    private void handleBack() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Teams.fxml");
    }
}
