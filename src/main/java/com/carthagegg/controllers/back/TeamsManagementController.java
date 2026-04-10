package com.carthagegg.controllers.back;

import com.carthagegg.dao.TeamDAO;
import com.carthagegg.dao.UserDAO;
import com.carthagegg.models.Team;
import com.carthagegg.models.User;
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
import java.time.LocalDate;
import java.util.List;

public class TeamsManagementController {

    @FXML private TableView<Team> teamsTable;
    @FXML private TableColumn<Team, Integer> colId;
    @FXML private TableColumn<Team, String> colName;
    @FXML private TableColumn<Team, String> colCaptain;
    @FXML private TableColumn<Team, LocalDate> colCreationDate;
    @FXML private TableColumn<Team, Void> colActions;

    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private ComboBox<User> captainComboBox;
    @FXML private TextField logoField;

    private TeamDAO teamDAO = new TeamDAO();
    private UserDAO userDAO = new UserDAO();
    private ObservableList<Team> teamsList = FXCollections.observableArrayList();
    private Team selectedTeam;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadTeams();
        loadCaptains();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("teamId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        colCreationDate.setCellValueFactory(new PropertyValueFactory<>("creationDate"));

        colCaptain.setCellValueFactory(cellData -> {
            try {
                User u = userDAO.findAll().stream()
                        .filter(user -> user.getUserId() == cellData.getValue().getUserId())
                        .findFirst().orElse(null);
                return new SimpleStringProperty(u != null ? u.getUsername() : "Unknown");
            } catch (SQLException e) {
                return new SimpleStringProperty("Error");
            }
        });

        colActions.setCellFactory(param -> new TableCell<Team, Void>() {
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

    private void loadTeams() {
        try {
            teamsList.setAll(teamDAO.findAll());
            teamsTable.setItems(teamsList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCaptains() {
        try {
            captainComboBox.setItems(FXCollections.observableArrayList(userDAO.findAll()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleShowAddForm() {
        selectedTeam = null;
        formTitle.setText("ADD TEAM");
        clearForm();
        showForm();
    }

    private void handleEdit(Team t) {
        selectedTeam = t;
        formTitle.setText("EDIT TEAM");
        nameField.setText(t.getTeamName());
        logoField.setText(t.getLogo());
        
        for (User u : captainComboBox.getItems()) {
            if (u.getUserId() == t.getUserId()) {
                captainComboBox.setValue(u);
                break;
            }
        }
        showForm();
    }

    private void handleDelete(Team t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete team: " + t.getTeamName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    teamDAO.delete(t.getTeamId());
                    teamsList.remove(t);
                } catch (SQLException e) {
                    showAlert("Error", "Could not delete team: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleSaveTeam() {
        String name = nameField.getText();
        User captain = captainComboBox.getValue();
        String logo = logoField.getText();

        if (name.isEmpty() || captain == null) {
            showAlert("Error", "Name and Captain are required!", Alert.AlertType.ERROR);
            return;
        }

        try {
            Team t = (selectedTeam == null) ? new Team() : selectedTeam;
            t.setTeamName(name);
            t.setUserId(captain.getUserId());
            t.setLogo(logo);
            
            if (selectedTeam == null) {
                t.setCreationDate(LocalDate.now());
                teamDAO.save(t);
                teamsList.add(t);
            } else {
                teamDAO.update(t);
                teamsTable.refresh();
            }
            hideForm();
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showForm() { formPane.setVisible(true); formPane.setManaged(true); }
    @FXML private void handleHideForm() { hideForm(); }
    private void hideForm() { formPane.setVisible(false); formPane.setManaged(false); }
    private void clearForm() { nameField.clear(); captainComboBox.setValue(null); logoField.clear(); }
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
