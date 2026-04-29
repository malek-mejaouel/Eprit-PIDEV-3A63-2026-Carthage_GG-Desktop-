package com.carthagegg.controllers.back;

import com.carthagegg.dao.StreamDAO;
import com.carthagegg.models.Stream;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import com.carthagegg.utils.TwitchService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class StreamsManagementController {

    @FXML private TableView<Stream> streamsTable;
    @FXML private TableColumn<Stream, Integer> colId;
    @FXML private TableColumn<Stream, String> colTitle;
    @FXML private TableColumn<Stream, String> colPlatform;
    @FXML private TableColumn<Stream, String> colChannel;
    @FXML private TableColumn<Stream, Boolean> colLive;
    @FXML private TableColumn<Stream, Integer> colViewers;
    @FXML private TableColumn<Stream, Void> colActions;

    @FXML private VBox formPane;
    @FXML private Label formTitle;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> platformComboBox;
    @FXML private TextField channelField;
    @FXML private TextField ytIdField;
    @FXML private TextField twitchLinkField;
    @FXML private CheckBox liveCheckBox;
    @FXML private TextField viewersField;
    @FXML private Label errorLabel;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button fetchTwitchBtn;

    private StreamDAO streamDAO = new StreamDAO();
    private ObservableList<Stream> streamsList = FXCollections.observableArrayList();
    private Stream selectedStream;
    private String lastFetchedThumbnail = null;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadStreams();
        platformComboBox.setItems(FXCollections.observableArrayList("twitch", "youtube"));
        setupSort();
    }

    private void setupSort() {
        sortComboBox.setItems(FXCollections.observableArrayList(
            "ID ASC", "ID DESC", "Title ASC", "Title DESC", "Viewers ASC", "Viewers DESC"
        ));
        sortComboBox.setOnAction(e -> {
            String selected = sortComboBox.getValue();
            if (selected == null) return;

            switch (selected) {
                case "ID ASC" -> streamsList.sort((s1, s2) -> Integer.compare(s1.getStreamId(), s2.getStreamId()));
                case "ID DESC" -> streamsList.sort((s1, s2) -> Integer.compare(s2.getStreamId(), s1.getStreamId()));
                case "Title ASC" -> streamsList.sort((s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle()));
                case "Title DESC" -> streamsList.sort((s1, s2) -> s2.getTitle().compareToIgnoreCase(s1.getTitle()));
                case "Viewers ASC" -> streamsList.sort((s1, s2) -> Integer.compare(s1.getViewerCount(), s2.getViewerCount()));
                case "Viewers DESC" -> streamsList.sort((s1, s2) -> Integer.compare(s2.getViewerCount(), s1.getViewerCount()));
            }
            streamsTable.refresh();
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("streamId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPlatform.setCellValueFactory(new PropertyValueFactory<>("platform"));
        colChannel.setCellValueFactory(new PropertyValueFactory<>("channelName"));
        colLive.setCellValueFactory(new PropertyValueFactory<>("live"));
        colViewers.setCellValueFactory(new PropertyValueFactory<>("viewerCount"));

        colActions.setCellFactory(param -> new TableCell<Stream, Void>() {
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

    private void loadStreams() {
        try {
            streamsList.setAll(streamDAO.findAll());
            streamsTable.setItems(streamsList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleShowAddForm() {
        selectedStream = null;
        formTitle.setText("ADD STREAM");
        hideError();
        clearForm();
        showForm();
    }

    @FXML
    private void handleFetchTwitchInfo() {
        String link = twitchLinkField.getText().trim();
        String channelName = TwitchService.extractChannelName(link);

        if (channelName == null) {
            showError("Invalid Twitch link or username. Format: twitch.tv/channel or username");
            return;
        }

        fetchTwitchBtn.setDisable(true);
        fetchTwitchBtn.setText("Fetching...");
        hideError();

        TwitchService.getStreamInfo(channelName).thenAccept(data -> {
            Platform.runLater(() -> {
                fetchTwitchBtn.setDisable(false);
                fetchTwitchBtn.setText("FETCH");
                if (data != null) {
                    titleField.setText(data.title);
                    descriptionField.setText(data.description != null ? data.description : "");
                    channelField.setText(data.channelName);
                    platformComboBox.setValue("twitch");
                    liveCheckBox.setSelected(data.isLive);
                    viewersField.setText(String.valueOf(data.viewerCount));
                    lastFetchedThumbnail = data.thumbnail;
                } else {
                    showError("Could not find Twitch channel: " + channelName);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                fetchTwitchBtn.setDisable(false);
                fetchTwitchBtn.setText("FETCH");
                showError("Error: " + ex.getMessage());
            });
            return null;
        });
    }

    private void handleEdit(Stream s) {
        selectedStream = s;
        formTitle.setText("EDIT STREAM");
        hideError();
        titleField.setText(s.getTitle());
        descriptionField.setText(s.getDescription());
        platformComboBox.setValue(s.getPlatform());
        channelField.setText(s.getChannelName());
        ytIdField.setText(s.getYoutubeVideoId());
        liveCheckBox.setSelected(s.isLive());
        viewersField.setText(String.valueOf(s.getViewerCount()));
        showForm();
    }

    private void handleDelete(Stream s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete stream: " + s.getTitle() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    streamDAO.delete(s.getStreamId());
                    streamsList.remove(s);
                } catch (SQLException e) { showAlert("Error", "Could not delete stream", Alert.AlertType.ERROR); }
            }
        });
    }

    @FXML
    private void handleSaveStream() {
        if (!validateInput()) {
            return;
        }

        try {
            Stream s = (selectedStream == null) ? new Stream() : selectedStream;
            s.setTitle(titleField.getText().trim());
            s.setDescription(descriptionField.getText().trim());
            s.setPlatform(platformComboBox.getValue());
            s.setChannelName(channelField.getText().trim());
            s.setYoutubeVideoId(ytIdField.getText().trim());
            s.setLive(liveCheckBox.isSelected());
            s.setViewerCount(Integer.parseInt(viewersField.getText().trim()));
            s.setCreatedBy(SessionManager.getCurrentUser().getUserId());
            
            if (lastFetchedThumbnail != null) {
                s.setThumbnail(lastFetchedThumbnail);
            }

            if (selectedStream == null) {
                streamDAO.save(s);
                streamsList.add(s);
            } else {
                streamDAO.update(s);
                streamsTable.refresh();
            }
            hideForm();
        } catch (Exception e) {
            e.printStackTrace();
            showError("An unexpected error occurred.");
        }
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessage.append("• Stream title is required.\n");
        }

        if (platformComboBox.getValue() == null) {
            errorMessage.append("• Please select a platform.\n");
        }

        if (channelField.getText() == null || channelField.getText().trim().isEmpty()) {
            errorMessage.append("• Channel name is required.\n");
        }

        if (viewersField.getText() == null || viewersField.getText().trim().isEmpty()) {
            errorMessage.append("• Viewer count is required.\n");
        } else {
            try {
                int viewers = Integer.parseInt(viewersField.getText().trim());
                if (viewers < 0) {
                    errorMessage.append("• Viewers cannot be negative.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("• Viewers must be a valid number.\n");
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
        titleField.clear();
        descriptionField.clear();
        platformComboBox.setValue(null);
        channelField.clear();
        ytIdField.clear();
        twitchLinkField.clear();
        liveCheckBox.setSelected(false);
        viewersField.setText("0");
        lastFetchedThumbnail = null;
        hideError();
    }
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
