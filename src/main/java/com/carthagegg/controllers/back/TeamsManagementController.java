package com.carthagegg.controllers.back;

import com.carthagegg.dao.TeamDAO;
import com.carthagegg.dao.UserDAO;
import com.carthagegg.models.Team;
import com.carthagegg.models.User;
import com.carthagegg.utils.AIService;
import com.carthagegg.utils.FileStorage;
import com.carthagegg.utils.LogoGeneratorService;
import com.carthagegg.utils.TeamStatsService;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamsManagementController {

    @FXML private TableView<Team> teamsTable;
    @FXML private TableColumn<Team, Integer> colId;
    @FXML private TableColumn<Team, String> colName;
    @FXML private TableColumn<Team, String> colCaptain;
    @FXML private TableColumn<Team, LocalDate> colCreationDate;
    @FXML private TableColumn<Team, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private Label nameErrorLabel;
    @FXML private ComboBox<User> captainComboBox;
    @FXML private Label captainErrorLabel;
    @FXML private TextArea aiPromptArea;
    @FXML private TextField logoField;
    @FXML private Label logoErrorLabel;
    @FXML private ImageView logoPreview;

    private final TeamDAO teamDAO = new TeamDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AIService aiService = new AIService();
    private final LogoGeneratorService logoGeneratorService = new LogoGeneratorService();
    private final TeamStatsService teamStatsService = new TeamStatsService();
    private final ObservableList<Team> teamsList = FXCollections.observableArrayList();
    private final FilteredList<Team> filteredTeams = new FilteredList<>(teamsList, team -> true);
    private final SortedList<Team> sortedTeams = new SortedList<>(filteredTeams);
    private final Map<Integer, User> usersById = new HashMap<>();
    private Team selectedTeam;
    private File selectedLogoFile;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadCaptains();
        setupFilters();
        loadTeams();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("teamId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        colCreationDate.setCellValueFactory(new PropertyValueFactory<>("creationDate"));

        colCaptain.setCellValueFactory(cellData -> {
            User user = usersById.get(cellData.getValue().getUserId());
            return new SimpleStringProperty(user != null ? user.toString() : "Unknown");
        });

        colActions.setCellFactory(param -> new TableCell<Team, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button statsBtn = new Button("Fetch Stats");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, editBtn, deleteBtn, statsBtn);

            {
                editBtn.getStyleClass().add("btn-gold");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                statsBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");
                
                editBtn.setOnAction(e -> {
                    Team t = getTableView().getItems().get(getIndex());
                    if (t != null) handleEdit(t);
                });
                deleteBtn.setOnAction(e -> {
                    Team t = getTableView().getItems().get(getIndex());
                    if (t != null) handleDelete(t);
                });
                statsBtn.setOnAction(e -> {
                    Team t = getTableView().getItems().get(getIndex());
                    if (t != null) handleFetchStats(t);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupFilters() {
        sortComboBox.setItems(FXCollections.observableArrayList("ID Asc", "ID Desc"));
        sortComboBox.setValue("ID Asc");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applySort());

        applyFilters();
        applySort();
        teamsTable.setItems(sortedTeams);
    }

    private void loadTeams() {
        try {
            teamsList.setAll(teamDAO.findAll());
        } catch (SQLException e) {
            showAlert("Error", "Unable to load teams: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadCaptains() {
        try {
            List<User> users = userDAO.findAll();
            usersById.clear();
            for (User user : users) {
                usersById.put(user.getUserId(), user);
            }
            captainComboBox.setItems(FXCollections.observableArrayList(users));
        } catch (SQLException e) {
            showAlert("Error", "Unable to load captains: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filteredTeams.setPredicate(team -> {
            if (keyword.isEmpty()) {
                return true;
            }
            User captain = usersById.get(team.getUserId());
            return String.valueOf(team.getTeamId()).contains(keyword)
                    || safe(team.getTeamName()).contains(keyword)
                    || safe(team.getLogo()).contains(keyword)
                    || safe(team.getCreationDate() == null ? "" : team.getCreationDate().toString()).contains(keyword)
                    || safe(captain == null ? "" : captain.toString()).contains(keyword);
        });
    }

    private void applySort() {
        Comparator<Team> comparator = Comparator.comparingInt(Team::getTeamId);
        if ("ID Desc".equals(sortComboBox.getValue())) {
            comparator = comparator.reversed();
        }
        sortedTeams.setComparator(comparator);
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
        selectedLogoFile = null;
        loadLogoPreview(t.getLogo());
        
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
        if (!validateForm()) {
            return;
        }

        try {
            String name = nameField.getText().trim();
            User captain = captainComboBox.getValue();
            String logo = selectedTeam != null ? selectedTeam.getLogo() : "";

            if (selectedLogoFile != null) {
                logo = FileStorage.saveTeamLogo(selectedLogoFile);
            } else if (logoField.getText() != null && !logoField.getText().trim().isEmpty()) {
                logo = logoField.getText().trim();
            }

            Team t = (selectedTeam == null) ? new Team() : selectedTeam;
            t.setTeamName(name);
            t.setUserId(captain.getUserId());
            t.setLogo(logo);
            t.setCreationDate(selectedTeam == null ? LocalDate.now() : selectedTeam.getCreationDate());
            
            if (selectedTeam == null) {
                teamDAO.save(t);
            } else {
                teamDAO.update(t);
            }
            loadTeams();
            hideForm();
        } catch (IOException e) {
            showAlert("Error", "Could not save team logo: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGenerateLogo() {
        String teamName = nameField.getText();
        String customPrompt = aiPromptArea.getText();
        if (teamName == null || teamName.trim().isEmpty()) {
            showAlert("Input Required", "Please enter a team name first.", Alert.AlertType.WARNING);
            return;
        }

        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("AI Logo Generation");
        loadingAlert.setHeaderText("Generating Logo for " + teamName + "...");
        loadingAlert.setContentText("This may take a few seconds. Please wait...");
        loadingAlert.show();

        logoGeneratorService.generateLogoAsync(teamName, customPrompt)
            .thenAccept(imageData -> Platform.runLater(() -> {
                try {
                    loadingAlert.close();
                    java.nio.file.Path tempPath = logoGeneratorService.saveLogoLocally(imageData, teamName);
                    selectedLogoFile = tempPath.toFile();
                    logoField.setText(selectedLogoFile.getAbsolutePath());
                    logoPreview.setImage(new Image(new ByteArrayInputStream(imageData)));
                    showAlert("Success", "AI Logo generated successfully!", Alert.AlertType.INFORMATION);
                } catch (IOException e) {
                    showAlert("Error", "Failed to save generated image: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    loadingAlert.close();
                    showAlert("AI Error", "Failed to generate logo: " + ex.getMessage(), Alert.AlertType.ERROR);
                });
                return null;
            });
    }

    @FXML
    private void handleBrowseLogo() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Team Logo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );
        File file = chooser.showOpenDialog(nameField.getScene() != null ? nameField.getScene().getWindow() : null);
        if (file != null) {
            selectedLogoFile = file;
            logoField.setText(file.getAbsolutePath());
            loadLogoPreview(file.toURI().toString());
            logoErrorLabel.setText("");
        }
    }

    private boolean validateForm() {
        clearErrors();
        boolean valid = true;

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText("Team name is required.");
            valid = false;
        }
        if (captainComboBox.getValue() == null) {
            captainErrorLabel.setText("Captain is required.");
            valid = false;
        }
        if ((logoField.getText() == null || logoField.getText().trim().isEmpty()) && selectedLogoFile == null) {
            logoErrorLabel.setText("Team logo image is required.");
            valid = false;
        }

        return valid;
    }

    private void showForm() {
        clearErrors();
        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    @FXML private void handleHideForm() { hideForm(); }

    private void hideForm() {
        clearForm();
        clearErrors();
        formPane.setVisible(false);
        formPane.setManaged(false);
    }

    private void clearForm() {
        nameField.clear();
        captainComboBox.setValue(null);
        aiPromptArea.clear();
        logoField.clear();
        selectedLogoFile = null;
        logoPreview.setImage(null);
    }

    private void handleFetchStats(Team t) {
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Fetching Statistics");
        loadingAlert.setHeaderText("Retrieving stats for " + t.getTeamName() + "...");
        loadingAlert.setContentText("Please wait...");
        loadingAlert.show();

        new Thread(() -> {
            try {
                String summary = teamStatsService.getTeamSummary(t.getTeamName());
                Platform.runLater(() -> {
                    loadingAlert.close();
                    showStatsDialog(t.getTeamName(), summary);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    loadingAlert.close();
                    showAlert("API Error", "Could not fetch stats: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    private void showStatsDialog(String teamName, String summary) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📊 Team Statistics");
        alert.setHeaderText("Statistics for: " + teamName);
        
        TextArea textArea = new TextArea(summary);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(200);
        textArea.setPrefWidth(300);
        textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px;");

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void clearErrors() {
        nameErrorLabel.setText("");
        captainErrorLabel.setText("");
        logoErrorLabel.setText("");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private void loadLogoPreview(String path) {
        if (path == null || path.isBlank()) {
            logoPreview.setImage(null);
            return;
        }

        try {
            String imageUrl = path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file:")
                    ? path
                    : java.nio.file.Path.of(path).toUri().toString();
            logoPreview.setImage(new Image(imageUrl, true));
        } catch (Exception e) {
            logoPreview.setImage(null);
        }
    }
    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
