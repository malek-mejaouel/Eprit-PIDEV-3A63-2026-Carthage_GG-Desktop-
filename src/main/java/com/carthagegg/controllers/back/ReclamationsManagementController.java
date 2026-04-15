package com.carthagegg.controllers.back;

import com.carthagegg.dao.ReclamationDAO;
import com.carthagegg.models.Reclamation;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;

public class ReclamationsManagementController {

    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colUsername;
    @FXML private TableColumn<Reclamation, String> colTitle;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatus;
    @FXML private TableColumn<Reclamation, Void> colActions;
    @FXML private ComboBox<String> statusFilter;

    private ReclamationDAO reclamationDAO = new ReclamationDAO();
    private ObservableList<Reclamation> reclamationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupFilters();
        setupTable();
        loadReclamations();
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "PENDING", "IN_PROGRESS", "RESOLVED"));
        statusFilter.setValue("All");
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadReclamations());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    switch (item.toLowerCase()) {
                        case "pending": setStyle("-fx-text-fill: #fbbf24;"); break;
                        case "in_progress": setStyle("-fx-text-fill: #00f0ff;"); break;
                        case "resolved": setStyle("-fx-text-fill: #10b981;"); break;
                        default: setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<Reclamation, Void>() {
            private final Button progressBtn = new Button("Progress");
            private final Button resolveBtn = new Button("Resolve");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, progressBtn, resolveBtn, deleteBtn);

            {
                progressBtn.setStyle("-fx-background-color: #00f0ff; -fx-text-fill: black; -fx-font-size: 10;");
                resolveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 10;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10;");

                progressBtn.setOnAction(event -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(r, "in_progress");
                });

                resolveBtn.setOnAction(event -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(r, "resolved");
                });

                deleteBtn.setOnAction(event -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    handleDelete(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadReclamations() {
        try {
            List<Reclamation> all = reclamationDAO.findAll();
            String filter = statusFilter.getValue();
            
            if (filter != null && !filter.equals("All")) {
                all = all.stream()
                        .filter(r -> r.getStatus().equalsIgnoreCase(filter))
                        .toList();
            }
            
            reclamationList.setAll(all);
            reclamationTable.setItems(reclamationList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleUpdateStatus(Reclamation r, String status) {
        try {
            reclamationDAO.updateStatus(r.getId(), status);
            loadReclamations();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Reclamation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete this reclamation?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reclamationDAO.delete(r.getId());
                    loadReclamations();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
