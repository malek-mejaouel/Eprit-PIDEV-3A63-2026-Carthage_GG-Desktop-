package com.carthagegg.controllers.front;

import com.carthagegg.dao.EventDAO;
import com.carthagegg.dao.ReservationDAO;
import com.carthagegg.models.Event;
import com.carthagegg.models.Reservation;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReservationFormController {

    @FXML private TextField fullNameField;
    @FXML private TextField priceField;
    @FXML private TextField seatsField;
    @FXML private TextField reservationIdField;
    @FXML private TextField createdAtField;
    @FXML private TextField eventIdField;
    @FXML private Label eventTitleLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label availableSeatsLabel;
    @FXML private Button reserveButton;
    @FXML private Button cancelButton;

    private Event event;
    private ReservationDAO reservationDAO = new ReservationDAO();
    private EventDAO eventDAO = new EventDAO();

    @FXML
    public void initialize() {
        // Initialize seats field
        seatsField.setText("1");

        // Add listener to seats field to update total price
        seatsField.textProperty().addListener((obs, oldValue, newValue) -> {
            updateTotalPrice();
        });
        
        // Set up button handlers
        if (reserveButton != null) {
            reserveButton.setOnAction(e -> handleReserve());
        }
        if (cancelButton != null) {
            cancelButton.setOnAction(e -> handleCancel());
        }
    }

    public void setEvent(Event event) {
        this.event = event;

        // Set event details
        eventTitleLabel.setText(event.getTitle());
        if (event.getStartAt() != null) {
            eventDateLabel.setText(event.getStartAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm")));
        }
        availableSeatsLabel.setText("Available seats: " + event.getMaxSeats());

        // Auto-fill event ID
        eventIdField.setText(String.valueOf(event.getId()));

        // Set price per seat (assuming a default price, you can modify this logic)
        BigDecimal pricePerSeat = new BigDecimal("25.00"); // Default price, you can make this dynamic
        priceField.setText(pricePerSeat.toString());

        updateTotalPrice();
    }

    private void updateTotalPrice() {
        try {
            BigDecimal pricePerSeat = new BigDecimal(priceField.getText());
            int seats = Integer.parseInt(seatsField.getText());
            BigDecimal total = pricePerSeat.multiply(new BigDecimal(seats));
            reservationIdField.setText("Will be auto-generated");
        } catch (NumberFormatException e) {
            reservationIdField.setText("Will be auto-generated");
        }
    }

    private void handleReserve() {
        // Validation
        String name = fullNameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Erreur de validation", "Veuillez entrer votre nom complet.", Alert.AlertType.ERROR);
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur de validation", "Veuillez entrer un prix valide supérieur à 0.", Alert.AlertType.ERROR);
            return;
        }

        int seats;
        try {
            seats = Integer.parseInt(seatsField.getText().trim());
            if (seats <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur de validation", "Veuillez entrer un nombre de places valide supérieur à 0.", Alert.AlertType.ERROR);
            return;
        }

        if (SessionManager.getCurrentUser() == null) {
            showAlert("Erreur", "Vous devez être connecté pour faire une réservation.", Alert.AlertType.ERROR);
            return;
        }

        try {
            Reservation reservation = new Reservation();
            reservation.setName(name);
            reservation.setPrice(price);
            reservation.setSeats(seats);
            reservation.setEventId(event.getId());
            reservation.setUserId(SessionManager.getCurrentUser().getUserId());
            // Statut toujours WAITING - l'admin le changera plus tard
            reservation.setStatus(Reservation.Status.WAITING);

            reservationDAO.save(reservation);

            showAlert("Succès", "Réservation créée avec succès!\nVotre réservation est en attente de confirmation par l'admin.", Alert.AlertType.INFORMATION);

            // Update the form with generated values
            reservationIdField.setText(String.valueOf(reservation.getId()));
            createdAtField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm")));

            // Navigate back to events
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Events.fxml");
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de créer la réservation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de créer la réservation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Events.fxml");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
