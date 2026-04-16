package com.carthagegg.controllers.back;

import com.carthagegg.dao.CategoryDAO;
import com.carthagegg.models.Category;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CategoriesManagementController {

    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colDescription;
    @FXML private TableColumn<Category, String> colCreatedAt;
    @FXML private TableColumn<Category, Void> colActions;

    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;
    @FXML private ComboBox<String> sortComboBox;

    private CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Category> categoriesList = FXCollections.observableArrayList();
    private Category selectedCategory;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadCategories();
        setupSort();
    }

    private void setupSort() {
        sortComboBox.setItems(FXCollections.observableArrayList(
            "ID ASC", "ID DESC", "Name ASC", "Name DESC"
        ));
        sortComboBox.setOnAction(e -> {
            String selected = sortComboBox.getValue();
            if (selected == null) return;

            switch (selected) {
                case "ID ASC" -> categoriesList.sort((c1, c2) -> Integer.compare(c1.getId(), c2.getId()));
                case "ID DESC" -> categoriesList.sort((c1, c2) -> Integer.compare(c2.getId(), c1.getId()));
                case "Name ASC" -> categoriesList.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
                case "Name DESC" -> categoriesList.sort((c1, c2) -> c2.getName().compareToIgnoreCase(c1.getName()));
            }
            categoriesTable.refresh();
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        colCreatedAt.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getCreatedAt();
            return new javafx.beans.property.SimpleStringProperty(
                date != null ? date.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : ""
            );
        });

        colActions.setCellFactory(param -> new TableCell<Category, Void>() {
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

    private void loadCategories() {
        try {
            List<Category> categories = categoryDAO.findAll();
            categoriesList.setAll(categories);
            categoriesTable.setItems(categoriesList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleShowAddForm() {
        selectedCategory = null;
        formTitle.setText("ADD CATEGORY");
        hideError();
        nameField.clear();
        descriptionArea.clear();
        showForm();
    }

    private void handleEdit(Category c) {
        selectedCategory = c;
        formTitle.setText("EDIT CATEGORY");
        hideError();
        nameField.setText(c.getName());
        descriptionArea.setText(c.getDescription());
        showForm();
    }

    private void handleDelete(Category c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete category: " + c.getName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    categoryDAO.delete(c.getId());
                    categoriesList.remove(c);
                } catch (SQLException e) {
                    showAlert("Error", "Could not delete category: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleSaveCategory() {
        String name = nameField.getText().trim();
        String desc = descriptionArea.getText().trim();

        if (name.isEmpty()) {
            showError("Category name is required.");
            return;
        }

        try {
            if (selectedCategory == null) {
                Category c = new Category();
                c.setName(name);
                c.setDescription(desc);
                categoryDAO.save(c);
                categoriesList.add(c);
            } else {
                selectedCategory.setName(name);
                selectedCategory.setDescription(desc);
                categoryDAO.update(selectedCategory);
                categoriesTable.refresh();
            }
            hideForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error: Could not save category.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
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
