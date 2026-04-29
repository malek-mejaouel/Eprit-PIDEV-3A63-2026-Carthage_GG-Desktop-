package com.carthagegg.controllers.back;

import com.carthagegg.dao.CategoryDAO;
import com.carthagegg.dao.ProductDAO;
import com.carthagegg.models.Category;
import com.carthagegg.models.Product;
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
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductsManagementController {

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, BigDecimal> colPrice;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, Void> colActions;

    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField discountPriceField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField stockField;
    @FXML private TextField imageField;
    @FXML private Label errorLabel;
    @FXML private ComboBox<String> sortComboBox;

    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Product> productsList = FXCollections.observableArrayList();
    private Product selectedProduct;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadProducts();
        loadCategories();
        setupSort();
    }

    private void setupSort() {
        sortComboBox.setItems(FXCollections.observableArrayList(
            "ID ASC", "ID DESC", "Name ASC", "Name DESC", "Price ASC", "Price DESC"
        ));
        sortComboBox.setOnAction(e -> {
            String selected = sortComboBox.getValue();
            if (selected == null) return;

            switch (selected) {
                case "ID ASC" -> productsList.sort((p1, p2) -> Integer.compare(p1.getId(), p2.getId()));
                case "ID DESC" -> productsList.sort((p1, p2) -> Integer.compare(p2.getId(), p1.getId()));
                case "Name ASC" -> productsList.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
                case "Name DESC" -> productsList.sort((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
                case "Price ASC" -> productsList.sort((p1, p2) -> p1.getPrice().compareTo(p2.getPrice()));
                case "Price DESC" -> productsList.sort((p1, p2) -> p2.getPrice().compareTo(p1.getPrice()));
            }
            productsTable.refresh();
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        colPrice.setCellValueFactory(cellData -> {
            Product p = cellData.getValue();
            // Always return the effective price for display/sorting in this column
            return new javafx.beans.property.SimpleObjectProperty<>(p.getEffectivePrice());
        });

        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        
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

        colActions.setCellFactory(param -> new TableCell<Product, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button();
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-gold");
                
                FontIcon trashIcon = new FontIcon("fas-trash-alt");
                trashIcon.setIconColor(javafx.scene.paint.Color.web("#ef4444"));
                trashIcon.setIconSize(14);
                deleteBtn.setGraphic(trashIcon);
                deleteBtn.setTooltip(new Tooltip("Delete product"));
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                
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

    private void loadProducts() {
        try {
            productsList.setAll(productDAO.findAll());
            productsTable.setItems(productsList);
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
        selectedProduct = null;
        formTitle.setText("ADD PRODUCT");
        hideError();
        clearForm();
        showForm();
    }

    private void handleEdit(Product p) {
        selectedProduct = p;
        formTitle.setText("EDIT PRODUCT");
        hideError();
        nameField.setText(p.getName());
        priceField.setText(p.getPrice().toString());
        discountPriceField.setText(p.getDiscountPrice() != null ? p.getDiscountPrice().toString() : "");
        stockField.setText(String.valueOf(p.getStock()));
        imageField.setText(p.getImage());
        
        for (Category c : categoryComboBox.getItems()) {
            if (c.getId() == p.getCategoryId()) {
                categoryComboBox.setValue(c);
                break;
            }
        }
        showForm();
    }

    private void handleDelete(Product p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete product: " + p.getName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productDAO.delete(p.getId());
                    productsList.remove(p);
                } catch (SQLException e) {
                    showAlert("Error", "Could not delete product", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(nameField.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String savedPath = FileStorage.saveProductImage(selectedFile);
                imageField.setText(savedPath);
                hideError();
            } catch (IOException e) {
                e.printStackTrace();
                showError("Failed to upload image.");
            }
        }
    }

    @FXML
    private void handleSaveProduct() {
        if (!validateInput()) {
            return;
        }

        try {
            Product p = (selectedProduct == null) ? new Product() : selectedProduct;
            p.setName(nameField.getText().trim());
            p.setPrice(new BigDecimal(priceField.getText().trim()));
            
            String discountText = discountPriceField.getText().trim();
            if (discountText.isEmpty()) {
                p.setDiscountPrice(null);
            } else {
                p.setDiscountPrice(new BigDecimal(discountText));
            }

            p.setCategoryId(categoryComboBox.getValue().getId());
            p.setStock(Integer.parseInt(stockField.getText().trim()));
            p.setImage(imageField.getText().trim());

            if (selectedProduct == null) {
                productDAO.save(p);
                productsList.add(p);
            } else {
                productDAO.update(p);
                loadProducts(); 
            }
            hideForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error: Could not save product.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("An unexpected error occurred.");
        }
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage.append("• Product name is required.\n");
        }

        if (priceField.getText() == null || priceField.getText().trim().isEmpty()) {
            errorMessage.append("• Price is required.\n");
        } else {
            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("• Price must be greater than 0.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("• Price must be a valid number.\n");
            }
        }

        String discountText = discountPriceField.getText().trim();
        if (!discountText.isEmpty()) {
            try {
                BigDecimal discountPrice = new BigDecimal(discountText);
                BigDecimal originalPrice = new BigDecimal(priceField.getText().trim());
                if (discountPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("• Sale price must be greater than 0.\n");
                } else if (discountPrice.compareTo(originalPrice) >= 0) {
                    errorMessage.append("• Sale price must be less than the original price.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("• Sale price must be a valid number.\n");
            }
        }

        if (categoryComboBox.getValue() == null) {
            errorMessage.append("• Please select a category.\n");
        }

        if (stockField.getText() == null || stockField.getText().trim().isEmpty()) {
            errorMessage.append("• Stock quantity is required.\n");
        } else {
            try {
                int stock = Integer.parseInt(stockField.getText().trim());
                if (stock < 0) {
                    errorMessage.append("• Stock cannot be negative.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("• Stock must be a valid number.\n");
            }
        }

        if (errorMessage.length() > 0) {
            showError(errorMessage.toString().trim());
            return false;
        }

        hideError();
        return true;
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
    private void clearForm() { 
        nameField.clear(); 
        priceField.setText("0.00"); 
        discountPriceField.clear();
        categoryComboBox.setValue(null); 
        stockField.setText("0"); 
        imageField.clear(); 
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML private void handleNavOrders() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/OrdersManagement.fxml"); }
    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
