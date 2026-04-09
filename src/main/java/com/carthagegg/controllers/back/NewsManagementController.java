package com.carthagegg.controllers.back;

import com.carthagegg.dao.CategoryDAO;
import com.carthagegg.dao.NewsDAO;
import com.carthagegg.models.Category;
import com.carthagegg.models.News;
import com.carthagegg.utils.FileStorage;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
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
    @FXML private TextField imageField;
    @FXML private TextArea contentArea;

    private NewsDAO newsDAO = new NewsDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<News> newsList = FXCollections.observableArrayList();
    private News selectedNews;
    private File selectedImageFile;

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
            String name = cellData.getValue().getCategory();
            return new SimpleStringProperty(name != null ? name : "Unknown");
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
            List<News> list = newsDAO.findAll();
            newsList.setAll(list);
            newsTable.setItems(newsList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCategories() {
        try {
            List<Category> cats = categoryDAO.findAll();
            categoryComboBox.setItems(FXCollections.observableArrayList(cats));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowAddForm() {
        selectedNews = null;
        selectedImageFile = null;
        formTitle.setText("ADD ARTICLE");
        titleField.clear();
        categoryComboBox.setValue(null);
        imageField.clear();
        contentArea.clear();
        showForm();
    }

    private void handleEdit(News n) {
        selectedNews = n;
        selectedImageFile = null;
        formTitle.setText("EDIT ARTICLE");
        titleField.setText(n.getTitle());
        imageField.setText(n.getImage());
        contentArea.setText(n.getContent());
        
        for (Category c : categoryComboBox.getItems()) {
            if (c.getName() != null && c.getName().equalsIgnoreCase(n.getCategory())) {
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
        String image = imageField.getText();
        String content = contentArea.getText();

        if (title.isEmpty() || cat == null || content.isEmpty()) {
            showAlert("Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (selectedImageFile != null) {
                try {
                    image = FileStorage.saveNewsImage(selectedImageFile);
                    imageField.setText(image);
                } catch (IOException ex) {
                    showAlert("Error", "Could not save image file: " + ex.getMessage(), Alert.AlertType.ERROR);
                    return;
                }
            }
            if (selectedNews == null) {
                News n = new News();
                n.setTitle(title);
                n.setCategory(cat.getName());
                n.setImage(image);
                n.setContent(content);
                newsDAO.save(n);
                newsList.add(n);
            } else {
                selectedNews.setTitle(title);
                selectedNews.setCategory(cat.getName());
                selectedNews.setImage(image);
                selectedNews.setContent(content);
                newsDAO.update(selectedNews);
                newsTable.refresh();
            }
            hideForm();
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose News Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = chooser.showOpenDialog(titleField.getScene() != null ? titleField.getScene().getWindow() : null);
        if (file != null) {
            selectedImageFile = file;
            imageField.setText(file.getAbsolutePath());
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
