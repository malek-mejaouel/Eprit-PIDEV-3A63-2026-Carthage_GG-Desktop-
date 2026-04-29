package com.carthagegg.controllers.front;

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

import java.sql.SQLException;
import java.util.List;

public class ShopController {

    @FXML private FlowPane productsGrid;
    @FXML private SidebarController sidebarController;
    @FXML private Label cartCountLabel;
    @FXML private TextField searchField;

    private ProductDAO productDAO = new ProductDAO();
    private int cartCount = 0;
    private List<Product> allProducts = new java.util.ArrayList<>();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("shop");
        }
        cartCount = CartManager.getTotalItems();
        cartCountLabel.setText(String.valueOf(cartCount));
        
        loadProducts();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts(newValue);
        });
    }

    private void loadProducts() {
        try {
            allProducts = productDAO.findAll();
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
        
        Rectangle clip = new Rectangle(260, 220);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imgContainer.setClip(clip);
        imgContainer.getChildren().add(img);

        // Content Container
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(product.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
        name.setWrapText(true);
        name.setPrefHeight(45);
        name.setAlignment(Pos.TOP_LEFT);
        
        HBox priceRow = new HBox(10);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        
        Label price = new Label(product.getPrice() + " USD");
        price.getStyleClass().add("neon-label");
        price.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label stock = new Label(product.getStock() > 0 ? "IN STOCK" : "OUT OF STOCK");
        stock.setStyle(product.getStock() > 0 ? 
            "-fx-text-fill: #22c55e; -fx-font-size: 10; -fx-font-weight: bold;" : 
            "-fx-text-fill: #ef4444; -fx-font-size: 10; -fx-font-weight: bold;");
        
        priceRow.getChildren().addAll(price, spacer, stock);

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
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-radius: 12; -fx-overflow: hidden; -fx-border-color: #ffb800; -fx-border-radius: 12; -fx-translate-y: -5; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-radius: 12; -fx-overflow: hidden; -fx-border-color: rgba(255, 184, 0, 0.1); -fx-border-radius: 12; -fx-translate-y: 0;"));

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
