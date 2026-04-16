package com.carthagegg.controllers.front;

import com.carthagegg.dao.EventDAO;
import com.carthagegg.models.Event;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.SQLException;
import java.util.List;

public class EventsController {

    @FXML private VBox eventsContainer;
    @FXML private SidebarController sidebarController;

    private EventDAO eventDAO = new EventDAO();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("events");
        }
        loadEvents();
    }

    private void loadEvents() {
        try {
            List<Event> events = eventDAO.findAll();
            eventsContainer.getChildren().clear();
            
            for (Event event : events) {
                eventsContainer.getChildren().add(createEventCard(event));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createEventCard(Event event) {
        HBox row = new HBox(20);
        row.getStyleClass().add("card");
        row.setPadding(new Insets(20));
        row.setAlignment(Pos.CENTER_LEFT);

        VBox dateBox = new VBox(5);
        dateBox.setAlignment(Pos.CENTER);
        dateBox.setMinWidth(100);
        dateBox.setStyle("-fx-background-color: #1e1e2e; -fx-background-radius: 8;");
        dateBox.setPadding(new Insets(10));

        Label month = new Label(event.getStartAt().getMonth().name().substring(0, 3));
        month.setStyle("-fx-text-fill: #71717a; -fx-font-weight: bold;");
        Label day = new Label(String.valueOf(event.getStartAt().getDayOfMonth()));
        day.getStyleClass().add("neon-label");
        day.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        dateBox.getChildren().addAll(month, day);

        VBox content = new VBox(5);
        Label title = new Label(event.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");
        Label desc = new Label(event.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #71717a;");

        content.getChildren().addAll(title, desc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button detailsBtn = new Button("DETAILS");
        detailsBtn.getStyleClass().add("btn-primary");
        detailsBtn.setPrefHeight(40);
        detailsBtn.setPrefWidth(120);
        detailsBtn.setOnAction(e -> handleShowDetails(event));

        row.getChildren().addAll(dateBox, content, spacer, detailsBtn);
        return row;
    }

    private void handleShowDetails(Event event) {
        EventDetailsController controller = SceneNavigator.navigateTo("/com/carthagegg/fxml/front/EventDetails.fxml", event);
        if (controller != null) {
            controller.setEvent(event);
        }
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
