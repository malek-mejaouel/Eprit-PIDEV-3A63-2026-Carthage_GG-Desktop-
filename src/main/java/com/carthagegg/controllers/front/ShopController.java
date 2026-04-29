package com.carthagegg.controllers.front;

import com.carthagegg.dao.CategoryDAO;
import com.carthagegg.dao.OrderDAO;
import com.carthagegg.dao.ProductDAO;
import com.carthagegg.models.Category;
import com.carthagegg.models.Product;
import com.carthagegg.utils.CartManager;
import com.carthagegg.utils.SceneNavigator;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
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
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShopController {

    @FXML private FlowPane productsGrid;
    @FXML private SidebarController sidebarController;
    @FXML private Label cartCountLabel;
    @FXML private TextField searchField;

    private ProductDAO productDAO = new ProductDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
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
        
        loadData();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts(newValue);
        });
    }

    private void loadData() {
        try {
            allProducts = productDAO.findAll();
            salesCounts = orderDAO.getProductSalesCounts();
            categoryNames = categoryDAO.findAll().stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
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
        
        // Hover Overlay
        VBox hoverOverlay = new VBox(10);
        hoverOverlay.setAlignment(Pos.CENTER);
        hoverOverlay.setPadding(new Insets(20));
        hoverOverlay.setStyle("-fx-background-color: rgba(10, 10, 15, 0.9); -fx-background-radius: 12 12 0 0;");
        hoverOverlay.setOpacity(0);

        Label overlayTitle = new Label("PRODUCT DETAILS");
        overlayTitle.setStyle("-fx-text-fill: #ffb800; -fx-font-weight: bold; -fx-font-size: 12;");
        
        Label overlayDesc = new Label(product.getDescription() != null && !product.getDescription().isEmpty() 
            ? product.getDescription() : "No description available.");
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
        priceContainer.setMinWidth(100);
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
        
        // Hover Transitions
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), hoverOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), hoverOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition liftUp = new TranslateTransition(Duration.millis(200), card);
        liftUp.setToY(-8);

        TranslateTransition settleDown = new TranslateTransition(Duration.millis(200), card);
        settleDown.setToY(0);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-radius: 12; -fx-overflow: hidden; -fx-border-color: #ffb800; -fx-border-radius: 12; -fx-cursor: hand; -fx-background-color: #1a1a24;");
            fadeOut.stop();
            fadeIn.playFromStart();
            settleDown.stop();
            liftUp.playFromStart();
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-radius: 12; -fx-overflow: hidden; -fx-border-color: rgba(255, 184, 0, 0.1); -fx-border-radius: 12; -fx-background-color: transparent;");
            fadeIn.stop();
            fadeOut.playFromStart();
            liftUp.stop();
            settleDown.playFromStart();
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
