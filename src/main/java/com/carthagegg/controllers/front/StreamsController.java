package com.carthagegg.controllers.front;

import com.carthagegg.dao.StreamDAO;
import com.carthagegg.models.Stream;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.SQLException;
import java.util.List;

public class StreamsController {

    @FXML private FlowPane streamsGrid;
    @FXML private SidebarController sidebarController;

    private StreamDAO streamDAO = new StreamDAO();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("streams");
        }
        loadStreams();
    }

    private void loadStreams() {
        try {
            List<Stream> streams = streamDAO.findAll();
            streamsGrid.getChildren().clear();
            
            for (Stream stream : streams) {
                streamsGrid.getChildren().add(createStreamCard(stream));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createStreamCard(Stream stream) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15));
        card.setPrefWidth(280);
        card.setAlignment(Pos.CENTER);

        ImageView thumbnail = new ImageView();
        thumbnail.setFitHeight(150);
        thumbnail.setFitWidth(250);
        thumbnail.setPreserveRatio(false);
        if (stream.getThumbnail() != null && !stream.getThumbnail().isEmpty()) {
            try { thumbnail.setImage(new Image(stream.getThumbnail())); } catch (Exception e) {}
        }

        Label title = new Label(stream.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        HBox liveInfo = new HBox(10);
        liveInfo.setAlignment(Pos.CENTER);
        Label status = new Label(stream.isLive() ? "● LIVE" : "OFFLINE");
        status.setStyle(stream.isLive() ? "-fx-text-fill: #ef4444; -fx-font-weight: bold;" : "-fx-text-fill: #71717a;");
        Label viewers = new Label(stream.getViewerCount() + " viewers");
        viewers.setStyle("-fx-text-fill: #71717a;");
        liveInfo.getChildren().addAll(status, viewers);

        Button watchBtn = new Button("WATCH NOW");
        watchBtn.getStyleClass().add("btn-primary");
        watchBtn.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(thumbnail, title, liveInfo, watchBtn);
        return card;
    }

    @FXML private void handleNavHome() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml"); }
    @FXML private void handleNavTournaments() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Tournaments.fxml"); }
    @FXML private void handleNavTeams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Teams.fxml"); }
    @FXML private void handleNavMatches() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Matches.fxml"); }
    @FXML private void handleNavEvents() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Events.fxml"); }
    @FXML private void handleNavNews() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/News.fxml"); }
    @FXML private void handleNavShop() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Shop.fxml"); }
    @FXML private void handleNavStreams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Streams.fxml"); }
}
