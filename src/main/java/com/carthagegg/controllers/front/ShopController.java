package com.carthagegg.controllers.front;

import com.carthagegg.dao.OrderDAO;
import com.carthagegg.dao.ProductDAO;
import com.carthagegg.models.Product;
import com.carthagegg.utils.CartManager;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ShopController {

    @FXML private FlowPane productsGrid;
    @FXML private SidebarController sidebarController;
    @FXML private Label cartCountLabel;
    @FXML private TextField searchField;

    private ProductDAO productDAO = new ProductDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private com.carthagegg.dao.CategoryDAO categoryDAO = new com.carthagegg.dao.CategoryDAO();
    private int cartCount = 0;
    private List<Product> allProducts = new java.util.ArrayList<>();
    private Map<Integer, Integer> salesCounts = new java.util.HashMap<>();
    private Map<Integer, String> categoryNames = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("shop");
        }
        cartCount = CartManager.getTotalItems();
        cartCountLabel.setText(String.valueOf(cartCount));
        
        loadCategories();
        loadProducts();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts(newValue);
        });
    }

    private void loadCategories() {
        try {
            categoryDAO.findAll().forEach(c -> categoryNames.put(c.getId(), c.getName()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        try {
            allProducts = productDAO.findAll();
            salesCounts = orderDAO.getProductSalesCounts();
            displayProducts(allProducts);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterProducts(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayProducts(allProducts);
            return;
        }

        String filter = searchText.toLowerCase().trim();
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(filter))
                .toList();
        
        displayProducts(filtered);
    }

    private void displayProducts(List<Product> products) {
        productsGrid.getChildren().clear();
        for (Product product : products) {
            productsGrid.getChildren().add(createProductCard(product));
        }
    }

    private VBox createProductCard(Product product) {
        int sales = salesCounts.getOrDefault(product.getId(), 0);
        boolean isTrending = sales >= 5;

        VBox card = new VBox(0);
        card.getStyleClass().add("card");
        card.setPrefWidth(260);
        card.setPadding(Insets.EMPTY);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-radius: 12; -fx-overflow: hidden; -fx-border-color: rgba(255, 184, 0, 0.1); -fx-border-radius: 12;");

        // Image Container with fixed size and background
        StackPane imgContainer = new StackPane();
        imgContainer.setPrefSize(260, 220);
        imgContainer.setStyle("-fx-background-color: #16161e; -fx-background-radius: 12 12 0 0;");
        
        ImageView img = new ImageView();
        img.setFitHeight(180);
        img.setFitWidth(180);
        img.setPreserveRatio(true);
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try { img.setImage(new Image(product.getImage())); } catch (Exception e) {}
        }
        
        // Hover Overlay (Description & Details)
        VBox hoverOverlay = new VBox(10);
        hoverOverlay.setAlignment(Pos.CENTER);
        hoverOverlay.setPadding(new Insets(20));
        hoverOverlay.setStyle("-fx-background-color: rgba(10, 10, 15, 0.9); -fx-background-radius: 12 12 0 0;");
        hoverOverlay.setOpacity(0); // Hidden by default

        Label overlayTitle = new Label("PRODUCT DETAILS");
        overlayTitle.setStyle("-fx-text-fill: #ffb800; -fx-font-weight: bold; -fx-font-size: 12; -fx-letter-spacing: 1;");
        
        Label overlayDesc = new Label(product.getDescription() != null ? product.getDescription() : "No description available.");
         overlayDesc.setStyle("-fx-text-fill: white; -fx-font-size: 12; -fx-text-alignment: center;");
         overlayDesc.setWrapText(true);
         overlayDesc.setMaxWidth(220);
 
         Label overlayCategory = new Label("Category: " + categoryNames.getOrDefault(product.getCategoryId(), "General"));
         overlayCategory.setStyle("-fx-text-fill: #ffb800; -fx-font-size: 11; -fx-font-style: italic;");
 
         hoverOverlay.getChildren().addAll(overlayTitle, overlayDesc, overlayCategory);
        
        Rectangle clip = new Rectangle(260, 220);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imgContainer.setClip(clip);
        imgContainer.getChildren().addAll(img, hoverOverlay);

        // Trending Badge
        if (isTrending) {
            Label trendingBadge = new Label("TRENDING 🔥");
            trendingBadge.setStyle("-fx-background-color: #ffb800; -fx-text-fill: #0a0a0f; -fx-padding: 5 10; -fx-font-weight: bold; -fx-font-size: 10; -fx-background-radius: 4;");
            StackPane.setAlignment(trendingBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(trendingBadge, new Insets(10));
            imgContainer.getChildren().add(trendingBadge);
            
            card.setStyle(card.getStyle() + "-fx-border-color: #ffb800; -fx-border-width: 1;");
        }

        // Content Container
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(product.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
        name.setWrapText(true);
        name.setPrefHeight(45);
        name.setAlignment(Pos.TOP_LEFT);

        // Price Row
        HBox priceRow = new HBox(10);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        
        VBox priceContainer = new VBox(2);
        priceContainer.setMinWidth(100); // Prevent truncation
        if (product.getDiscountPrice() != null && product.getDiscountPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            Text oldPrice = new Text(product.getPrice() + " USD");
            oldPrice.setFill(Color.web("#71717a"));
            oldPrice.setStyle("-fx-font-size: 12;");
            oldPrice.setStrikethrough(true);
            
            Label price = new Label(product.getDiscountPrice() + " USD");
            price.getStyleClass().add("neon-label");
            price.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
            price.setMinWidth(Region.USE_PREF_SIZE);
            priceContainer.getChildren().addAll(oldPrice, price);
        } else {
            Label price = new Label(product.getPrice() + " USD");
            price.getStyleClass().add("neon-label");
            price.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
            price.setMinWidth(Region.USE_PREF_SIZE);
            priceContainer.getChildren().add(price);
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label stock = new Label(product.getStock() > 0 ? "IN STOCK" : "OUT OF STOCK");
        stock.setStyle(product.getStock() > 0 ? 
            "-fx-text-fill: #22c55e; -fx-font-size: 10; -fx-font-weight: bold;" : 
            "-fx-text-fill: #ef4444; -fx-font-size: 10; -fx-font-weight: bold;");
        
        priceRow.getChildren().addAll(priceContainer, spacer, stock);

        // Sale Badge
        if (product.getDiscountPrice() != null && product.getDiscountPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            Label saleBadge = new Label("SALE");
            saleBadge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-weight: bold; -fx-font-size: 10; -fx-background-radius: 4;");
            StackPane.setAlignment(saleBadge, Pos.TOP_LEFT);
            StackPane.setMargin(saleBadge, new Insets(10));
            imgContainer.getChildren().add(saleBadge);
        }

        Button buyBtn = new Button(product.getStock() > 0 ? "ADD TO CART" : "OUT OF STOCK");
        buyBtn.getStyleClass().add(product.getStock() > 0 ? "btn-gold" : "btn-disabled");
        buyBtn.setDisable(product.getStock() <= 0);
        buyBtn.setMaxWidth(Double.MAX_VALUE);
        buyBtn.setPrefHeight(45);
        buyBtn.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        
        if (product.getStock() > 0) {
            buyBtn.setOnAction(e -> {
                CartManager.addProduct(product);
                cartCount = CartManager.getTotalItems();
                cartCountLabel.setText(String.valueOf(cartCount));
            });
        }

        content.getChildren().addAll(name, priceRow, buyBtn);
        card.getChildren().addAll(imgContainer, content);
        
        // Hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-radius: 12; -fx-overflow: hidden; -fx-border-color: #ffb800; -fx-border-radius: 12; -fx-translate-y: -5; -fx-cursor: hand;");
            hoverOverlay.setOpacity(1);
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-radius: 12; -fx-overflow: hidden; -fx-border-color: rgba(255, 184, 0, 0.1); -fx-border-radius: 12; -fx-translate-y: 0;");
            hoverOverlay.setOpacity(0);
        });

        return card;
    }

    @FXML private void handleViewCart() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Cart.fxml");
    }

    @FXML private void handleNavHome() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml"); }
    @FXML private void handleNavTournaments() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Tournaments.fxml"); }
    @FXML private void handleNavTeams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Teams.fxml"); }
    @FXML private void handleNavMatches() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Matches.fxml"); }
    @FXML private void handleNavEvents() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Events.fxml"); }
    @FXML private void handleNavNews() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/News.fxml"); }
    @FXML private void handleNavShop() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Shop.fxml"); }
    @FXML private void handleNavStreams() { SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Streams.fxml"); }
}
