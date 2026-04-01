package com.carthagegg.controllers.front;

import com.carthagegg.dao.CommentDAO;
import com.carthagegg.dao.NewsDAO;
import com.carthagegg.models.Comment;
import com.carthagegg.models.News;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NewsController {

    @FXML private VBox newsContainer;
    @FXML private SidebarController sidebarController;

    private NewsDAO newsDAO = new NewsDAO();
    private CommentDAO commentDAO = new CommentDAO();

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
                String src = news.getImage().trim();
                if (src.startsWith("http://") || src.startsWith("https://") || src.startsWith("file:")) {
                    img.setImage(new Image(src));
                } else {
                    img.setImage(new Image(Path.of(src).toUri().toString()));
                }
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

        VBox commentsSection = buildCommentsSection(news.getNewsId());
        content.getChildren().add(commentsSection);
        card.getChildren().addAll(img, content);
        
        return card;
    }

    private VBox buildCommentsSection(int newsId) {
        VBox wrapper = new VBox(10);
        wrapper.setPadding(new Insets(10, 0, 0, 0));

        Hyperlink toggle = new Hyperlink("Comments");
        toggle.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");

        VBox panel = new VBox(10);
        panel.setVisible(false);
        panel.setManaged(false);

        VBox listBox = new VBox(10);

        TextField input = new TextField();
        input.setPromptText("Write a comment...");
        input.getStyleClass().add("text-field-dark");

        Button post = new Button("Post");
        post.getStyleClass().add("btn-primary");
        post.setPrefHeight(36);

        HBox composer = new HBox(10, input, post);
        HBox.setHgrow(input, javafx.scene.layout.Priority.ALWAYS);

        post.setOnAction(e -> {
            String content = input.getText() != null ? input.getText().trim() : "";
            if (content.isEmpty()) {
                return;
            }
            if (SessionManager.getCurrentUser() == null) {
                showInlineError(listBox, "You must be signed in to comment.");
                return;
            }
            try {
                Comment c = new Comment();
                c.setContenu(content);
                c.setNewsId(newsId);
                c.setUserId(SessionManager.getCurrentUser().getUserId());
                c.setUpvotes(0);
                c.setDownvotes(0);
                if (input.getUserData() instanceof Integer) {
                    c.setParentId((Integer) input.getUserData());
                }
                commentDAO.save(c);
                input.clear();
                input.setPromptText("Write a comment...");
                input.setUserData(null);
                loadCommentsInto(newsId, listBox);
            } catch (SQLException ex) {
                showInlineError(listBox, "Comments are not available (database).");
            }
        });

        toggle.setOnAction(e -> {
            boolean show = !panel.isVisible();
            panel.setVisible(show);
            panel.setManaged(show);
            if (show) {
                loadCommentsInto(newsId, listBox);
            }
        });

        panel.getChildren().addAll(listBox, composer);
        wrapper.getChildren().addAll(toggle, panel);
        return wrapper;
    }

    private void loadCommentsInto(int newsId, VBox listBox) {
        listBox.getChildren().clear();
        try {
            List<Comment> comments = commentDAO.findByNewsId(newsId);
            if (comments.isEmpty()) {
                Label empty = new Label("No comments yet.");
                empty.setStyle("-fx-text-fill: #949499;");
                listBox.getChildren().add(empty);
                return;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            for (Comment c : comments) {
                VBox row = new VBox(8);
                row.getStyleClass().add("card");
                row.setPadding(new Insets(12));
                if (c.getParentId() > 0) {
                    row.setTranslateX(30);
                    row.setStyle("-fx-background-color: #1e1e2e; -fx-border-color: #3f3f46; -fx-border-width: 0 0 0 2;");
                }

                HBox header = new HBox(10);
                header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                ImageView avatar = new ImageView();
                avatar.setFitHeight(30);
                avatar.setFitWidth(30);
                avatar.setClip(new javafx.scene.shape.Circle(15, 15, 15));
                if (c.getAvatar() != null && !c.getAvatar().isEmpty()) {
                    try {
                        avatar.setImage(new Image(c.getAvatar()));
                    } catch (Exception e) {
                        avatar.setImage(new Image(getClass().getResourceAsStream("/images/zz.png")));
                    }
                } else {
                    avatar.setImage(new Image(getClass().getResourceAsStream("/images/zz.png")));
                }

                VBox userMeta = new VBox(2);
                Label uname = new Label(c.getUsername() != null ? c.getUsername() : "User #" + c.getUserId());
                uname.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-font-size: 13;");

                String dateStr = (c.getDateCommentaire() != null ? c.getDateCommentaire().format(fmt) : "Recently");
                if (c.getParentId() > 0) {
                    dateStr += " • Replying to #" + c.getParentId();
                }
                Label meta = new Label(dateStr);
                meta.setStyle("-fx-text-fill: #949499; -fx-font-size: 10;");
                
                userMeta.getChildren().addAll(uname, meta);
                header.getChildren().addAll(avatar, userMeta);

                Label body = new Label(c.getContenu());
                body.setWrapText(true);
                body.setStyle("-fx-text-fill: white; -fx-font-size: 13;");

                row.getChildren().addAll(header, body);

                if (c.getGifUrl() != null && !c.getGifUrl().isEmpty()) {
                    try {
                        ImageView gif = new ImageView(new Image(c.getGifUrl()));
                        gif.setFitWidth(200);
                        gif.setPreserveRatio(true);
                        row.getChildren().add(gif);
                    } catch (Exception e) {}
                }

                HBox footer = new HBox(15);
                footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label votes = new Label("▲ " + c.getUpvotes() + "   ▼ " + c.getDownvotes());
                votes.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 11;");

                Hyperlink reply = new Hyperlink("Reply");
                reply.setStyle("-fx-text-fill: #949499; -fx-font-size: 11;");
                reply.setOnAction(e -> {
                    // Find the composer for this news article and set parentId
                    VBox panel = (VBox) row.getParent().getParent();
                    HBox composer = (HBox) panel.getChildren().get(panel.getChildren().size() - 1);
                    TextField input = (TextField) composer.getChildren().get(0);
                    input.setPromptText("Replying to " + (c.getUsername() != null ? c.getUsername() : "#" + c.getCommentaireId()) + "...");
                    input.setUserData(c.getCommentaireId());
                    input.requestFocus();
                });

                footer.getChildren().addAll(votes, reply);
                row.getChildren().add(footer);
                
                listBox.getChildren().add(row);
            }
        } catch (SQLException ex) {
            showInlineError(listBox, "Comments are not available (database).");
        }
    }

    private void showInlineError(VBox listBox, String message) {
        listBox.getChildren().clear();
        Label err = new Label(message);
        err.setStyle("-fx-text-fill: #ef4444;");
        listBox.getChildren().add(err);
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
