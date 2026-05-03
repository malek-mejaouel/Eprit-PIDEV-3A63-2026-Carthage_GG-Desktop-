package com.carthagegg.controllers.front;

import com.carthagegg.models.Product;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ReceiptController {

    @FXML private VBox receiptItemsContainer;
    @FXML private Label orderDateLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalPaidLabel;
    @FXML private HBox discountRow;

    private static Map<Product, Integer> lastOrderItems;
    private static BigDecimal lastSubtotal;
    private static BigDecimal lastDiscount;
    private static BigDecimal lastTotal;

    /**
     * static method to pass data to the receipt before navigation
     */
    public static void setOrderData(Map<Product, Integer> items, BigDecimal subtotal, BigDecimal discount, BigDecimal total) {
        lastOrderItems = items;
        lastSubtotal = subtotal;
        lastDiscount = discount;
        lastTotal = total;
    }

    @FXML
    public void initialize() {
        orderDateLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        
        if (lastOrderItems != null) {
            displayReceipt();
        }
    }

    private void displayReceipt() {
        receiptItemsContainer.getChildren().clear();
        
        for (Map.Entry<Product, Integer> entry : lastOrderItems.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            
            VBox nameBox = new VBox(2);
            Label name = new Label(p.getName());
            name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            Label details = new Label(qty + " x " + p.getEffectivePrice() + " USD");
            details.setStyle("-fx-text-fill: #949499; -fx-font-size: 11;");
            nameBox.getChildren().addAll(name, details);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            BigDecimal lineTotal = p.getEffectivePrice().multiply(new BigDecimal(qty));
            Label total = new Label(lineTotal.toString() + " USD");
            total.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            
            row.getChildren().addAll(nameBox, spacer, total);
            receiptItemsContainer.getChildren().add(row);
        }

        subtotalLabel.setText(lastSubtotal.toString() + " USD");
        
        if (lastDiscount != null && lastDiscount.compareTo(BigDecimal.ZERO) > 0) {
            discountRow.setVisible(true);
            discountRow.setManaged(true);
            discountLabel.setText("-" + lastDiscount.toString() + " USD");
        }
        
        totalPaidLabel.setText(lastTotal.toString() + " USD");
    }

    @FXML
    private void handleContinueShopping() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Shop.fxml");
    }
}
