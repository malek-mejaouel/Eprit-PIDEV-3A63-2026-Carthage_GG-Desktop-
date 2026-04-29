package com.carthagegg.controllers.back;

import com.carthagegg.dao.OrderDAO;
import com.carthagegg.dao.ProductDAO;
import com.carthagegg.dao.UserDAO;
import com.carthagegg.models.Order;
import com.carthagegg.models.Product;
import com.carthagegg.models.User;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersManagementController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String> colUser;
    @FXML private TableColumn<Order, String> colProduct;
    @FXML private TableColumn<Order, Integer> colQuantity;
    @FXML private TableColumn<Order, String> colTotal;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, Void> colActions;

    @FXML private ComboBox<Order.Status> statusFilter;

    private OrderDAO orderDAO = new OrderDAO();
    private UserDAO userDAO = new UserDAO();
    private ProductDAO productDAO = new ProductDAO();
    private ObservableList<Order> ordersList = FXCollections.observableArrayList();
    
    private Map<Integer, User> userCache = new HashMap<>();
    private Map<Integer, Product> productCache = new HashMap<>();

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        loadCaches();
        setupTable();
        loadOrders();
        statusFilter.setItems(FXCollections.observableArrayList(Order.Status.values()));
        setupStatusFilter();
    }

    private void loadCaches() {
        try {
            userDAO.findAll().forEach(u -> userCache.put(u.getUserId(), u));
            productDAO.findAll().forEach(p -> productCache.put(p.getId(), p));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getOrderDate() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(cellData.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        });

        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));

        colUser.setCellValueFactory(cellData -> {
            User u = userCache.get(cellData.getValue().getUserId());
            return new SimpleStringProperty(u != null ? u.getUsername() : "User #" + cellData.getValue().getUserId());
        });

        colProduct.setCellValueFactory(cellData -> {
            Product p = productCache.get(cellData.getValue().getProductId());
            return new SimpleStringProperty(p != null ? p.getName() : "Product #" + cellData.getValue().getProductId());
        });

        colTotal.setCellValueFactory(cellData -> {
            Product p = productCache.get(cellData.getValue().getProductId());
            if (p != null) {
                BigDecimal total = p.getPrice().multiply(new BigDecimal(cellData.getValue().getQuantity()));
                return new SimpleStringProperty(total.toString() + " TND");
            }
            return new SimpleStringProperty("0.00 TND");
        });

        colActions.setCellFactory(param -> new TableCell<Order, Void>() {
            private final ComboBox<Order.Status> statusBox = new ComboBox<>(FXCollections.observableArrayList(Order.Status.values()));
            {
                statusBox.getStyleClass().add("text-field-dark");
                statusBox.setPrefWidth(150);
                statusBox.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(order, statusBox.getValue());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    statusBox.setValue(getTableView().getItems().get(getIndex()).getStatus());
                    setGraphic(statusBox);
                }
            }
        });
    }

    private void loadOrders() {
        ordersList.setAll(orderDAO.findAll());
        ordersTable.setItems(ordersList);
    }

    private void setupStatusFilter() {
        statusFilter.setOnAction(e -> {
            Order.Status selected = statusFilter.getValue();
            if (selected == null) ordersTable.setItems(ordersList);
            else {
                ordersTable.setItems(ordersList.filtered(order -> order.getStatus() == selected));
            }
        });
    }

    private void handleUpdateStatus(Order order, Order.Status status) {
        orderDAO.updateStatus(order.getOrderId(), status);
        order.setStatus(status);
        ordersTable.refresh();
    }

    @FXML private void handleExportCSV() {
        // Implementation for CSV export
        System.out.println("Exporting orders to CSV...");
    }

    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/ProductsManagement.fxml"); }
}
