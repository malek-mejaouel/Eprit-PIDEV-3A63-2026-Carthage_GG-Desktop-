package com.carthagegg.controllers.back;

import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import com.carthagegg.utils.StripeService;
import com.stripe.model.checkout.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class OrdersManagementController {

    @FXML private TableView<Session> stripeTable;
    @FXML private TableColumn<Session, String> colStripeId;
    @FXML private TableColumn<Session, String> colStripeCustomer;
    @FXML private TableColumn<Session, String> colStripeAmount;
    @FXML private TableColumn<Session, String> colStripeStatus;
    @FXML private TableColumn<Session, String> colStripeUrl;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupStripeTable();
        handleRefreshStripe();
    }

    private void setupStripeTable() {
        colStripeId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        
        colStripeCustomer.setCellValueFactory(cellData -> {
            Session s = cellData.getValue();
            if (s.getCustomerDetails() != null && s.getCustomerDetails().getEmail() != null) {
                return new SimpleStringProperty(s.getCustomerDetails().getEmail());
            }
            return new SimpleStringProperty(s.getCustomer() != null ? s.getCustomer() : "Guest");
        });

        colStripeAmount.setCellValueFactory(cellData -> {
            Long total = cellData.getValue().getAmountTotal();
            if (total == null) return new SimpleStringProperty("0.00 USD");
            return new SimpleStringProperty(String.format("%.2f USD", total / 100.0));
        });

        colStripeStatus.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getPaymentStatus();
            return new SimpleStringProperty(status != null ? status.toUpperCase() : "UNKNOWN");
        });

        colStripeUrl.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUrl()));
    }

    @FXML private void handleRefreshStripe() {
        try {
            List<Session> sessions = StripeService.getRecentSessions();
            stripeTable.setItems(FXCollections.observableArrayList(sessions));
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Stripe Error");
            alert.setHeaderText("Could not fetch sessions from Stripe");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML private void handleExportCSV() {
        if (stripeTable.getItems().isEmpty()) {
            showAlert("No Data", "There are no Stripe sessions to export.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.setInitialFileName("stripe_orders.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showSaveDialog(SceneNavigator.getPrimaryStage());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Header
                writer.println("Session ID,Customer,Amount,Status,Receipt/URL");
                
                // Data
                for (Session session : stripeTable.getItems()) {
                    String customer = (session.getCustomerDetails() != null && session.getCustomerDetails().getEmail() != null) 
                            ? session.getCustomerDetails().getEmail() 
                            : (session.getCustomer() != null ? session.getCustomer() : "Guest");
                    
                    Long amountTotal = session.getAmountTotal();
                    String amount = amountTotal != null ? String.format("%.2f", amountTotal / 100.0) : "0.00";
                    String status = session.getPaymentStatus() != null ? session.getPaymentStatus().toUpperCase() : "UNKNOWN";
                    
                    writer.println(String.format("%s,%s,%s,%s,%s",
                            session.getId(),
                            customer,
                            amount,
                            status,
                            session.getUrl()
                    ));
                }
                
                showAlert("Success", "Orders exported successfully to " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Could not export orders: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/ProductsManagement.fxml"); }
}
