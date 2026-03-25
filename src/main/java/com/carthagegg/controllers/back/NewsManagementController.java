package com.carthagegg.controllers.back;

import com.carthagegg.dao.CategoryDAO;
import com.carthagegg.dao.NewsDAO;
import com.carthagegg.models.Category;
import com.carthagegg.models.News;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NewsManagementController {

    @FXML private TableView<News> newsTable;
    @FXML private TableColumn<News, Integer> colId;
    @FXML private TableColumn<News, String> colTitle;
    @FXML private TableColumn<News, String> colCategory;
    @FXML private TableColumn<News, String> colPublishedAt;
    @FXML private TableColumn<News, Void> colActions;

    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField titleField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextArea contentArea;

    private NewsDAO newsDAO = new NewsDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<News> newsList = FXCollections.observableArrayList();
    private News selectedNews;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadNews();
        loadCategories();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("newsId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        colPublishedAt.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPublishedAt() == null) return new SimpleStringProperty("Not Set");
            return new SimpleStringProperty(cellData.getValue().getPublishedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        });

        colCategory.setCellValueFactory(cellData -> {
            try {
                List<Category> cats = categoryDAO.findAll();
                Category c = cats.stream().filter(cat -> cat.getId() == cellData.getValue().getCategoryId())
                        .findFirst().orElse(null);
                return new SimpleStringProperty(c != null ? c.getName() : "Unknown");
            } catch (SQLException e) {
                return new SimpleStringProperty("Error");
            }
        });

        colActions.setCellFactory(param -> new TableCell<News, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-gold");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadNews() {
        try {
            newsList.setAll(newsDAO.findAll());
            newsTable.setItems(newsList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCategories() {
        try {
            categoryComboBox.setItems(FXCollections.observableArrayList(categoryDAO.findAll()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleShowAddForm() {
        selectedNews = null;
        formTitle.setText("ADD ARTICLE");
        titleField.clear();
        categoryComboBox.setValue(null);
        contentArea.clear();
        showForm();
    }

    private void handleEdit(News n) {
        selectedNews = n;
        formTitle.setText("EDIT ARTICLE");
        titleField.setText(n.getTitle());
        contentArea.setText(n.getContent());
        
        for (Category c : categoryComboBox.getItems()) {
            if (c.getId() == n.getCategoryId()) {
                categoryComboBox.setValue(c);
                break;
            }
        }
        showForm();
    }

    private void handleDelete(News n) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete news: " + n.getTitle() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    newsDAO.delete(n.getNewsId());
                    newsList.remove(n);
                } catch (SQLException e) {
                    showAlert("Error", "Could not delete article: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleSaveNews() {
        String title = titleField.getText();
        Category cat = categoryComboBox.getValue();
        String content = contentArea.getText();

        if (title.isEmpty() || cat == null || content.isEmpty()) {
            showAlert("Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (selectedNews == null) {
                News n = new News();
                n.setTitle(title);
                n.setCategoryId(cat.getId());
                n.setContent(content);
                newsDAO.save(n);
                newsList.add(n);
            } else {
                selectedNews.setTitle(title);
                selectedNews.setCategoryId(cat.getId());
                selectedNews.setContent(content);
                newsDAO.update(selectedNews);
                newsTable.refresh();
            }
            hideForm();
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showForm() { formPane.setVisible(true); formPane.setManaged(true); }
    @FXML private void handleHideForm() { hideForm(); }
    private void hideForm() { formPane.setVisible(false); formPane.setManaged(false); }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
