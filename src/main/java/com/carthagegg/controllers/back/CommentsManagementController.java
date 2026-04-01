package com.carthagegg.controllers.back;

import com.carthagegg.dao.CommentDAO;
import com.carthagegg.models.Comment;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommentsManagementController {

    @FXML private TableView<Comment> commentsTable;
    @FXML private TableColumn<Comment, Integer> colId;
    @FXML private TableColumn<Comment, String> colNewsId;
    @FXML private TableColumn<Comment, String> colUserId;
    @FXML private TableColumn<Comment, String> colDate;
    @FXML private TableColumn<Comment, String> colContent;
    @FXML private TableColumn<Comment, String> colVotes;
    @FXML private TableColumn<Comment, Void> colActions;

    private final CommentDAO commentDAO = new CommentDAO();
    private final com.carthagegg.dao.NewsDAO newsDAO = new com.carthagegg.dao.NewsDAO();
    private final ObservableList<Comment> commentsList = FXCollections.observableArrayList();
    private final java.util.Map<Integer, String> newsTitleById = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        loadNewsMap();
        setupTable();
        loadComments();
    }

    private void loadNewsMap() {
        try {
            List<com.carthagegg.models.News> newsList = newsDAO.findAll();
            for (com.carthagegg.models.News n : newsList) {
                newsTitleById.put(n.getNewsId(), n.getTitle());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("commentaireId"));
        
        colNewsId.setCellValueFactory(cellData -> {
            int nid = cellData.getValue().getNewsId();
            String title = newsTitleById.get(nid);
            return new SimpleStringProperty(title != null ? title : "News #" + nid);
        });
        
        colUserId.setCellValueFactory(cellData -> {
            String uname = cellData.getValue().getUsername();
            if (uname != null) return new SimpleStringProperty(uname + " (#" + cellData.getValue().getUserId() + ")");
            return new SimpleStringProperty("#" + cellData.getValue().getUserId());
        });
        
        colContent.setCellValueFactory(cellData -> {
            String c = cellData.getValue().getContenu();
            if (cellData.getValue().getParentId() > 0) {
                return new SimpleStringProperty("[Reply to #" + cellData.getValue().getParentId() + "] " + c);
            }
            return new SimpleStringProperty(c);
        });

        colDate.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getDateCommentaire();
            if (dt == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        });

        colVotes.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getUpvotes() + " / " + cellData.getValue().getDownvotes()
        ));

        colActions.setCellFactory(param -> new TableCell<Comment, Void>() {
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, deleteBtn);

            {
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadComments() {
        try {
            List<Comment> list = commentDAO.findAll();
            commentsList.setAll(list);
            commentsTable.setItems(commentsList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Comment c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete comment ID " + c.getCommentaireId() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    commentDAO.delete(c.getCommentaireId());
                    commentsList.remove(c);
                } catch (SQLException e) {
                    showAlert("Error", "Could not delete comment: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml");
    }
}

