package com.carthagegg.controllers.front;

import com.carthagegg.models.LeaderboardEntry;
import com.carthagegg.utils.LeaderboardService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;
import java.util.List;

public class LeaderboardController {

    @FXML private TableView<LeaderboardEntry> leaderboardTable;
    @FXML private TableColumn<LeaderboardEntry, Integer> colRank;
    @FXML private TableColumn<LeaderboardEntry, String> colTeam;
    @FXML private TableColumn<LeaderboardEntry, Integer> colWins;
    @FXML private TableColumn<LeaderboardEntry, Integer> colLosses;
    @FXML private TableColumn<LeaderboardEntry, Integer> colDraws;
    @FXML private TableColumn<LeaderboardEntry, Double> colWinRate;
    @FXML private TableColumn<LeaderboardEntry, Integer> colPoints;

    private final LeaderboardService leaderboardService = new LeaderboardService();
    private final ObservableList<LeaderboardEntry> leaderboardData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadLeaderboard();
    }

    private void setupTable() {
        // Rank column with medals and gold color for top 3
        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colRank.setCellFactory(column -> new TableCell<LeaderboardEntry, Integer>() {
            @Override
            protected void updateItem(Integer rank, boolean empty) {
                super.updateItem(rank, empty);
                if (empty || rank == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String rankText = rank.toString();
                    Label label = new Label();
                    
                    if (rank == 1) {
                        label.setText("🥇 1");
                        label.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
                    } else if (rank == 2) {
                        label.setText("🥈 2");
                        label.setStyle("-fx-text-fill: #C0C0C0; -fx-font-weight: bold;");
                    } else if (rank == 3) {
                        label.setText("🥉 3");
                        label.setStyle("-fx-text-fill: #CD7F32; -fx-font-weight: bold;");
                    } else {
                        label.setText(rankText);
                        label.setStyle("-fx-text-fill: #949499;");
                    }
                    setGraphic(label);
                }
            }
        });

        // Team column with bold text
        colTeam.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        colTeam.setCellFactory(column -> new TableCell<LeaderboardEntry, String>() {
            @Override
            protected void updateItem(String team, boolean empty) {
                super.updateItem(team, empty);
                if (empty || team == null) {
                    setText(null);
                } else {
                    setText(team);
                    setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                }
            }
        });

        colWins.setCellValueFactory(new PropertyValueFactory<>("wins"));
        colLosses.setCellValueFactory(new PropertyValueFactory<>("losses"));
        colDraws.setCellValueFactory(new PropertyValueFactory<>("draws"));

        // Win Rate with yellow/orange color
        colWinRate.setCellValueFactory(new PropertyValueFactory<>("winRate"));
        colWinRate.setCellFactory(column -> new TableCell<LeaderboardEntry, Double>() {
            @Override
            protected void updateItem(Double rate, boolean empty) {
                super.updateItem(rate, empty);
                if (empty || rate == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", rate));
                    setStyle("-fx-text-fill: #FFB347;"); // Light Orange/Yellow
                }
            }
        });

        // Points with bold text
        colPoints.setCellValueFactory(new PropertyValueFactory<>("points"));
        colPoints.setCellFactory(column -> new TableCell<LeaderboardEntry, Integer>() {
            @Override
            protected void updateItem(Integer points, boolean empty) {
                super.updateItem(points, empty);
                if (empty || points == null) {
                    setText(null);
                } else {
                    setText(points.toString());
                    setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                }
            }
        });

        leaderboardTable.setItems(leaderboardData);
    }

    private void loadLeaderboard() {
        try {
            List<LeaderboardEntry> entries = leaderboardService.getTeamLeaderboard();
            leaderboardData.setAll(entries);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load leaderboard");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
