package com.carthagegg.controllers.back;

import com.carthagegg.dao.ReservationDAO;
import com.carthagegg.models.Reservation;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.SQLException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Controller for scanning QR Codes at event entry
 * Allows admins to validate reservations by scanning QR codes
 */
public class QRCodeScannerController {

    @FXML private TextField qrCodeInputField;
    @FXML private Label resultLabel;
    @FXML private VBox resultContainer;
    @FXML private HBox confirmationBadge;
    @FXML private Label reservationIdLabel;
    @FXML private Label reservationNameLabel;
    @FXML private Label reservationEventLabel;
    @FXML private Label reservationSeatsLabel;
    @FXML private Label reservationStatusLabel;
    @FXML private Button validateButton;

    private ReservationDAO reservationDAO = new ReservationDAO();
    private Reservation currentReservation;

    @FXML
    public void initialize() {
        resultContainer.setVisible(false);
        resultContainer.setManaged(false);
        confirmationBadge.setVisible(false);
        confirmationBadge.setManaged(false);
        
        // Listen for Enter key in the input field
        qrCodeInputField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleScanQRCode();
            }
        });
    }

    /**
     * Handles QR code scanning
     */
    @FXML
    private void handleScanQRCode() {
        String qrData = qrCodeInputField.getText().trim();
        
        if (qrData.isEmpty()) {
            showError("Please scan or enter a QR code");
            return;
        }

        processQRCodeData(qrData);
    }

    /**
     * Handles PDF file upload and QR code extraction
     */
    @FXML
    private void handleUploadPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Reservation PDF");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(qrCodeInputField.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                // Extract QR code from PDF
                String qrData = com.carthagegg.utils.QRCodePDFExtractor.extractQRCodeFromPDF(selectedFile);
                
                if (qrData != null && !qrData.isEmpty()) {
                    // Process the extracted QR code
                    qrCodeInputField.setText(qrData);
                    processQRCodeData(qrData);
                } else {
                    showError("No QR code found in the PDF file");
                }
            } catch (Exception e) {
                showError("Error reading PDF: " + e.getMessage());
                System.err.println("PDF Processing Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes QR code data regardless of source (scanner or PDF)
     */
    private void processQRCodeData(String qrData) {
        try {
            // Extract reservation ID from QR code (format: "RESERVATION|ID:X|..." or just the ID)
            int reservationId;
            
            if (qrData.contains("RESERVATION|ID:")) {
                // Detailed format
                String[] parts = qrData.split("\\|");
                String idPart = parts[0];
                reservationId = Integer.parseInt(idPart.split(":")[1]);
            } else {
                // Simple format - just the ID
                reservationId = Integer.parseInt(qrData);
            }

            // Fetch reservation from database
            currentReservation = reservationDAO.findAll().stream()
                    .filter(r -> r.getId() == reservationId)
                    .findFirst()
                    .orElse(null);

            if (currentReservation == null) {
                showError("Reservation not found! ID: " + reservationId);
                clearForm();
                return;
            }

            // Display reservation information
            displayReservation();
            showSuccess("Reservation found! Validating automatically...");
            
            // Auto-validate the reservation after a short delay
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(event -> handleValidateReservationAuto());
            pause.play();
            
        } catch (NumberFormatException e) {
            showError("Invalid QR code format");
            clearForm();
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            clearForm();
        }
    }

    /**
     * Automatically validates reservation (called after PDF scan)
     */
    private void handleValidateReservationAuto() {
        if (currentReservation == null) {
            return;
        }

        try {
            // Update reservation status to CONFIRMED
            currentReservation.setStatus(Reservation.Status.CONFIRMED);
            reservationDAO.update(currentReservation);
            
            // Send confirmation email to user's phone (email)
            sendConfirmationEmail();
            
            // Show confirmation badge
            showConfirmationBadge();
            
            // Auto-clear after 3 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> {
                clearForm();
                qrCodeInputField.clear();
                qrCodeInputField.requestFocus();
            });
            pause.play();
            
        } catch (SQLException e) {
            showError("Failed to update reservation: " + e.getMessage());
        }
    }

    /**
     * Displays the reservation information
     */
    private void displayReservation() {
        reservationIdLabel.setText("ID: " + currentReservation.getId());
        reservationNameLabel.setText("Name: " + currentReservation.getName());
        reservationSeatsLabel.setText("Seats: " + currentReservation.getSeats());
        reservationStatusLabel.setText("Status: " + currentReservation.getStatus().name());
        
        // Try to get event info if available
        reservationEventLabel.setText("Event ID: " + currentReservation.getEventId());
        
        resultContainer.setVisible(true);
        resultContainer.setManaged(true);
    }

    /**
     * Validates/confirms the reservation
     */
    @FXML
    private void handleValidateReservation() {
        if (currentReservation == null) {
            showError("No reservation selected");
            return;
        }

        try {
            // Update reservation status to CONFIRMED
            currentReservation.setStatus(Reservation.Status.CONFIRMED);
            reservationDAO.update(currentReservation);
            
            // Send confirmation email to user's phone (email)
            sendConfirmationEmail();
            
            // Show confirmation badge
            showConfirmationBadge();
            
            // Auto-clear after 3 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> {
                clearForm();
                qrCodeInputField.clear();
                qrCodeInputField.requestFocus();
            });
            pause.play();
            
        } catch (SQLException e) {
            showError("Failed to update reservation: " + e.getMessage());
        }
    }

    /**
     * Cancels/rejects the reservation
     */
    @FXML
    private void handleCancelReservation() {
        if (currentReservation == null) {
            showError("No reservation selected");
            return;
        }

        try {
            currentReservation.setStatus(Reservation.Status.CANCELLED);
            reservationDAO.update(currentReservation);
            
            showError("Reservation cancelled.");
            clearForm();
            qrCodeInputField.clear();
            qrCodeInputField.requestFocus();
            
        } catch (SQLException e) {
            showError("Failed to cancel reservation: " + e.getMessage());
        }
    }

    /**
     * Sends reservation PDF ticket via email to the user
     */
    @FXML
    private void handleSendEmail() {
        if (currentReservation == null) {
            showError("No reservation selected");
            return;
        }

        try {
            // Get user information
            com.carthagegg.dao.UserDAO userDAO = new com.carthagegg.dao.UserDAO();
            com.carthagegg.models.User user = userDAO.findById(currentReservation.getUserId());

            if (user == null) {
                showError("User not found for this reservation");
                return;
            }

            // Get event information
            com.carthagegg.dao.EventDAO eventDAO = new com.carthagegg.dao.EventDAO();
            com.carthagegg.models.Event event = eventDAO.findById(currentReservation.getEventId());

            if (event == null) {
                showError("Event not found for this reservation");
                return;
            }

            // Get location information
            com.carthagegg.dao.LocationDAO locationDAO = new com.carthagegg.dao.LocationDAO();
            com.carthagegg.models.Location location = locationDAO.findById(event.getLocationId());

            // Generate PDF ticket
            java.io.File pdfFile = com.carthagegg.utils.PDFTicketGenerator.generateReservationTicket(
                currentReservation, event, location);

            // Send email with PDF attachment
            boolean emailSent = com.carthagegg.utils.EmailService.sendReservationConfirmation(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                currentReservation.getId(),
                event.getTitle(),
                pdfFile
            );

            if (emailSent) {
                showSuccess("Email sent successfully to " + user.getEmail());
            } else {
                showError("Failed to send email");
            }

        } catch (Exception e) {
            showError("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears the form
     */
    private void clearForm() {
        currentReservation = null;
        resultContainer.setVisible(false);
        resultContainer.setManaged(false);
        confirmationBadge.setVisible(false);
        confirmationBadge.setManaged(false);
        resultLabel.setText("");
    }

    /**
     * Shows success message
     */
    private void showSuccess(String message) {
        resultLabel.setText(message);
        resultLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
    }

    /**
     * Shows error message
     */
    private void showError(String message) {
        resultLabel.setText(message);
        resultLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
    }

    /**
     * Shows confirmation badge (green badge with checkmark)
     */
    private void showConfirmationBadge() {
        confirmationBadge.setVisible(true);
        confirmationBadge.setManaged(true);
        resultContainer.setVisible(false);
        resultContainer.setManaged(false);
        resultLabel.setText("");
    }

    /**
     * Sends confirmation email to user when reservation is validated (acts as phone notification)
     */
    private void sendConfirmationEmail() {
        if (currentReservation == null) {
            return;
        }

        try {
            // Get user information
            com.carthagegg.dao.UserDAO userDAO = new com.carthagegg.dao.UserDAO();
            com.carthagegg.models.User user = userDAO.findById(currentReservation.getUserId());

            if (user == null) {
                System.err.println("User not found for reservation confirmation email");
                return;
            }

            // Get event information
            com.carthagegg.dao.EventDAO eventDAO = new com.carthagegg.dao.EventDAO();
            com.carthagegg.models.Event event = eventDAO.findById(currentReservation.getEventId());

            String eventTitle = (event != null) ? event.getTitle() : "Événement";

            // Send simple confirmation email
            String subject = "✅ Réservation Confirmée - " + eventTitle;
            String body = String.format("""
                Bonjour %s,

                Votre réservation #%d pour l'événement "%s" a été validée avec succès !

                Vous pouvez maintenant accéder à l'événement. Présentez-vous à l'entrée avec votre billet.

                Merci d'utiliser CarthageGG !

                Cordialement,
                L'équipe CarthageGG
                """, user.getFirstName() + " " + user.getLastName(), currentReservation.getId(), eventTitle);

            boolean emailSent = com.carthagegg.utils.EmailService.sendEmail(user.getEmail(), subject, body);

            if (emailSent) {
                System.out.println("Confirmation email sent to " + user.getEmail());
            } else {
                System.err.println("Failed to send confirmation email to " + user.getEmail());
            }

        } catch (Exception e) {
            System.err.println("Error sending confirmation email: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml");
    }
}
