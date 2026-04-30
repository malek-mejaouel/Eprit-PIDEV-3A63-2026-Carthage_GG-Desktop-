package com.carthagegg.controllers.back;

import com.carthagegg.dao.CouponDAO;
import com.carthagegg.models.Coupon;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.util.Optional;

public class CouponsManagementController {

    @FXML private TableView<Coupon> couponsTable;
    @FXML private TableColumn<Coupon, String> colCode;
    @FXML private TableColumn<Coupon, Double> colDiscount;
    @FXML private TableColumn<Coupon, LocalDate> colExpiry;
    @FXML private TableColumn<Coupon, Boolean> colStatus;
    @FXML private TableColumn<Coupon, Void> colActions;

    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField codeField;
    @FXML private TextField discountField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private Label errorLabel;

    private CouponDAO couponDAO = new CouponDAO();
    private ObservableList<Coupon> couponsList = FXCollections.observableArrayList();
    private Coupon selectedCoupon;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadCoupons();
    }

    private void setupTable() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        colDiscount.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + "%");
                    setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
                }
            }
        });

        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Coupon coupon = getTableView().getItems().get(getIndex());
                    boolean valid = coupon.isValid();
                    Label badge = new Label(valid ? "ACTIVE" : "EXPIRED");
                    badge.getStyleClass().addAll("badge", valid ? "badge-success" : "badge-danger");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox pane = new HBox(12, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER_LEFT);
                editBtn.setGraphic(new FontIcon("fas-edit"));
                editBtn.getStyleClass().addAll("btn-icon", "btn-icon-gold");
                editBtn.setStyle("-fx-font-size: 18px;");
                Tooltip.install(editBtn, new Tooltip("Edit Coupon"));

                deleteBtn.setGraphic(new FontIcon("fas-trash"));
                deleteBtn.getStyleClass().addAll("btn-icon", "btn-icon-danger");
                deleteBtn.setStyle("-fx-font-size: 18px;");
                Tooltip.install(deleteBtn, new Tooltip("Delete Coupon"));

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

    private void loadCoupons() {
        couponsList.setAll(couponDAO.findAll());
        couponsTable.setItems(couponsList);
    }

    @FXML
    private void handleShowAddForm() {
        selectedCoupon = null;
        formTitle.setText("ADD COUPON");
        clearForm();
        hideError();
        showForm();
    }

    private void handleEdit(Coupon c) {
        selectedCoupon = c;
        formTitle.setText("EDIT COUPON");
        codeField.setText(c.getCode());
        codeField.setEditable(false); // Code shouldn't be changed during edit
        discountField.setText(String.valueOf(c.getDiscountPercentage()));
        expiryDatePicker.setValue(c.getExpiryDate());
        hideError();
        showForm();
    }

    private void handleDelete(Coupon c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete coupon: " + c.getCode() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                couponsList.remove(c);
                couponDAO.saveAll(couponsList);
            }
        });
    }

    @FXML
    private void handleSaveCoupon() {
        String code = codeField.getText().trim();
        String discountStr = discountField.getText().trim();
        LocalDate expiry = expiryDatePicker.getValue();

        if (code.isEmpty() || discountStr.isEmpty() || expiry == null) {
            showError("All fields are required.");
            return;
        }

        try {
            double discount = Double.parseDouble(discountStr);
            if (discount <= 0 || discount > 100) {
                showError("Discount must be between 1 and 100.");
                return;
            }

            if (selectedCoupon == null) {
                // Check if code already exists
                if (couponDAO.findByCode(code).isPresent()) {
                    showError("Coupon code already exists.");
                    return;
                }
                Coupon newCoupon = new Coupon(code, discount, expiry);
                couponDAO.save(newCoupon);
            } else {
                selectedCoupon.setDiscountPercentage(discount);
                selectedCoupon.setExpiryDate(expiry);
                couponDAO.save(selectedCoupon);
            }

            loadCoupons();
            handleHideForm();
        } catch (NumberFormatException e) {
            showError("Invalid discount value.");
        }
    }

    @FXML private void handleHideForm() {
        formPane.setVisible(false);
        formPane.setManaged(false);
    }

    private void showForm() {
        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    private void clearForm() {
        codeField.clear();
        codeField.setEditable(true);
        discountField.clear();
        expiryDatePicker.setValue(null);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML private void handleBack() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/back/ProductsManagement.fxml");
    }
}
