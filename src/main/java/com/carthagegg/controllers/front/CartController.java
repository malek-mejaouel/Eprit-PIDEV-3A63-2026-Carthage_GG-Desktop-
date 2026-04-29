package com.carthagegg.controllers.front;

import com.carthagegg.dao.CouponDAO;
import com.carthagegg.dao.OrderDAO;
import com.carthagegg.dao.ProductDAO;
import com.carthagegg.models.Coupon;
import com.carthagegg.models.Order;
import com.carthagegg.models.Product;
import com.carthagegg.utils.CartManager;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import com.carthagegg.utils.StripeService;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class CartController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label itemCountLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label totalLabel;
    @FXML private SidebarController sidebarController;
    
    @FXML private TextField couponField;
    @FXML private Label couponMessageLabel;
    @FXML private HBox discountRow;
    @FXML private Label discountLabel;

    private OrderDAO orderDAO = new OrderDAO();
    private ProductDAO productDAO = new ProductDAO();
    private CouponDAO couponDAO = new CouponDAO();
    private Coupon activeCoupon = null;

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
        info.setMinWidth(250); // Ensure enough space for name and prices
        Label name = new Label(product.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
        
        HBox priceContainer = new HBox(12);
        priceContainer.setAlignment(Pos.CENTER_LEFT);
        
        if (product.getDiscountPrice() != null && product.getDiscountPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            Text oldPrice = new Text(product.getPrice() + " USD");
            oldPrice.setFill(Color.web("#949499"));
            oldPrice.setStyle("-fx-font-size: 13;");
            oldPrice.setStrikethrough(true);
            oldPrice.setOpacity(0.6);
            
            Label price = new Label(product.getDiscountPrice() + " USD");
            price.setStyle("-fx-text-fill: #ffb800; -fx-font-weight: bold; -fx-font-size: 18;");
            price.setMinWidth(Region.USE_PREF_SIZE); // Prevent truncation
            
            priceContainer.getChildren().addAll(oldPrice, price);
        } else {
            Label price = new Label(product.getPrice() + " USD");
            price.setStyle("-fx-text-fill: #ffb800; -fx-font-weight: bold; -fx-font-size: 18;");
            price.setMinWidth(Region.USE_PREF_SIZE); // Prevent truncation
            priceContainer.getChildren().add(price);
        }
        
        info.getChildren().addAll(name, priceContainer);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Quantity Controls
        HBox qtyControls = new HBox(15);
        qtyControls.setAlignment(Pos.CENTER);
        qtyControls.setStyle("-fx-background-color: #0d0d15; -fx-background-radius: 20; -fx-padding: 5 15;");
        
        Button minusBtn = new Button();
        FontIcon minusIcon = new FontIcon("fas-minus");
        minusIcon.setIconSize(12);
        minusIcon.setIconColor(javafx.scene.paint.Color.web("#ffb800"));
        minusBtn.setGraphic(minusIcon);
        minusBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        minusBtn.setOnAction(e -> {
            CartManager.updateQuantity(product, quantity - 1);
            refreshCart();
        });

        Label qtyLabel = new Label(String.valueOf(quantity));
        qtyLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");

        Button plusBtn = new Button();
        FontIcon plusIcon = new FontIcon("fas-plus");
        plusIcon.setIconSize(12);
        plusIcon.setIconColor(javafx.scene.paint.Color.web("#ffb800"));
        plusBtn.setGraphic(plusIcon);
        plusBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
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
        BigDecimal subtotal = CartManager.getTotalPrice();
        BigDecimal total = subtotal;

        if (activeCoupon != null) {
            BigDecimal discount = subtotal.multiply(new BigDecimal(activeCoupon.getDiscountPercentage()))
                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            total = subtotal.subtract(discount);
            
            discountRow.setVisible(true);
            discountRow.setManaged(true);
            discountLabel.setText("-" + discount + " USD");
        } else {
            discountRow.setVisible(false);
            discountRow.setManaged(false);
        }
        
        itemCountLabel.setText(count + (count == 1 ? " item" : " items") + " in your cart");
        subtotalLabel.setText(subtotal + " USD");
        totalLabel.setText(total + " USD");
    }

    @FXML private void handleApplyCoupon() {
        String code = couponField.getText().trim();
        if (code.isEmpty()) return;

        Optional<Coupon> couponOpt = couponDAO.findByCode(code);
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            if (coupon.isValid()) {
                activeCoupon = coupon;
                couponMessageLabel.setText("Coupon applied: " + coupon.getDiscountPercentage() + "% OFF!");
                couponMessageLabel.setTextFill(Color.web("#22c55e"));
                couponMessageLabel.setVisible(true);
                updateSummary();
            } else {
                activeCoupon = null;
                couponMessageLabel.setText("This coupon has expired.");
                couponMessageLabel.setTextFill(Color.web("#ef4444"));
                couponMessageLabel.setVisible(true);
                updateSummary();
            }
        } else {
            activeCoupon = null;
            couponMessageLabel.setText("Invalid coupon code.");
            couponMessageLabel.setTextFill(Color.web("#ef4444"));
            couponMessageLabel.setVisible(true);
            updateSummary();
        }
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

            // --- Stripe Integration ---
            try {
                double discountPct = activeCoupon != null ? activeCoupon.getDiscountPercentage() : 0.0;
                String checkoutUrl = StripeService.createCheckoutSession(items, discountPct);
                StripeService.openCheckoutInBrowser(checkoutUrl);
                
                // For a desktop app, we usually show a dialog asking if payment was successful
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Payment Confirmation");
                confirmationAlert.setHeaderText("A checkout page has been opened in your browser.");
                confirmationAlert.setContentText("Did you complete the payment successfully?");
                
                ButtonType yesButton = new ButtonType("Yes, Complete Order", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType("No, Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirmationAlert.getButtonTypes().setAll(yesButton, noButton);

                java.util.Optional<ButtonType> result = confirmationAlert.showAndWait();
                if (result.isEmpty() || result.get() != yesButton) {
                    return; // User cancelled or payment failed
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Payment Error", "Could not initialize Stripe checkout: " + e.getMessage(), Alert.AlertType.ERROR);
                return;
            }
            // --------------------------

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
