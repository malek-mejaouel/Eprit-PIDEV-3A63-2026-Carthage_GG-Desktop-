package com.carthagegg.utils;

import com.carthagegg.models.LeaderboardEntry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardService {

    public List<LeaderboardEntry> getTeamLeaderboard() throws SQLException {
        List<LeaderboardEntry> entries = new ArrayList<>();
        
        // Note: We use a simplified SQL to get raw counts, then rank in Java
        // The user's SQL logic for winner_id assumes a column exists. 
        // Let's adapt based on match scores if winner_id isn't present.
        String sql = "SELECT " +
                     "    t.team_id, " +
                     "    t.team_name, " +
                     "    SUM(CASE WHEN (m.team_a_id = t.team_id AND m.score_team_a > m.score_team_b) OR (m.team_b_id = t.team_id AND m.score_team_b > m.score_team_a) THEN 1 ELSE 0 END) AS wins, " +
                     "    SUM(CASE WHEN (m.team_a_id = t.team_id AND m.score_team_a < m.score_team_b) OR (m.team_b_id = t.team_id AND m.score_team_b < m.score_team_a) THEN 1 ELSE 0 END) AS losses, " +
                     "    SUM(CASE WHEN (m.team_a_id = t.team_id OR m.team_b_id = t.team_id) AND m.score_team_a = m.score_team_b THEN 1 ELSE 0 END) AS draws " +
                     "FROM teams t " +
                     "LEFT JOIN matches m ON t.team_id = m.team_a_id OR t.team_id = m.team_b_id " +
                     "GROUP BY t.team_id, t.team_name";

        try (Connection conn = DatabaseConnection.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                LeaderboardEntry entry = new LeaderboardEntry();
                entry.setTeamId(rs.getInt("team_id"));
                entry.setTeamName(rs.getString("team_name"));
                int wins = rs.getInt("wins");
                int losses = rs.getInt("losses");
                int draws = rs.getInt("draws");
                
                entry.setWins(wins);
                entry.setLosses(losses);
                entry.setDraws(draws);
                
                // Calculate Points: Win = 3, Draw = 1, Loss = 0
                entry.setPoints((wins * 3) + (draws * 1));
                
                // Calculate Win Rate: (wins / (wins + losses + draws)) * 100
                double totalMatches = wins + losses + draws;
                double winRate = (totalMatches > 0) ? (wins * 100.0) / totalMatches : 0.0;
                entry.setWinRate(winRate);
                
                entries.add(entry);
            }
        }

        // Apply Ranking Algorithm:
        // 1st → Most Points
        // 2nd → If equal points → highest Win Rate
        // 3rd → If equal Win Rate → most Wins
        // 4th → If still equal → alphabetical by team name
        List<LeaderboardEntry> rankedEntries = entries.stream()
            .sorted((e1, e2) -> {
                if (e1.getPoints() != e2.getPoints()) return e2.getPoints() - e1.getPoints();
                if (Double.compare(e2.getWinRate(), e1.getWinRate()) != 0) 
                    return Double.compare(e2.getWinRate(), e1.getWinRate());
                if (e1.getWins() != e2.getWins()) return e2.getWins() - e1.getWins();
                return e1.getTeamName().compareToIgnoreCase(e2.getTeamName());
            })
            .collect(Collectors.toList());

        // Set Rank numbers
        for (int i = 0; i < rankedEntries.size(); i++) {
            rankedEntries.get(i).setRank(i + 1);
        }

        return rankedEntries;
    }
}
