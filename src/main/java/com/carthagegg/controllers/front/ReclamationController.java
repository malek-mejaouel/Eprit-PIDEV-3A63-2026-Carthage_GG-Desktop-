package com.carthagegg.controllers.front;

import com.carthagegg.dao.ReclamationDAO;
import com.carthagegg.models.Reclamation;
import com.carthagegg.models.User;
import com.carthagegg.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReclamationController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;
    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, String> colDate;
    @FXML private TableColumn<Reclamation, String> colTitle;
    @FXML private TableColumn<Reclamation, String> colStatus;
    @FXML private TableColumn<Reclamation, Void> colActions;
    @FXML private SidebarController sidebarController;

    private ReclamationDAO reclamationDAO = new ReclamationDAO();
    private ObservableList<Reclamation> reclamationList = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("reclamation");
        }
        setupTable();
        loadReclamations();
    }

    private void setupTable() {
        colDate.setCellValueFactory(cellData -> {
            Reclamation r = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(r.getCreatedAt().format(formatter));
        });
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        
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
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, deleteBtn);

            {
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10;");
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
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            try {
                List<Reclamation> list = reclamationDAO.findByUserId(user.getUserId());
                reclamationList.setAll(list);
                reclamationTable.setItems(reclamationList);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSubmit() {
        clearError();
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        User user = SessionManager.getCurrentUser();

        if (title.isEmpty() || description.isEmpty()) {
            showError("Please fill in both title and description");
            return;
        }

        if (user != null) {
            try {
                Reclamation r = new Reclamation();
                r.setUserId(user.getUserId());
                r.setTitle(title);
                r.setDescription(description);
                
                reclamationDAO.save(r);
                
                titleField.clear();
                descriptionArea.clear();
                loadReclamations();
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Reclamation submitted successfully!");
                success.show();
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleDelete(Reclamation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete this reclamation?");
        alert.setContentText("This action cannot be undone.");
        
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

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
