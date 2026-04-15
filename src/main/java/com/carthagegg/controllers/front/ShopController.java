package com.carthagegg.controllers.front;

import com.carthagegg.dao.ProductDAO;
import com.carthagegg.models.Product;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.SQLException;
import java.util.List;

public class ShopController {

    @FXML private FlowPane productsGrid;
    @FXML private SidebarController sidebarController;

    private ProductDAO productDAO = new ProductDAO();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("shop");
        }
        loadProducts();
    }

    private void loadProducts() {
        try {
            List<Product> products = productDAO.findAll();
            productsGrid.getChildren().clear();
            
            for (Product product : products) {
                productsGrid.getChildren().add(createProductCard(product));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15));
        card.setPrefWidth(220);
        card.setAlignment(Pos.CENTER);

        ImageView img = new ImageView();
        img.setFitHeight(150);
        img.setFitWidth(150);
        img.setPreserveRatio(true);
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try { img.setImage(new Image(product.getImage())); } catch (Exception e) {}
        }

        Label name = new Label(product.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Label price = new Label(product.getPrice() + " TND");
        price.getStyleClass().add("neon-label");
        price.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Button buyBtn = new Button("ADD TO CART");
        buyBtn.getStyleClass().add("btn-gold");
        buyBtn.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(img, name, price, buyBtn);
        return card;
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
