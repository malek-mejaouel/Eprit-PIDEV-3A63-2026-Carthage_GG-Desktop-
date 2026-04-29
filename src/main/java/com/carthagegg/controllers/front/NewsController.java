package com.carthagegg.controllers.front;

import com.carthagegg.dao.NewsDAO;
import com.carthagegg.models.News;
import com.carthagegg.services.NewsService;
import com.carthagegg.utils.NewsApiService;
import com.carthagegg.utils.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class NewsController {

    @FXML private FlowPane newsContainer;
    @FXML private SidebarController sidebarController;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> dateSortCombo;
    @FXML private Button worldNewsBtn;

    private NewsDAO newsDAO = new NewsDAO();
    private NewsService newsService = new NewsService();
    private CommentController commentController = new CommentController();
    private NewsApiService newsApiService = new NewsApiService();

    private boolean showingWorldNews = false;

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("news");
        }
        dateSortCombo.getItems().addAll("Hot", "Latest", "Oldest");
        dateSortCombo.setValue("Hot");
        
        dateSortCombo.setOnAction(e -> {
            if (showingWorldNews) return; // Ignore sorting if on world news
            if (!searchField.getText().isEmpty()) {
                handleSearch();
            } else {
                loadNews();
            }
        });
        
        loadNews();
    }

    @FXML
    private void handleSearch() {
        showingWorldNews = false; // Reset flag when searching local news
        String query = searchField.getText() != null ? searchField.getText().trim() : "";
        String sortValue = dateSortCombo.getValue();
        
        newsContainer.getChildren().clear();
        Label loading = new Label("Searching news...");
        loading.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 16;");
        newsContainer.getChildren().add(loading);
        
        new Thread(() -> {
            try {
                List<News> newsList;
                if (query.isEmpty()) {
                    if ("Hot".equals(sortValue)) {
                        newsList = newsService.getSortedNewsByScore();
                    } else {
                        String sortDir = "Oldest".equals(sortValue) ? "ASC" : "DESC";
                        newsList = newsDAO.findAll(sortDir);
                    }
                } else {
                    if ("Hot".equals(sortValue)) {
                        // For Hot + Search, fetch all sorted by score, then filter by title
                        newsList = newsService.getSortedNewsByScore();
                        String lowerQuery = query.toLowerCase();
                        newsList.removeIf(n -> n.getTitle() == null || !n.getTitle().toLowerCase().contains(lowerQuery));
                    } else {
                        String sortDir = "Oldest".equals(sortValue) ? "ASC" : "DESC";
                        newsList = newsDAO.findByTitle(query, sortDir);
                    }
                }
                
                Platform.runLater(() -> {
                    newsContainer.getChildren().clear();
                    
                    // Filter to only show articles with images
                    newsList.removeIf(n -> n.getImage() == null || n.getImage().trim().isEmpty());
                    
                    if (newsList.isEmpty()) {
                        Label noNews = new Label("No news found matching your search.");
                        noNews.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
                        newsContainer.getChildren().add(noNews);
                    } else {
                        for (News news : newsList) {
                            newsContainer.getChildren().add(createNewsCard(news));
                        }
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    newsContainer.getChildren().clear();
                    Label error = new Label("Error loading news.");
                    error.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16;");
                    newsContainer.getChildren().add(error);
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleFetchWorldNews() {
        showingWorldNews = true;
        worldNewsBtn.setDisable(true);
        worldNewsBtn.setText("Fetching...");
        
        newsApiService.fetchEsportsNewsAsync().thenAccept(externalNews -> {
            Platform.runLater(() -> {
                newsContainer.getChildren().clear();
                
                // Filter to only show articles with images
                externalNews.removeIf(n -> n.getImage() == null || n.getImage().trim().isEmpty());

                if (externalNews.isEmpty()) {
                    Label noNews = new Label("No worldwide news found at the moment.");
                    noNews.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
                    newsContainer.getChildren().add(noNews);
                } else {
                    for (News news : externalNews) {
                        newsContainer.getChildren().add(createNewsCard(news));
                    }
                }
                worldNewsBtn.setDisable(false);
                worldNewsBtn.setText("Worldwide Esports News");
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                worldNewsBtn.setDisable(false);
                worldNewsBtn.setText("Worldwide Esports News");
                ex.printStackTrace();
            });
            return null;
        });
    }

    private void loadNews() {
        showingWorldNews = false;
        String sortValue = dateSortCombo.getValue();
        
        newsContainer.getChildren().clear();
        Label loading = new Label("Loading news...");
        loading.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 16;");
        newsContainer.getChildren().add(loading);
        
        new Thread(() -> {
            try {
                List<News> newsList;
                if ("Hot".equals(sortValue)) {
                    newsList = newsService.getSortedNewsByScore();
                } else {
                    String sortDir = "Oldest".equals(sortValue) ? "ASC" : "DESC";
                    newsList = newsDAO.findAll(sortDir);
                }
                
                Platform.runLater(() -> {
                    newsContainer.getChildren().clear();
                    
                    // Filter to only show articles with images
                    newsList.removeIf(n -> n.getImage() == null || n.getImage().trim().isEmpty());

                    if (newsList.isEmpty()) {
                        Label noNews = new Label("No news available.");
                        noNews.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
                        newsContainer.getChildren().add(noNews);
                    } else {
                        for (News news : newsList) {
                            newsContainer.getChildren().add(createNewsCard(news));
                        }
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    newsContainer.getChildren().clear();
                    Label error = new Label("Error loading news.");
                    error.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16;");
                    newsContainer.getChildren().add(error);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private VBox createNewsCard(News news) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(0));
        card.setPrefWidth(350);
        card.setMinWidth(350);
        card.setMaxWidth(350);
        card.setStyle("-fx-background-color: #18181b; -fx-background-radius: 12; -fx-overflow: hidden;");

        ImageView img = new ImageView();
        img.setFitHeight(200);
        img.setFitWidth(350);
        img.setPreserveRatio(false);
        // Apply rounded corners to image top
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(350, 200);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        img.setClip(clip);

        if (news.getImage() != null && !news.getImage().isEmpty()) {
            try {
                String src = news.getImage().trim();
                if (src.startsWith("http://") || src.startsWith("https://")) {
                    img.setImage(new Image(src, true)); // load in background to prevent UI freeze
                } else if (src.startsWith("file:")) {
                    img.setImage(new Image(src, false)); // load synchronously for instant display
                } else {
                    img.setImage(new Image(Path.of(src).toUri().toString(), false));
                }
            } catch (Exception e) {}
        }

        VBox content = new VBox(8);
        content.setPadding(new Insets(15));

        if (news.getCategory() != null) {
            Label cat = new Label(news.getCategory().toUpperCase());
            cat.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-font-size: 10; -fx-background-color: rgba(255, 193, 7, 0.1); -fx-padding: 4 8; -fx-background-radius: 4;");
            content.getChildren().add(cat);
        }

        Label title = new Label(news.getTitle());
        title.setWrapText(true);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");

        Label date = new Label(news.getPublishedAt() != null ? news.getPublishedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")) : "Recent");
        date.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12;");

        Label text = new Label(news.getContent());
        text.setWrapText(true);
        text.setMaxHeight(40);
        text.setStyle("-fx-text-fill: #949499; -fx-font-size: 13;");

        Button readMore = new Button("Read More →");
        readMore.getStyleClass().add("btn-primary");
        readMore.setStyle("-fx-font-size: 12; -fx-padding: 8 15;");
        readMore.setOnAction(e -> {
            NewsDetailController.setSelectedNews(news);
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/NewsDetail.fxml");
        });

        content.getChildren().addAll(title, date, text, readMore);
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
