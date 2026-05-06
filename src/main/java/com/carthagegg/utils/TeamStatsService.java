package com.carthagegg.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service to fetch team statistics directly from the local MariaDB database.
 */
public class TeamStatsService {

    public TeamStatsService() {
        // No special initialization needed for DB-based stats
    }

    /**
     * Fetches statistics for a team from the local database matches table.
     */
    public String getTeamSummary(String teamName) throws IOException {
        String sql = "SELECT " +
                     "    t.team_name, " +
                     "    SUM(CASE WHEN (m.team_a_id = t.team_id AND m.score_team_a > m.score_team_b) OR (m.team_b_id = t.team_id AND m.score_team_b > m.score_team_a) THEN 1 ELSE 0 END) AS wins, " +
                     "    SUM(CASE WHEN (m.team_a_id = t.team_id AND m.score_team_a < m.score_team_b) OR (m.team_b_id = t.team_id AND m.score_team_b < m.score_team_a) THEN 1 ELSE 0 END) AS losses, " +
                     "    SUM(CASE WHEN (m.team_a_id = t.team_id OR m.team_b_id = t.team_id) AND m.score_team_a = m.score_team_b THEN 1 ELSE 0 END) AS draws, " +
                     "    SUM(CASE WHEN m.team_a_id = t.team_id THEN m.score_team_a " +
                     "             WHEN m.team_b_id = t.team_id THEN m.score_team_b " +
                     "             ELSE 0 END) AS total_goals " +
                     "FROM teams t " +
                     "LEFT JOIN matches m ON t.team_id = m.team_a_id OR t.team_id = m.team_b_id " +
                     "WHERE t.team_name = ? " +
                     "GROUP BY t.team_id, t.team_name";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, teamName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("team_name");
                    int wins = rs.getInt("wins");
                    int losses = rs.getInt("losses");
                    int draws = rs.getInt("draws");
                    int goals = rs.getInt("total_goals");

                    return String.format(
                        "📊 Team Statistics\n" +
                        "------------------\n" +
                        "Team Name  : %s\n" +
                        "Wins       : %d\n" +
                        "Losses     : %d\n" +
                        "Draws      : %d\n" +
                        "Goals      : %d",
                        name, wins, losses, draws, goals
                    );
                } else {
                    return "No stats found for team: " + teamName;
                }
            }
        } catch (SQLException e) {
            throw new IOException("Database error while fetching team stats: " + e.getMessage());
        }
    }
}
