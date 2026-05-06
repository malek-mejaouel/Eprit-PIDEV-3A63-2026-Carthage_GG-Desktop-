package com.carthagegg.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RivalryDetector {

    // Returns true if two teams have played against each other at least 3 times
    public boolean isRivalry(int teamAId, int teamBId) {
        int matchCount = countHeadToHeadMatches(teamAId, teamBId);
        return matchCount >= 3;
    }

    // Count how many times two teams have faced each other in the matches table
    private int countHeadToHeadMatches(int teamAId, int teamBId) {
        String sql = "SELECT COUNT(*) FROM matches " +
                     "WHERE (team_a_id = ? AND team_b_id = ?) " +
                     "OR (team_a_id = ? AND team_b_id = ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamAId);
            ps.setInt(2, teamBId);
            ps.setInt(3, teamBId);
            ps.setInt(4, teamAId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getRivalrySummary(int teamAId, int teamBId, String teamAName, String teamBName) {
        int matches = 0;
        int teamAWins = 0;
        int teamBWins = 0;

        String sql = "SELECT score_team_a, score_team_b, team_a_id, team_b_id FROM matches " +
                     "WHERE (team_a_id = ? AND team_b_id = ?) " +
                     "OR (team_a_id = ? AND team_b_id = ?)";
        
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamAId);
            ps.setInt(2, teamBId);
            ps.setInt(3, teamBId);
            ps.setInt(4, teamAId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    matches++;
                    int sA = rs.getInt("score_team_a");
                    int sB = rs.getInt("score_team_b");
                    int tA = rs.getInt("team_a_id");

                    if (sA > sB) {
                        if (tA == teamAId) teamAWins++;
                        else teamBWins++;
                    } else if (sB > sA) {
                        if (tA == teamAId) teamBWins++;
                        else teamAWins++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        boolean isRivalry = matches >= 3;
        return String.format("%s vs %s - %d matches | %s won %d | %s won %d | Rivalry: %s",
                teamAName, teamBName, matches, teamAName, teamAWins, teamBName, teamBWins, isRivalry ? "YES 🔥" : "NO");
    }
}
