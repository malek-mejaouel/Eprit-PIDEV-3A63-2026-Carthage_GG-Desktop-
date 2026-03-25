package com.carthagegg.controllers.front;

import com.carthagegg.dao.NewsDAO;
import com.carthagegg.models.News;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;

import java.sql.SQLException;
import java.util.List;

public class NewsController {

    @FXML private VBox newsContainer;
    @FXML private SidebarController sidebarController;

    private NewsDAO newsDAO = new NewsDAO();

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("news");
        }
        loadNews();
    }

    private void loadNews() {
        try {
            List<News> newsList = newsDAO.findAll();
            newsContainer.getChildren().clear();
            
            for (News news : newsList) {
                newsContainer.getChildren().add(createNewsCard(news));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createNewsCard(News news) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(0)); // Image will be flush with top
        card.setClip(null);

        ImageView img = new ImageView();
        img.setFitHeight(200);
        img.setFitWidth(800);
        img.setPreserveRatio(false);
        if (news.getImage() != null && !news.getImage().isEmpty()) {
            try {
                img.setImage(new Image(news.getImage()));
            } catch (Exception e) {}
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label title = new Label(news.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20;");
        
        Label date = new Label("Published on " + (news.getPublishedAt() != null ? news.getPublishedAt().toLocalDate().toString() : "Recent"));
        date.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12;");

        Label text = new Label(news.getContent());
        text.setWrapText(true);
        text.setStyle("-fx-text-fill: #e4e4e7;");

        content.getChildren().addAll(title, date, text);
        card.getChildren().addAll(img, content);
        
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
