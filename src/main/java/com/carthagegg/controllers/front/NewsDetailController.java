package com.carthagegg.controllers.front;

import com.carthagegg.models.News;
import com.carthagegg.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class NewsDetailController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox contentContainer;
    @FXML private ImageView newsImage;
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label dateLabel;
    @FXML private Label contentText;
    @FXML private VBox commentsSectionContainer;
    @FXML private SidebarController sidebarController;

    private static News selectedNews;
    private CommentController commentController;

    public static void setSelectedNews(News news) {
        selectedNews = news;
    }

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("news");
        }
        
        if (selectedNews != null) {
            displayNews();
        }
    }

    private void displayNews() {
        titleLabel.setText(selectedNews.getTitle());
        
        if (selectedNews.getCategory() != null) {
            categoryLabel.setText(selectedNews.getCategory().toUpperCase());
            categoryLabel.setVisible(true);
            categoryLabel.setManaged(true);
        } else {
            categoryLabel.setVisible(false);
            categoryLabel.setManaged(false);
        }

        dateLabel.setText("Published on " + (selectedNews.getPublishedAt() != null ? 
            selectedNews.getPublishedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) : "Recent"));
        
        contentText.setText(selectedNews.getContent());

        if (selectedNews.getImage() != null && !selectedNews.getImage().isEmpty()) {
            try {
                String src = selectedNews.getImage().trim();
                if (src.startsWith("http://") || src.startsWith("https://") || src.startsWith("file:")) {
                    newsImage.setImage(new Image(src));
                } else {
                    newsImage.setImage(new Image(Path.of(src).toUri().toString()));
                }
            } catch (Exception e) {}
        }

        // Add comments section
        commentsSectionContainer.getChildren().clear();
        
        // Lazy load the comment section to avoid blocking the UI thread during FXML loading
        javafx.application.Platform.runLater(() -> {
            try {
                if (commentController == null) {
                    commentController = new CommentController();
                }
                commentsSectionContainer.getChildren().add(commentController.buildCommentsSection(selectedNews.getNewsId()));
            } catch (Throwable t) {
                System.err.println("Error building comments section: " + t.getMessage());
            }
        });
    }

    @FXML
    private void handleBack() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/front/News.fxml");
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
