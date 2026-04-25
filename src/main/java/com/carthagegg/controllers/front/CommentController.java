package com.carthagegg.controllers.front;

import com.carthagegg.dao.CommentDAO;
import com.carthagegg.models.Comment;
import com.carthagegg.utils.GiphyService;
import com.carthagegg.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommentController {

    private CommentDAO commentDAO;
    private int newsId;
    private VBox listBox;
    private String selectedGifUrl = null;

    public CommentController() {
        try {
            commentDAO = new CommentDAO();
        } catch (Throwable t) {
            System.err.println("CommentController Init Error: " + t.getMessage());
        }
    }

    public CommentController(int newsId, VBox listBox) {
        this.newsId = newsId;
        this.listBox = listBox;
    }

    public void setNewsIdAndListBox(int newsId, VBox listBox) {
        this.newsId = newsId;
        this.listBox = listBox;
    }

    public VBox buildCommentsSection(int newsId) {
        this.newsId = newsId;
        VBox panel = new VBox(10);
        panel.setStyle("-fx-padding: 10; -fx-background-color: #0d0d15;");

        VBox listBox = new VBox(10);
        this.listBox = listBox;
        loadCommentsInto(newsId, listBox);

        TextField input = new TextField();
        input.setPromptText("Write a comment...");
        input.getStyleClass().add("text-field-dark");

        Button post = new Button("Post");
        post.getStyleClass().add("btn-primary");
        post.setPrefHeight(36);

        Button gifBtn = new Button("GIF");
        gifBtn.getStyleClass().add("btn-secondary");
        gifBtn.setPrefHeight(36);
        gifBtn.setStyle("-fx-background-color: #4B0082; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox gifPreviewContainer = new VBox();
        gifPreviewContainer.setVisible(false);
        gifPreviewContainer.setManaged(false);
        gifPreviewContainer.setPadding(new Insets(5, 0, 5, 0));

        VBox giphyPicker = createGiphyPicker(url -> {
            selectedGifUrl = url;
            gifPreviewContainer.getChildren().clear();
            
            ImageView preview = new ImageView();
            preview.setFitHeight(100);
            preview.setPreserveRatio(true);
            
            // Use the same robust downloader for the preview image
            downloadGifAsync(url, preview);
            
            Button removeGif = new Button("✕");
            removeGif.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;");
            removeGif.setOnAction(ev -> {
                selectedGifUrl = null;
                gifPreviewContainer.setVisible(false);
                gifPreviewContainer.setManaged(false);
            });
            
            HBox previewWrapper = new HBox(10, preview, removeGif);
            previewWrapper.setAlignment(Pos.TOP_LEFT);
            
            gifPreviewContainer.getChildren().add(previewWrapper);
            gifPreviewContainer.setVisible(true);
            gifPreviewContainer.setManaged(true);
        });
        giphyPicker.setVisible(false);
        giphyPicker.setManaged(false);

        gifBtn.setOnAction(e -> {
            boolean show = !giphyPicker.isVisible();
            giphyPicker.setVisible(show);
            giphyPicker.setManaged(show);
            if (show && giphyPicker.getUserData() instanceof Runnable) {
                ((Runnable) giphyPicker.getUserData()).run();
                giphyPicker.setUserData(null); // Load only once
            }
        });

        Label errorLabel = new Label("You have to write something");
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        HBox composer = new HBox(10, input, gifBtn, post);
        HBox.setHgrow(input, javafx.scene.layout.Priority.ALWAYS);

        VBox composerWrapper = new VBox(5, gifPreviewContainer, composer, giphyPicker, errorLabel);

        input.textProperty().addListener((obs, oldVal, newVal) -> {
            if (errorLabel.isVisible() && !newVal.trim().isEmpty()) {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
        });

        post.setOnAction(e -> {
            String content = input.getText() != null ? input.getText().trim() : "";
            if (content.isEmpty() && selectedGifUrl == null) {
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }
            if (SessionManager.getCurrentUser() == null) {
                showInlineError(listBox, "You must be signed in to comment.");
                return;
            }
            if (commentDAO == null) {
                showInlineError(listBox, "Comments service is unavailable.");
                return;
            }
            try {
                Comment c = new Comment();
                c.setContenu(content);
                c.setGifUrl(selectedGifUrl);
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
                selectedGifUrl = null;
                gifPreviewContainer.setVisible(false);
                gifPreviewContainer.setManaged(false);
                giphyPicker.setVisible(false);
                giphyPicker.setManaged(false);
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
                loadCommentsInto(newsId, listBox);
            } catch (SQLException ex) {
                showInlineError(listBox, "Comments are not available (database).");
            }
        });

        panel.getChildren().addAll(listBox, composerWrapper);
        return panel;
    }

    public void loadCommentsInto(int newsId, VBox listBox) {
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
                        String avatarPath = c.getAvatar();
                        if (avatarPath.startsWith("http") || avatarPath.startsWith("file:")) {
                            avatar.setImage(new Image(avatarPath));
                        } else {
                            avatar.setImage(new Image(java.nio.file.Path.of(avatarPath).toUri().toString()));
                        }
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
                        ImageView gif = new ImageView();
                        gif.setFitWidth(200);
                        gif.setPreserveRatio(true);
                        // Use the same robust downloader for displayed GIFs in comments
                        downloadGifAsync(c.getGifUrl(), gif);
                        row.getChildren().add(gif);
                    } catch (Exception e) {}
                }

                HBox footer = new HBox(15);
                footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Hyperlink upvote = new Hyperlink("▲ " + c.getUpvotes());
                upvote.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 11;");

                Hyperlink downvote = new Hyperlink("▼ " + c.getDownvotes());
                downvote.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 11;");

                final int[] userVote = {0};

                upvote.setOnAction(ev -> {
                    if (SessionManager.getCurrentUser() == null) return;
                    try {
                        if (userVote[0] == 1) {
                            c.setUpvotes(Math.max(0, c.getUpvotes() - 1));
                            userVote[0] = 0;
                            upvote.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 11;");
                        } else {
                            if (userVote[0] == -1) {
                                c.setDownvotes(Math.max(0, c.getDownvotes() - 1));
                                downvote.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 11;");
                            }
                            c.setUpvotes(c.getUpvotes() + 1);
                            userVote[0] = 1;
                            upvote.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 11; -fx-font-weight: bold; -fx-underline: true;");
                        }

                        commentDAO.updateVotes(c.getCommentaireId(), c.getUpvotes(), c.getDownvotes());
                        upvote.setText("▲ " + c.getUpvotes());
                        downvote.setText("▼ " + c.getDownvotes());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                downvote.setOnAction(ev -> {
                    if (SessionManager.getCurrentUser() == null) return;
                    try {
                        if (userVote[0] == -1) {
                            c.setDownvotes(Math.max(0, c.getDownvotes() - 1));
                            userVote[0] = 0;
                            downvote.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 11;");
                        } else {
                            if (userVote[0] == 1) {
                                c.setUpvotes(Math.max(0, c.getUpvotes() - 1));
                                upvote.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 11;");
                            }
                            c.setDownvotes(c.getDownvotes() + 1);
                            userVote[0] = -1;
                            downvote.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 11; -fx-font-weight: bold; -fx-underline: true;");
                        }

                        commentDAO.updateVotes(c.getCommentaireId(), c.getUpvotes(), c.getDownvotes());
                        upvote.setText("▲ " + c.getUpvotes());
                        downvote.setText("▼ " + c.getDownvotes());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                Hyperlink reply = new Hyperlink("Reply");
                reply.setStyle("-fx-text-fill: #949499; -fx-font-size: 11;");
                reply.setOnAction(e -> {
                    VBox panel = (VBox) row.getParent().getParent();
                    HBox composer = (HBox) ((VBox) panel.getChildren().get(panel.getChildren().size() - 1)).getChildren().get(0);
                    TextField input = (TextField) composer.getChildren().get(0);
                    input.setPromptText("Replying to " + (c.getUsername() != null ? c.getUsername() : "#" + c.getCommentaireId()) + "...");
                    input.setUserData(c.getCommentaireId());
                    input.requestFocus();
                });

                footer.getChildren().addAll(upvote, downvote, reply);

                int currentUserId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getUserId() : -1;
                if (currentUserId > 0 && c.getUserId() == currentUserId) {
                    Hyperlink edit = new Hyperlink("Edit");
                    edit.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 11;");
                    edit.setOnAction(ev -> enterEditMode(row, c, listBox, newsId));

                    Hyperlink delete = new Hyperlink("Delete");
                    delete.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11;");
                    delete.setOnAction(ev -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Delete Comment");
                        alert.setHeaderText("Are you sure you want to delete this comment?");
                        alert.setContentText("This action cannot be undone.");
                        
                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                try {
                                    commentDAO.delete(c.getCommentaireId());
                                    loadCommentsInto(newsId, listBox);
                                } catch (SQLException ex) {
                                    showInlineError(listBox, "Failed to delete comment.");
                                }
                            }
                        });
                    });

                    footer.getChildren().addAll(edit, delete);
                }

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

    public void enterEditMode(VBox row, Comment comment, VBox listBox, int newsId) {
        row.getChildren().clear();

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ImageView avatar = new ImageView();
        avatar.setFitHeight(30);
        avatar.setFitWidth(30);
        avatar.setClip(new javafx.scene.shape.Circle(15, 15, 15));
        if (comment.getAvatar() != null && !comment.getAvatar().isEmpty()) {
            try {
                String avatarPath = comment.getAvatar();
                if (avatarPath.startsWith("http") || avatarPath.startsWith("file:")) {
                    avatar.setImage(new Image(avatarPath));
                } else {
                    avatar.setImage(new Image(java.nio.file.Path.of(avatarPath).toUri().toString()));
                }
            } catch (Exception e) {
                avatar.setImage(new Image(getClass().getResourceAsStream("/images/zz.png")));
            }
        } else {
            avatar.setImage(new Image(getClass().getResourceAsStream("/images/zz.png")));
        }

        VBox userMeta = new VBox(2);
        Label uname = new Label(comment.getUsername() != null ? comment.getUsername() : "User #" + comment.getUserId());
        uname.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-font-size: 13;");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        String dateStr = (comment.getDateCommentaire() != null ? comment.getDateCommentaire().format(fmt) : "Recently");
        if (comment.getParentId() > 0) {
            dateStr += " • Replying to #" + comment.getParentId();
        }
        Label meta = new Label(dateStr + " (Editing)");
        meta.setStyle("-fx-text-fill: #949499; -fx-font-size: 10;");

        userMeta.getChildren().addAll(uname, meta);
        header.getChildren().addAll(avatar, userMeta);

        TextArea editInput = new TextArea(comment.getContenu());
        editInput.setWrapText(true);
        editInput.setPrefRowCount(3);
        editInput.getStyleClass().add("text-field-dark");
        editInput.setStyle("-fx-control-inner-background: #1e1e2e; -fx-text-fill: white; -fx-font-size: 13;");

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> {
            String newContent = editInput.getText() != null ? editInput.getText().trim() : "";
            if (newContent.isEmpty()) {
                return;
            }
            try {
                comment.setContenu(newContent);
                commentDAO.update(comment);
                loadCommentsInto(newsId, listBox);
            } catch (SQLException ex) {
                showInlineError(listBox, "Failed to update comment.");
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> loadCommentsInto(newsId, listBox));

        HBox buttonRow = new HBox(10, saveBtn, cancelBtn);
        buttonRow.setPadding(new Insets(5, 0, 0, 0));

        row.getChildren().addAll(header, editInput, buttonRow);
        row.requestLayout();
    }

    private final java.util.concurrent.ExecutorService downloadPool = java.util.concurrent.Executors.newFixedThreadPool(4);

    private void downloadGifAsync(String url, ImageView target) {
        downloadPool.submit(() -> {
            try {
                java.net.URL imageUrl = new java.net.URL(url);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imageUrl.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
                connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
                connection.setConnectTimeout(15000); // Increased to 15s
                connection.setReadTimeout(15000);
                
                try (java.io.InputStream is = connection.getInputStream()) {
                    Image image = new Image(is);
                    javafx.application.Platform.runLater(() -> {
                        target.setImage(image);
                    });
                }
            } catch (Exception ex) {
                System.err.println("Giphy Download Error: " + url + " -> " + ex.getMessage());
            }
        });
    }

    private VBox createGiphyPicker(java.util.function.Consumer<String> onGifSelected) {
        VBox picker = new VBox(10);
        picker.getStyleClass().add("card");
        picker.setPadding(new Insets(10));
        picker.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #3f3f46; -fx-border-width: 1; -fx-border-radius: 5;");
        picker.setPrefHeight(300);

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search GIFs...");
        searchBar.getStyleClass().add("text-field-dark");

        FlowPane gifGrid = new FlowPane(5, 5);
        gifGrid.setPadding(new Insets(10));
        gifGrid.setAlignment(Pos.TOP_LEFT);
        gifGrid.setPrefWidth(380);
        gifGrid.setMinWidth(380);
        gifGrid.setStyle("-fx-background-color: #1a1a2e;");

        ScrollPane scrollPane = new ScrollPane(gifGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e; -fx-border-color: #3f3f46;");

        Runnable loadTrending = () -> {
            System.out.println("Giphy: Loading trending...");
            javafx.application.Platform.runLater(() -> {
                gifGrid.getChildren().clear();
                Label loading = new Label("Loading GIFs...");
                loading.setStyle("-fx-text-fill: #FFC107;");
                gifGrid.getChildren().add(loading);
            });

            new Thread(() -> {
                try {
                    List<String> gifs = GiphyService.getTrendingGifs();
                    System.out.println("Giphy: Fetched " + gifs.size() + " trending URLs");
                    javafx.application.Platform.runLater(() -> {
                        gifGrid.getChildren().clear();
                        if (gifs.isEmpty()) {
                            Label noGifs = new Label("No GIFs found. Check internet.");
                            noGifs.setStyle("-fx-text-fill: #ef4444;");
                            gifGrid.getChildren().add(noGifs);
                        }
                        for (String url : gifs) {
                            try {
                                ImageView img = new ImageView();
                                img.setFitWidth(110);
                                img.setFitHeight(110);
                                img.setPreserveRatio(true);
                                
                                downloadGifAsync(url, img);

                                img.setCursor(javafx.scene.Cursor.HAND);
                                img.setOnMouseClicked(e -> {
                                    onGifSelected.accept(url);
                                    picker.setVisible(false);
                                    picker.setManaged(false);
                                });
                                gifGrid.getChildren().add(img);
                            } catch (Exception e) {
                                System.err.println("Giphy UI: Error creating ImageView: " + e.getMessage());
                            }
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Giphy UI: Thread error: " + e.getMessage());
                }
            }).start();
        };

        searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal.trim();
            if (query.isEmpty()) {
                loadTrending.run();
                return;
            }
            
            gifGrid.getChildren().clear();
            Label searching = new Label("Searching...");
            searching.setStyle("-fx-text-fill: #FFC107;");
            gifGrid.getChildren().add(searching);

            new Thread(() -> {
                try {
                    List<String> gifs = GiphyService.searchGifs(query);
                    System.out.println("Giphy: Fetched " + gifs.size() + " search results for: " + query);
                    javafx.application.Platform.runLater(() -> {
                        gifGrid.getChildren().clear();
                        if (gifs.isEmpty()) {
                            Label noResults = new Label("No results found.");
                            noResults.setStyle("-fx-text-fill: #949499;");
                            gifGrid.getChildren().add(noResults);
                        }
                        for (String url : gifs) {
                            try {
                                ImageView img = new ImageView();
                                img.setFitWidth(110);
                                img.setFitHeight(110);
                                img.setPreserveRatio(true);
                                
                                downloadGifAsync(url, img);
                                
                                img.setCursor(javafx.scene.Cursor.HAND);
                                img.setOnMouseClicked(e -> {
                                    onGifSelected.accept(url);
                                    picker.setVisible(false);
                                    picker.setManaged(false);
                                });
                                gifGrid.getChildren().add(img);
                            } catch (Exception e) {}
                        }
                    });
                } catch (Exception e) {}
            }).start();
        });

        picker.setUserData(loadTrending);
        picker.getChildren().addAll(searchBar, scrollPane);
        return picker;
    }
}
