package com.carthagegg.controllers.front;

import com.carthagegg.dao.NewsDAO;
import com.carthagegg.models.News;
import com.carthagegg.services.NewsService;
import com.carthagegg.utils.GroqAiService;
import com.carthagegg.utils.NewsApiService;
import com.carthagegg.utils.VoiceService;
import com.carthagegg.utils.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.sql.SQLException;
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
    
    // AI Summarization
    @FXML private Button summarizeBtn;
    @FXML private VBox summaryBox;
    @FXML private Label summaryText;
    @FXML private Button readAloudBtn;
    
    private boolean isReading = false;
    
    private GroqAiService groqAiService = new GroqAiService();
    private NewsApiService newsApiService = new NewsApiService();
    private NewsDAO newsDAO = new NewsDAO();

    private static News selectedNews;
    private CommentController commentController;
    private NewsService newsService = new NewsService();

    public static void setSelectedNews(News news) {
        selectedNews = news;
    }

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("news");
        }
        
        if (selectedNews != null) {
            // Ensure external news is persisted so comments can work
            ensureNewsExistsInDB();
            
            // Increment view count when article is opened
            newsService.incrementViewCount(selectedNews.getNewsId());
            displayNews();
        }
    }

    private void ensureNewsExistsInDB() {
        if (selectedNews.getNewsId() > 0) return; // Already in DB

        try {
            News existing = newsDAO.findByTitleStrict(selectedNews.getTitle());
            if (existing != null) {
                selectedNews.setNewsId(existing.getNewsId());
                return;
            }

            // Save to DB to generate an ID
            newsDAO.save(selectedNews);
        } catch (SQLException e) {
            System.err.println("Error persisting external news: " + e.getMessage());
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

        // For external news, fetch full content automatically
        if (selectedNews.getUrl() != null && !selectedNews.getUrl().isEmpty()) {
            contentText.setText("Loading full article content...");
            newsApiService.fetchFullContentAsync(selectedNews.getUrl(), selectedNews.getTitle())
                .thenAccept(fullContent -> {
                    Platform.runLater(() -> {
                        contentText.setText(fullContent);
                        selectedNews.setContent(fullContent); // Cache it for this session
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        contentText.setText(selectedNews.getContent() + "\n\n(Note: Unable to load full content from source.)");
                    });
                    return null;
                });
        }

        if (selectedNews.getImage() != null && !selectedNews.getImage().isEmpty()) {
            try {
                String src = selectedNews.getImage().trim();
                if (src.startsWith("http://") || src.startsWith("https://")) {
                    newsImage.setImage(new Image(src, true));
                } else if (src.startsWith("file:")) {
                    newsImage.setImage(new Image(src, false));
                } else {
                    newsImage.setImage(new Image(Path.of(src).toUri().toString(), false));
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
        VoiceService.stopSpeaking();
        SceneNavigator.navigateTo("/com/carthagegg/fxml/front/News.fxml");
    }

    @FXML
    private void handleReadAloud() {
        if (selectedNews == null) return;

        FontIcon icon = (FontIcon) readAloudBtn.getGraphic();

        if (isReading) {
            // Stop reading
            VoiceService.stopSpeaking();
            isReading = false;
            icon.setIconLiteral("fas-volume-up");
            readAloudBtn.setText("Read Aloud");
        } else {
            // Start reading
            isReading = true;
            icon.setIconLiteral("fas-volume-mute");
            readAloudBtn.setText("Stop Reading");

            String textToRead = selectedNews.getTitle() + ". " + contentText.getText();
            VoiceService.speak(textToRead, () -> {
                Platform.runLater(() -> {
                    if (isReading) {
                        isReading = false;
                        icon.setIconLiteral("fas-volume-up");
                        readAloudBtn.setText("Read Aloud");
                    }
                });
            });
        }
    }

    @FXML
    private void handleSummarize() {
        if (selectedNews == null) return;

        // Set loading state
        summarizeBtn.setDisable(true);
        FontIcon robotIcon = (FontIcon) summarizeBtn.getGraphic();
        robotIcon.setIconLiteral("fas-spinner");
        
        summaryBox.setVisible(true);
        summaryBox.setManaged(true);
        summaryText.setText("AI is reading the article and generating a summary...");

        // Call Gemini
        groqAiService.summarizeAsync(selectedNews.getTitle(), selectedNews.getContent())
            .thenAccept(summary -> {
                Platform.runLater(() -> {
                    summaryText.setText(summary);
                    robotIcon.setIconLiteral("fas-check");
                    summarizeBtn.setText("Summarized");
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    summaryText.setText("Sorry, I couldn't summarize this article. Please try again later.");
                    robotIcon.setIconLiteral("fas-robot");
                    summarizeBtn.setDisable(false);
                });
                return null;
            });
    }

    @FXML private void handleNavHome() { VoiceService.stopSpeaking(); SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml"); }
    @FXML private void handleNavTournaments() { VoiceService.stopSpeaking(); SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Tournaments.fxml"); }
    @FXML private void handleNavTeams() { VoiceService.stopSpeaking(); SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Teams.fxml"); }
    @FXML private void handleNavMatches() { VoiceService.stopSpeaking(); SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Matches.fxml"); }
    @FXML private void handleNavEvents() { VoiceService.stopSpeaking(); SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Events.fxml"); }
    @FXML private void handleNavNews() { VoiceService.stopSpeaking(); SceneNavigator.navigateTo("/com/carthagegg/fxml/front/News.fxml"); }
    @FXML private void handleNavShop() { VoiceService.stopSpeaking(); SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Shop.fxml"); }
    @FXML private void handleNavStreams() { VoiceService.stopSpeaking(); SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Streams.fxml"); }
}
