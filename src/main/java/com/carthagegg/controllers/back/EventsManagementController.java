package com.carthagegg.controllers.back;

import com.carthagegg.dao.EventDAO;
import com.carthagegg.dao.LocationDAO;
import com.carthagegg.dao.ReservationDAO;
import com.carthagegg.models.Event;
import com.carthagegg.models.Location;
import com.carthagegg.models.Reservation;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
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

    // Reservations Table
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, Integer> colResId;
    @FXML private TableColumn<Reservation, String> colResName;
    @FXML private TableColumn<Reservation, BigDecimal> colResPrice;
    @FXML private TableColumn<Reservation, String> colResDate;
    @FXML private TableColumn<Reservation, String> colResEvent;
    @FXML private TableColumn<Reservation, String> colResStatus;
    @FXML private TableColumn<Reservation, Void> colResActions;

    // Panes & Forms
    @FXML private TabPane mainTabPane;
    @FXML private VBox eventFormPane;
    @FXML private VBox locationFormPane;
    @FXML private VBox reservationFormPane;
    @FXML private Label eventFormTitle;
    @FXML private Label locationFormTitle;
    @FXML private Label reservationFormTitle;

    // Search & Filters
    @FXML private TextField eventSearchField;
    @FXML private ComboBox<Location> eventFilterLocation;
    @FXML private TextField locationSearchField;
    @FXML private TextField reservationSearchField;
    @FXML private ComboBox<Reservation.Status> reservationFilterStatus;

    // Event Fields
    @FXML private TextField eventTitleField;
    @FXML private ComboBox<Location> eventLocationComboBox;
    @FXML private DatePicker eventStartDatePicker;
    @FXML private DatePicker eventEndDatePicker;
    @FXML private TextField eventSeatsField;
    @FXML private TextArea eventDescriptionArea;

    // Event Error Labels
    @FXML private Label eventTitleError;
    @FXML private Label eventLocationError;
    @FXML private Label eventStartDateError;
    @FXML private Label eventEndDateError;
    @FXML private Label eventSeatsError;
    @FXML private Label eventDescriptionError;

    // Location Fields
    @FXML private TextField locNameField;
    @FXML private TextField locAddressField;
    @FXML private TextField locCapacityField;
    @FXML private TextField locLatField;
    @FXML private TextField locLngField;

    // Location Error Labels
    @FXML private Label locNameError;
    @FXML private Label locAddressError;
    @FXML private Label locCapacityError;
    @FXML private Label locLatError;
    @FXML private Label locLngError;

    // Reservation Fields
    @FXML private TextField resNameField;
    @FXML private TextField resPriceField;
    @FXML private ComboBox<Event> resEventComboBox;
    @FXML private ComboBox<Reservation.Status> resStatusComboBox;

    // Reservation Error Labels
    @FXML private Label resNameError;
    @FXML private Label resPriceError;
    @FXML private Label resEventError;
    @FXML private Label resStatusError;

    private EventDAO eventDAO = new EventDAO();
    private LocationDAO locationDAO = new LocationDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();

    private ObservableList<Event> eventsList = FXCollections.observableArrayList();
    private ObservableList<Location> locationsList = FXCollections.observableArrayList();
    private ObservableList<Reservation> reservationsList = FXCollections.observableArrayList();
    
    private FilteredList<Event> filteredEvents;
    private FilteredList<Location> filteredLocations;
    private FilteredList<Reservation> filteredReservations;

    private Event selectedEvent;
    private Location selectedLocation;
    private Reservation selectedReservation;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupEventsTable();
        setupLocationsTable();
        setupReservationsTable();
        
        loadEvents();
        loadLocations();
        loadReservations();

        setupSearchAndFilters();
        
        // Initialize ComboBoxes
        resStatusComboBox.setItems(FXCollections.observableArrayList(Reservation.Status.values()));
        reservationFilterStatus.setItems(FXCollections.observableArrayList(Reservation.Status.values()));
    }

    private void setupSearchAndFilters() {
        // Event Search & Filter
        filteredEvents = new FilteredList<>(eventsList, p -> true);
        eventSearchField.textProperty().addListener((observable, oldValue, newValue) -> updateEventFilter());
        eventFilterLocation.valueProperty().addListener((observable, oldValue, newValue) -> updateEventFilter());
        SortedList<Event> sortedEvents = new SortedList<>(filteredEvents);
        sortedEvents.comparatorProperty().bind(eventsTable.comparatorProperty());
        eventsTable.setItems(sortedEvents);

        // Location Search
        filteredLocations = new FilteredList<>(locationsList, p -> true);
        locationSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredLocations.setPredicate(loc -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return loc.getName().toLowerCase().contains(lowerCaseFilter) ||
                       loc.getAddress().toLowerCase().contains(lowerCaseFilter);
            });
        });
        SortedList<Location> sortedLocations = new SortedList<>(filteredLocations);
        sortedLocations.comparatorProperty().bind(locationsTable.comparatorProperty());
        locationsTable.setItems(sortedLocations);

        // Reservation Search & Filter
        filteredReservations = new FilteredList<>(reservationsList, p -> true);
        reservationSearchField.textProperty().addListener((observable, oldValue, newValue) -> updateReservationFilter());
        reservationFilterStatus.valueProperty().addListener((observable, oldValue, newValue) -> updateReservationFilter());
        SortedList<Reservation> sortedReservations = new SortedList<>(filteredReservations);
        sortedReservations.comparatorProperty().bind(reservationsTable.comparatorProperty());
        reservationsTable.setItems(sortedReservations);
    }

    private void updateEventFilter() {
        filteredEvents.setPredicate(event -> {
            String searchText = eventSearchField.getText();
            Location filterLoc = eventFilterLocation.getValue();

            boolean matchesSearch = true;
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                matchesSearch = event.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                                event.getDescription().toLowerCase().contains(lowerCaseFilter);
            }

            boolean matchesLocation = true;
            if (filterLoc != null) {
                matchesLocation = event.getLocationId() == filterLoc.getId();
            }

            return matchesSearch && matchesLocation;
        });
    }

    private void updateReservationFilter() {
        filteredReservations.setPredicate(res -> {
            String searchText = reservationSearchField.getText();
            Reservation.Status filterStatus = reservationFilterStatus.getValue();

            boolean matchesSearch = true;
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                matchesSearch = res.getName().toLowerCase().contains(lowerCaseFilter);
            }

            boolean matchesStatus = true;
            if (filterStatus != null) {
                matchesStatus = res.getStatus() == filterStatus;
            }

            return matchesSearch && matchesStatus;
        });
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
            Location l = locationsList.stream().filter(loc -> loc.getId() == cellData.getValue().getLocationId())
                    .findFirst().orElse(null);
            return new SimpleStringProperty(l != null ? l.getName() : "Unknown");
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

    private void setupReservationsTable() {
        colResId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colResName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colResPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        colResDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getReservationDate() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(cellData.getValue().getReservationDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        });

        colResEvent.setCellValueFactory(cellData -> {
            Event e = eventsList.stream().filter(ev -> ev.getId() == cellData.getValue().getEventId())
                    .findFirst().orElse(null);
            return new SimpleStringProperty(e != null ? e.getTitle() : "Unknown");
        });

        colResStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));

        colResActions.setCellFactory(param -> new TableCell<Reservation, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-gold");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                editBtn.setOnAction(e -> handleEditReservation(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteReservation(getTableView().getItems().get(getIndex())));
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
            resEventComboBox.setItems(eventsList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadLocations() {
        try {
            List<Location> locs = locationDAO.findAll();
            locationsList.setAll(locs);
            eventLocationComboBox.setItems(locationsList);
            eventFilterLocation.setItems(locationsList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadReservations() {
        try {
            reservationsList.setAll(reservationDAO.findAll());
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Validation Helpers
    private void clearError(Control field, Label errorLabel) {
        field.getStyleClass().remove("text-field-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showError(Control field, Label errorLabel, String message) {
        if (!field.getStyleClass().contains("text-field-error")) {
            field.getStyleClass().add("text-field-error");
        }
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private boolean validateEventForm() {
        boolean isValid = true;
        
        clearError(eventTitleField, eventTitleError);
        clearError(eventLocationComboBox, eventLocationError);
        clearError(eventStartDatePicker, eventStartDateError);
        clearError(eventSeatsField, eventSeatsError);

        if (eventTitleField.getText().isEmpty()) {
            showError(eventTitleField, eventTitleError, "Title is required");
            isValid = false;
        }
        if (eventLocationComboBox.getValue() == null) {
            showError(eventLocationComboBox, eventLocationError, "Location is required");
            isValid = false;
        }
        if (eventStartDatePicker.getValue() == null) {
            showError(eventStartDatePicker, eventStartDateError, "Start date is required");
            isValid = false;
        }
        try {
            int seats = Integer.parseInt(eventSeatsField.getText());
            if (seats <= 0) {
                showError(eventSeatsField, eventSeatsError, "Seats must be > 0");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showError(eventSeatsField, eventSeatsError, "Invalid number");
            isValid = false;
        }

        return isValid;
    }

    private boolean validateLocationForm() {
        boolean isValid = true;
        
        clearError(locNameField, locNameError);
        clearError(locAddressField, locAddressError);
        clearError(locCapacityField, locCapacityError);

        if (locNameField.getText().isEmpty()) {
            showError(locNameField, locNameError, "Name is required");
            isValid = false;
        }
        if (locAddressField.getText().isEmpty()) {
            showError(locAddressField, locAddressError, "Address is required");
            isValid = false;
        }
        try {
            int cap = Integer.parseInt(locCapacityField.getText());
            if (cap <= 0) {
                showError(locCapacityField, locCapacityError, "Capacity must be > 0");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showError(locCapacityField, locCapacityError, "Invalid number");
            isValid = false;
        }

        return isValid;
    }

    private boolean validateReservationForm() {
        boolean isValid = true;
        
        clearError(resNameField, resNameError);
        clearError(resPriceField, resPriceError);
        clearError(resEventComboBox, resEventError);
        clearError(resStatusComboBox, resStatusError);

        if (resNameField.getText().isEmpty()) {
            showError(resNameField, resNameError, "Name is required");
            isValid = false;
        }
        try {
            BigDecimal price = new BigDecimal(resPriceField.getText());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                showError(resPriceField, resPriceError, "Price must be >= 0");
                isValid = false;
            }
        } catch (Exception e) {
            showError(resPriceField, resPriceError, "Invalid price");
            isValid = false;
        }
        if (resEventComboBox.getValue() == null) {
            showError(resEventComboBox, resEventError, "Event is required");
            isValid = false;
        }
        if (resStatusComboBox.getValue() == null) {
            showError(resStatusComboBox, resStatusError, "Status is required");
            isValid = false;
        }

        return isValid;
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
        if (!validateEventForm()) return;

        try {
            Event e = (selectedEvent == null) ? new Event() : selectedEvent;
            e.setTitle(eventTitleField.getText());
            e.setLocationId(eventLocationComboBox.getValue().getId());
            
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
        if (!validateLocationForm()) return;

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

    // Reservation Actions
    @FXML private void handleShowAddReservationForm() {
        selectedReservation = null;
        reservationFormTitle.setText("ADD RESERVATION");
        clearReservationForm();
        showReservationForm();
    }

    private void handleEditReservation(Reservation r) {
        selectedReservation = r;
        reservationFormTitle.setText("EDIT RESERVATION");
        resNameField.setText(r.getName());
        resPriceField.setText(r.getPrice().toString());
        resStatusComboBox.setValue(r.getStatus());
        
        for (Event e : resEventComboBox.getItems()) {
            if (e.getId() == r.getEventId()) {
                resEventComboBox.setValue(e);
                break;
            }
        }
        showReservationForm();
    }

    private void handleDeleteReservation(Reservation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete reservation for: " + r.getName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reservationDAO.delete(r.getId());
                    reservationsList.remove(r);
                } catch (SQLException ex) { showAlert("Error", "Could not delete reservation", Alert.AlertType.ERROR); }
            }
        });
    }

    @FXML private void handleSaveReservation() {
        if (!validateReservationForm()) return;

        try {
            Reservation r = (selectedReservation == null) ? new Reservation() : selectedReservation;
            r.setName(resNameField.getText());
            r.setPrice(new BigDecimal(resPriceField.getText()));
            r.setEventId(resEventComboBox.getValue().getId());
            r.setStatus(resStatusComboBox.getValue());
            r.setReservationDate(java.time.LocalDateTime.now());

            if (selectedReservation == null) {
                reservationDAO.save(r);
                reservationsList.add(r);
            } else {
                reservationDAO.update(r);
                reservationsTable.refresh();
            }
            hideReservationForm();
        } catch (Exception ex) { showAlert("Error", "Check all fields", Alert.AlertType.ERROR); }
    }

    // Form Helpers
    @FXML private void showEventForm() { eventFormPane.setVisible(true); eventFormPane.setManaged(true); hideLocationForm(); hideReservationForm(); }
    @FXML private void hideEventForm() { eventFormPane.setVisible(false); eventFormPane.setManaged(false); }
    @FXML private void showLocationForm() { locationFormPane.setVisible(true); locationFormPane.setManaged(true); hideEventForm(); hideReservationForm(); }
    @FXML private void hideLocationForm() { locationFormPane.setVisible(false); locationFormPane.setManaged(false); }
    @FXML private void showReservationForm() { reservationFormPane.setVisible(true); reservationFormPane.setManaged(true); hideEventForm(); hideLocationForm(); }
    @FXML private void hideReservationForm() { reservationFormPane.setVisible(false); reservationFormPane.setManaged(false); }
    
    private void clearEventForm() { 
        eventTitleField.clear(); eventLocationComboBox.setValue(null); eventStartDatePicker.setValue(null); eventEndDatePicker.setValue(null); eventSeatsField.setText("50"); eventDescriptionArea.clear(); 
        clearError(eventTitleField, eventTitleError); clearError(eventLocationComboBox, eventLocationError); clearError(eventStartDatePicker, eventStartDateError); clearError(eventSeatsField, eventSeatsError);
    }
    private void clearLocationForm() { 
        locNameField.clear(); locAddressField.clear(); locCapacityField.setText("100"); locLatField.setText("0.0"); locLngField.setText("0.0"); 
        clearError(locNameField, locNameError); clearError(locAddressField, locAddressError); clearError(locCapacityField, locCapacityError);
    }
    private void clearReservationForm() {
        resNameField.clear(); resPriceField.clear(); resEventComboBox.setValue(null); resStatusComboBox.setValue(Reservation.Status.WAITING);
        clearError(resNameField, resNameError); clearError(resPriceField, resPriceError); clearError(resEventComboBox, resEventError); clearError(resStatusComboBox, resStatusError);
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
