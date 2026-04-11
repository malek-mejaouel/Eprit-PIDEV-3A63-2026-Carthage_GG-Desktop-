package com.carthagegg.controllers.back;

import com.carthagegg.dao.StreamDAO;
import com.carthagegg.models.Stream;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
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
    @FXML private ComboBox<String> platformComboBox;
    @FXML private TextField channelField;
    @FXML private TextField ytIdField;
    @FXML private CheckBox liveCheckBox;
    @FXML private TextField viewersField;

    private StreamDAO streamDAO = new StreamDAO();
    private ObservableList<Stream> streamsList = FXCollections.observableArrayList();
    private Stream selectedStream;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
            return;
        }

        setupTable();
        loadStreams();
        platformComboBox.setItems(FXCollections.observableArrayList("twitch", "youtube"));
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
        clearForm();
        showForm();
    }

    private void handleEdit(Stream s) {
        selectedStream = s;
        formTitle.setText("EDIT STREAM");
        titleField.setText(s.getTitle());
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
        try {
            Stream s = (selectedStream == null) ? new Stream() : selectedStream;
            s.setTitle(titleField.getText());
            s.setPlatform(platformComboBox.getValue());
            s.setChannelName(channelField.getText());
            s.setYoutubeVideoId(ytIdField.getText());
            s.setLive(liveCheckBox.isSelected());
            s.setViewerCount(Integer.parseInt(viewersField.getText()));
            s.setCreatedBy(SessionManager.getCurrentUser().getUserId());

            if (selectedStream == null) {
                streamDAO.save(s);
                streamsList.add(s);
            } else {
                streamDAO.update(s);
                streamsTable.refresh();
            }
            hideForm();
        } catch (Exception e) { showAlert("Error", "Check all fields: " + e.getMessage(), Alert.AlertType.ERROR); }
    }

    private void showForm() { formPane.setVisible(true); formPane.setManaged(true); }
    @FXML private void handleHideForm() { hideForm(); }
    private void hideForm() { formPane.setVisible(false); formPane.setManaged(false); }
    private void clearForm() {
        titleField.clear(); platformComboBox.setValue(null); channelField.clear();
        ytIdField.clear(); liveCheckBox.setSelected(false); viewersField.setText("0");
    }
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML private void handleBack() { SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml"); }
}
