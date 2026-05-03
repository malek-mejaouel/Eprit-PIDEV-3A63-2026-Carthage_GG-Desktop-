package com.carthagegg.controllers.front;

import com.carthagegg.dao.LocationDAO;
import com.carthagegg.models.Event;
import com.carthagegg.models.Location;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class EventDetailsController {

    @FXML private SidebarController sidebarController;
    
    @FXML private Label eventTitleLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventSeatsLabel;
    @FXML private Label eventDescriptionLabel;
    
    @FXML private Label locationNameLabel;
    @FXML private Label locationAddressLabel;
    @FXML private Label locationCapacityLabel;

    private LocationDAO locationDAO = new LocationDAO();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("events");
        }
    }

    public void setEvent(Event event) {
        eventTitleLabel.setText(event.getTitle());
        if (event.getStartAt() != null) {
            eventDateLabel.setText(event.getStartAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm")));
        }
        eventSeatsLabel.setText(event.getMaxSeats() + " seats available");
        eventDescriptionLabel.setText(event.getDescription());

        try {
            Location location = locationDAO.findAll().stream()
                    .filter(l -> l.getId() == event.getLocationId())
                    .findFirst()
                    .orElse(null);

            if (location != null) {
                locationNameLabel.setText(location.getName());
                locationAddressLabel.setText(location.getAddress());
                locationCapacityLabel.setText("Capacity: " + location.getCapacity() + " people");
            } else {
                locationNameLabel.setText("Unknown Location");
                locationAddressLabel.setText("N/A");
                locationCapacityLabel.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Events.fxml");
    }
}
