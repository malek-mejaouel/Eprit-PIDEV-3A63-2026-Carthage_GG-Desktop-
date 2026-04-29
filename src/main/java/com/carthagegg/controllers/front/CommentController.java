package com.carthagegg.controllers.front;

import com.carthagegg.dao.CommentDAO;
import com.carthagegg.models.Comment;
import com.carthagegg.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommentController {

    private CommentDAO commentDAO = new CommentDAO();
    private int newsId;
    private VBox listBox;

    public CommentController() {}

    public CommentController(int newsId, VBox listBox) {
        this.newsId = newsId;
        this.listBox = listBox;
    }

    public void setNewsIdAndListBox(int newsId, VBox listBox) {
        this.newsId = newsId;
        this.listBox = listBox;
    }

    public VBox buildCommentsSection(int newsId) {
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

        Label errorLabel = new Label("You have to write something");
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        HBox composer = new HBox(10, input, post);
        HBox.setHgrow(input, javafx.scene.layout.Priority.ALWAYS);

        VBox composerWrapper = new VBox(5, composer, errorLabel);

        input.textProperty().addListener((obs, oldVal, newVal) -> {
            if (errorLabel.isVisible() && !newVal.trim().isEmpty()) {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
        });

        post.setOnAction(e -> {
            String content = input.getText() != null ? input.getText().trim() : "";
            if (content.isEmpty()) {
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
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
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
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

        panel.getChildren().addAll(listBox, composerWrapper);
        wrapper.getChildren().addAll(toggle, panel);
        return wrapper;
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
                        ImageView gif = new ImageView(new Image(c.getGifUrl()));
                        gif.setFitWidth(200);
                        gif.setPreserveRatio(true);
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
}
