package com.carthagegg.controllers.back;

import com.carthagegg.dao.EventDAO;
import com.carthagegg.dao.LocationDAO;
import com.carthagegg.models.Event;
import com.carthagegg.models.Location;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventsManagementController {

    // Events Table
    @FXML private TableView<Event> eventsTable;
    @FXML private TableColumn<Event, Integer> colEventId;
    @FXML private TableColumn<Event, String> colEventTitle;
    @FXML private TableColumn<Event, String> colEventLocation;
    @FXML private TableColumn<Event, String> colEventStart;
    @FXML private TableColumn<Event, Integer> colEventSeats;
    @FXML private TableColumn<Event, Void> colEventActions;

    // Locations Table
    @FXML private TableView<Location> locationsTable;
    @FXML private TableColumn<Location, Integer> colLocId;
    @FXML private TableColumn<Location, String> colLocName;
    @FXML private TableColumn<Location, String> colLocAddress;
    @FXML private TableColumn<Location, Integer> colLocCapacity;
    @FXML private TableColumn<Location, Void> colLocActions;

    // Panes & Forms
    @FXML private TabPane mainTabPane;
    @FXML private VBox eventFormPane;
    @FXML private VBox locationFormPane;
    @FXML private Label eventFormTitle;
    @FXML private Label locationFormTitle;

    // Event Fields
    @FXML private TextField eventTitleField;
    @FXML private ComboBox<Location> eventLocationComboBox;
    @FXML private DatePicker eventStartDatePicker;
    @FXML private DatePicker eventEndDatePicker;
    @FXML private TextField eventSeatsField;
    @FXML private TextArea eventDescriptionArea;

    // Location Fields
    @FXML private TextField locNameField;
    @FXML private TextField locAddressField;
    @FXML private TextField locCapacityField;
    @FXML private TextField locLatField;
    @FXML private TextField locLngField;

    private EventDAO eventDAO = new EventDAO();
    private LocationDAO locationDAO = new LocationDAO();
    private ObservableList<Event> eventsList = FXCollections.observableArrayList();
    private ObservableList<Location> locationsList = FXCollections.observableArrayList();
    
    private Event selectedEvent;
    private Location selectedLocation;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupEventsTable();
        setupLocationsTable();
        loadEvents();
        loadLocations();
    }

    private void setupEventsTable() {
        colEventId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEventTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colEventSeats.setCellValueFactory(new PropertyValueFactory<>("maxSeats"));
        
        colEventStart.setCellValueFactory(cellData -> {
            if (cellData.getValue().getStartAt() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(cellData.getValue().getStartAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        });

        colEventLocation.setCellValueFactory(cellData -> {
            try {
                List<Location> locs = locationDAO.findAll();
                Location l = locs.stream().filter(loc -> loc.getId() == cellData.getValue().getLocationId())
                        .findFirst().orElse(null);
                return new SimpleStringProperty(l != null ? l.getName() : "Unknown");
            } catch (SQLException e) {
                return new SimpleStringProperty("Error");
            }
        });

        colEventActions.setCellFactory(param -> new TableCell<Event, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-gold");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                editBtn.setOnAction(e -> handleEditEvent(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteEvent(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupLocationsTable() {
        colLocId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLocName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLocAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colLocCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        colLocActions.setCellFactory(param -> new TableCell<Location, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-gold");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                editBtn.setOnAction(e -> handleEditLocation(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteLocation(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadEvents() {
        try {
            eventsList.setAll(eventDAO.findAll());
            eventsTable.setItems(eventsList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadLocations() {
        try {
            List<Location> locs = locationDAO.findAll();
            locationsList.setAll(locs);
            locationsTable.setItems(locationsList);
            eventLocationComboBox.setItems(locationsList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Event Actions
    @FXML private void handleShowAddEventForm() {
        selectedEvent = null;
        eventFormTitle.setText("ADD EVENT");
        clearEventForm();
        showEventForm();
    }

    private void handleEditEvent(Event e) {
        selectedEvent = e;
        eventFormTitle.setText("EDIT EVENT");
        eventTitleField.setText(e.getTitle());
        eventSeatsField.setText(String.valueOf(e.getMaxSeats()));
        eventDescriptionArea.setText(e.getDescription());
        
        if (e.getStartAt() != null) {
            eventStartDatePicker.setValue(e.getStartAt().toLocalDate());
        } else {
            eventStartDatePicker.setValue(null);
        }
        
        if (e.getEndAt() != null) {
            eventEndDatePicker.setValue(e.getEndAt().toLocalDate());
        } else {
            eventEndDatePicker.setValue(null);
        }
        
        for (Location l : eventLocationComboBox.getItems()) {
            if (l.getId() == e.getLocationId()) {
                eventLocationComboBox.setValue(l);
                break;
            }
        }
        showEventForm();
    }

    private void handleDeleteEvent(Event e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete event: " + e.getTitle() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    eventDAO.delete(e.getId());
                    eventsList.remove(e);
                } catch (SQLException ex) { showAlert("Error", "Could not delete event", Alert.AlertType.ERROR); }
            }
        });
    }

    @FXML private void handleSaveEvent() {
        try {
            Event e = (selectedEvent == null) ? new Event() : selectedEvent;
            e.setTitle(eventTitleField.getText());
            e.setLocationId(eventLocationComboBox.getValue() != null ? eventLocationComboBox.getValue().getId() : 0);
            
            if (eventStartDatePicker.getValue() != null) {
                e.setStartAt(eventStartDatePicker.getValue().atStartOfDay());
            } else {
                e.setStartAt(null);
            }
            
            if (eventEndDatePicker.getValue() != null) {
                e.setEndAt(eventEndDatePicker.getValue().atStartOfDay());
            } else {
                e.setEndAt(null);
            }
            
            e.setMaxSeats(Integer.parseInt(eventSeatsField.getText()));
            e.setDescription(eventDescriptionArea.getText());

            if (selectedEvent == null) {
                eventDAO.save(e);
                eventsList.add(e);
            } else {
                eventDAO.update(e);
                eventsTable.refresh();
            }
            hideEventForm();
        } catch (Exception ex) { showAlert("Error", "Check all fields", Alert.AlertType.ERROR); }
    }

    // Location Actions
    @FXML private void handleShowAddLocationForm() {
        selectedLocation = null;
        locationFormTitle.setText("ADD LOCATION");
        clearLocationForm();
        showLocationForm();
    }

    private void handleEditLocation(Location l) {
        selectedLocation = l;
        locationFormTitle.setText("EDIT LOCATION");
        locNameField.setText(l.getName());
        locAddressField.setText(l.getAddress());
        locCapacityField.setText(String.valueOf(l.getCapacity()));
        locLatField.setText(String.valueOf(l.getLatitude()));
        locLngField.setText(String.valueOf(l.getLongitude()));
        showLocationForm();
    }

    private void handleDeleteLocation(Location l) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete location: " + l.getName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    locationDAO.delete(l.getId());
                    locationsList.remove(l);
                    loadLocations(); // Refresh events combo box
                } catch (SQLException ex) { showAlert("Error", "Could not delete location", Alert.AlertType.ERROR); }
            }
        });
    }

    @FXML private void handleSaveLocation() {
        try {
            Location l = (selectedLocation == null) ? new Location() : selectedLocation;
            l.setName(locNameField.getText());
            l.setAddress(locAddressField.getText());
            l.setCapacity(Integer.parseInt(locCapacityField.getText()));
            l.setLatitude(Double.parseDouble(locLatField.getText()));
            l.setLongitude(Double.parseDouble(locLngField.getText()));

            if (selectedLocation == null) {
                locationDAO.save(l);
                locationsList.add(l);
            } else {
                locationDAO.update(l);
                locationsTable.refresh();
            }
            loadLocations(); // Refresh events combo box
            hideLocationForm();
        } catch (Exception ex) { showAlert("Error", "Check all fields", Alert.AlertType.ERROR); }
    }

    // Form Helpers
    @FXML private void showEventForm() { eventFormPane.setVisible(true); eventFormPane.setManaged(true); hideLocationForm(); }
    @FXML private void hideEventForm() { eventFormPane.setVisible(false); eventFormPane.setManaged(false); }
    @FXML private void showLocationForm() { locationFormPane.setVisible(true); locationFormPane.setManaged(true); hideEventForm(); }
    @FXML private void hideLocationForm() { locationFormPane.setVisible(false); locationFormPane.setManaged(false); }
    
    private void clearEventForm() { eventTitleField.clear(); eventLocationComboBox.setValue(null); eventStartDatePicker.setValue(null); eventEndDatePicker.setValue(null); eventSeatsField.setText("50"); eventDescriptionArea.clear(); }
    private void clearLocationForm() { locNameField.clear(); locAddressField.clear(); locCapacityField.setText("100"); locLatField.setText("0.0"); locLngField.setText("0.0"); }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
