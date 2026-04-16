package com.carthagegg.controllers.front;

import com.carthagegg.dao.OrderDAO;
import com.carthagegg.dao.ProductDAO;
import com.carthagegg.models.Order;
import com.carthagegg.models.Product;
import com.carthagegg.utils.CartManager;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.util.Map;

public class CartController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label itemCountLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label totalLabel;
    @FXML private SidebarController sidebarController;

    private OrderDAO orderDAO = new OrderDAO();
    private ProductDAO productDAO = new ProductDAO();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("shop");
        }
        refreshCart();
    }

    private void refreshCart() {
        cartItemsContainer.getChildren().clear();
        Map<Product, Integer> items = CartManager.getCartItems();
        
        if (items.isEmpty()) {
            showEmptyCart();
        } else {
            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                cartItemsContainer.getChildren().add(createCartItemRow(entry.getKey(), entry.getValue()));
            }
        }
        
        updateSummary();
    }

    private void showEmptyCart() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(100, 0, 0, 0));
        
        FontIcon icon = new FontIcon("fas-shopping-cart");
        icon.setIconSize(64);
        icon.setIconColor(javafx.scene.paint.Color.web("#3f3f46"));
        
        Label emptyLabel = new Label("Your cart is empty");
        emptyLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 20; -fx-font-weight: bold;");
        
        Button shopBtn = new Button("GO TO SHOP");
        shopBtn.getStyleClass().add("btn-gold");
        shopBtn.setOnAction(e -> handleBackToShop());
        
        emptyBox.getChildren().addAll(icon, emptyLabel, shopBtn);
        cartItemsContainer.getChildren().add(emptyBox);
    }

    private HBox createCartItemRow(Product product, int quantity) {
        HBox row = new HBox(20);
        row.getStyleClass().add("card");
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #16161e; -fx-background-radius: 8;");

        // Product Image
        ImageView img = new ImageView();
        img.setFitHeight(80);
        img.setFitWidth(80);
        img.setPreserveRatio(true);
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try { img.setImage(new Image(product.getImage())); } catch (Exception e) {}
        }

        // Product Info
        VBox info = new VBox(5);
        Label name = new Label(product.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
        Label price = new Label(product.getPrice() + " TND");
        price.setStyle("-fx-text-fill: #ffb800; -fx-font-weight: bold;");
        info.getChildren().addAll(name, price);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Quantity Controls
        HBox qtyControls = new HBox(15);
        qtyControls.setAlignment(Pos.CENTER);
        
        Button minusBtn = new Button();
        minusBtn.setGraphic(new FontIcon("fas-minus"));
        minusBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #71717a;");
        minusBtn.setOnAction(e -> {
            CartManager.updateQuantity(product, quantity - 1);
            refreshCart();
        });

        Label qtyLabel = new Label(String.valueOf(quantity));
        qtyLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        Button plusBtn = new Button();
        plusBtn.setGraphic(new FontIcon("fas-plus"));
        plusBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #71717a;");
        plusBtn.setOnAction(e -> {
            CartManager.updateQuantity(product, quantity + 1);
            refreshCart();
        });

        qtyControls.getChildren().addAll(minusBtn, qtyLabel, plusBtn);

        // Remove Button
        Button removeBtn = new Button();
        FontIcon trashIcon = new FontIcon("fas-trash-alt");
        trashIcon.setIconColor(javafx.scene.paint.Color.web("#ef4444"));
        removeBtn.setGraphic(trashIcon);
        removeBtn.setTooltip(new Tooltip("Remove product from cart"));
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-border-color: #ef4444; -fx-border-radius: 6; -fx-cursor: hand; -fx-padding: 8;");
        removeBtn.setOnAction(e -> {
            CartManager.removeProduct(product);
            refreshCart();
        });

        row.getChildren().addAll(img, info, spacer, qtyControls, removeBtn);
        return row;
    }

    private void updateSummary() {
        int count = CartManager.getTotalItems();
        String totalStr = CartManager.getTotalPrice().toString() + " TND";
        
        itemCountLabel.setText(count + (count == 1 ? " item" : " items") + " in your cart");
        subtotalLabel.setText(totalStr);
        totalLabel.setText(totalStr);
    }

    @FXML private void handleBackToShop() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Shop.fxml"); }
    
    @FXML private void handleClearCart() {
        CartManager.clear();
        refreshCart();
    }

    @FXML private void handleCheckout() {
        if (CartManager.getTotalItems() == 0) return;
        
        if (SessionManager.getCurrentUser() == null) {
            showAlert("Login Required", "Please sign in to place an order.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Map<Product, Integer> items = CartManager.getCartItems();
            
            // Check stock again before proceeding
            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                if (entry.getKey().getStock() < entry.getValue()) {
                    showAlert("Out of Stock", "Sorry, " + entry.getKey().getName() + " is no longer available in the requested quantity.", Alert.AlertType.ERROR);
                    return;
                }
            }

            // 1. Update stock in database
            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();
                
                product.setStock(product.getStock() - quantity);
                productDAO.update(product);
            }

            // 2. Save orders to local file (via OrderDAO)
            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();

                Order order = new Order();
                order.setUserId(SessionManager.getCurrentUser().getUserId());
                order.setProductId(product.getId());
                order.setQuantity(quantity);
                order.setStatus(Order.Status.PENDING);
                orderDAO.save(order);
            }

            CartManager.clear();
            refreshCart();
            showAlert("Order Placed", "Your order has been placed successfully!", Alert.AlertType.INFORMATION);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Checkout Error", "An error occurred while updating stock in the database: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Checkout Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
