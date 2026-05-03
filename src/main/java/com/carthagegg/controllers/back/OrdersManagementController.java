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

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.IOException;
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

    @FXML private void handleExportPDF() {
        if (stripeTable.getItems().isEmpty()) {
            showAlert("No Data", "There are no Stripe sessions to export.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.setInitialFileName("carthagegg_orders.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File file = fileChooser.showSaveDialog(SceneNavigator.getPrimaryStage());
        if (file != null) {
            try {
                PdfWriter writer = new PdfWriter(file);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf, PageSize.A4);
                document.setMargins(20, 20, 20, 20);

                // Add Logo
                try {
                    var logoResource = getClass().getResource("/images/zz.png");
                    if (logoResource != null) {
                        String logoPath = logoResource.toExternalForm();
                        Image logo = new Image(ImageDataFactory.create(logoPath));
                        logo.setWidth(100);
                        document.add(logo);
                    } else {
                        System.err.println("Logo resource not found: /images/zz.png");
                    }
                } catch (Exception e) {
                    System.err.println("Could not load logo: " + e.getMessage());
                }

                // Add Header
                Paragraph header = new Paragraph("CarthageGG - Orders Report")
                        .setFontSize(24)
                        .setBold()
                        .setFontColor(ColorConstants.BLACK)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20);
                document.add(header);

                // Add Table
                float[] columnWidths = {2, 3, 1, 1, 3};
                Table table = new Table(UnitValue.createPercentArray(columnWidths));
                table.setWidth(UnitValue.createPercentValue(100));

                // Table Header
                String[] headers = {"Session ID", "Customer", "Amount", "Status", "Receipt/URL"};
                for (String h : headers) {
                    table.addHeaderCell(new Cell().add(new Paragraph(h != null ? h : "").setBold())
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                            .setTextAlignment(TextAlignment.CENTER));
                }

                // Table Data
                for (Session session : stripeTable.getItems()) {
                    String customer = "Guest";
                    if (session.getCustomerDetails() != null && session.getCustomerDetails().getEmail() != null) {
                        customer = session.getCustomerDetails().getEmail();
                    } else if (session.getCustomer() != null) {
                        customer = session.getCustomer();
                    }
                    
                    Long amountTotal = session.getAmountTotal();
                    String amount = amountTotal != null ? String.format("%.2f USD", amountTotal / 100.0) : "0.00 USD";
                    String status = session.getPaymentStatus() != null ? session.getPaymentStatus().toUpperCase() : "UNKNOWN";
                    String sessionId = session.getId() != null ? session.getId() : "N/A";
                    String sessionUrl = session.getUrl() != null ? session.getUrl() : "N/A";
                    
                    table.addCell(new Cell().add(new Paragraph(sessionId).setFontSize(8)));
                    table.addCell(new Cell().add(new Paragraph(customer).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(amount).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)));
                    table.addCell(new Cell().add(new Paragraph(status).setFontSize(10).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(sessionUrl).setFontSize(8).setFontColor(ColorConstants.BLUE)));
                }

                document.add(table);

                // Footer
                document.add(new Paragraph("\nGenerated on: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .setFontSize(10)
                        .setItalic()
                        .setTextAlignment(TextAlignment.RIGHT));

                document.close();
                showAlert("Success", "Orders exported successfully to " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
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
