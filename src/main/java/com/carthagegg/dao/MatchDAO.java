package com.carthagegg.dao;

import com.carthagegg.models.Match;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchDAO {
    
    static {
        // Run database migration to ensure is_rivalry exists
        try (java.sql.Connection conn = com.carthagegg.utils.DatabaseConnection.getInstance();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE matches ADD COLUMN IF NOT EXISTS is_rivalry BOOLEAN DEFAULT FALSE");
        } catch (Exception e) {
            // Ignore if column already exists or IF NOT EXISTS not supported
            try (java.sql.Connection conn = com.carthagegg.utils.DatabaseConnection.getInstance();
                 java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE matches ADD COLUMN is_rivalry BOOLEAN DEFAULT FALSE");
            } catch (Exception ex) {
                // Already exists or other error
            }
        }
    }

    public MatchDAO() {
    }

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    public List<Match> findAll() throws SQLException {
        List<Match> list = new ArrayList<>();
        String sqlWithJoins = "SELECT m.*, t1.team_name as team_a_name, t2.team_name as team_b_name, tr.tournament_name, m.is_rivalry " +
                              "FROM matches m " +
                              "LEFT JOIN teams t1 ON m.team_a_id = t1.team_id " +
                              "LEFT JOIN teams t2 ON m.team_b_id = t2.team_id " +
                              "LEFT JOIN tournaments tr ON m.tournament_id = tr.tournament_id " +
                              "ORDER BY m.match_date DESC";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sqlWithJoins)) {
            while (rs.next()) list.add(mapMatch(rs));
        } catch (SQLException ex) {
            String sqlFallback = "SELECT * FROM matches ORDER BY match_date DESC";
            try (Connection conn = getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlFallback)) {
                while (rs.next()) list.add(mapMatch(rs));
            }
        }
        return list;
    }

    public void save(Match m) throws SQLException {
        if (m.getTeamAId() == m.getTeamBId()) {
            throw new IllegalArgumentException("A match cannot have the same team on both sides.");
        }
        String sql = "INSERT INTO matches (match_date, score_team_a, score_team_b, tournament_id, game_id, team_a_id, team_b_id, is_rivalry) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (m.getMatchDate() != null) {
                ps.setTimestamp(1, Timestamp.valueOf(m.getMatchDate()));
            } else {
                ps.setNull(1, Types.TIMESTAMP);
            }
            ps.setInt(2, m.getScoreTeamA());
            ps.setInt(3, m.getScoreTeamB());
            ps.setInt(4, m.getTournamentId());
            ps.setInt(5, m.getGameId());
            ps.setInt(6, m.getTeamAId());
            ps.setInt(7, m.getTeamBId());
            ps.setBoolean(8, m.isRivalry());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) m.setMatchId(rs.getInt(1));
            }
        }
        checkAndUpdateRivalry(m.getTeamAId(), m.getTeamBId());
    }

    public void update(Match m) throws SQLException {
        if (m.getTeamAId() == m.getTeamBId()) {
            throw new IllegalArgumentException("A match cannot have the same team on both sides.");
        }
        String sql = "UPDATE matches SET match_date=?, score_team_a=?, score_team_b=?, tournament_id=?, game_id=?, team_a_id=?, team_b_id=?, is_rivalry=? WHERE match_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (m.getMatchDate() != null) {
                ps.setTimestamp(1, Timestamp.valueOf(m.getMatchDate()));
            } else {
                ps.setNull(1, Types.TIMESTAMP);
            }
            ps.setInt(2, m.getScoreTeamA());
            ps.setInt(3, m.getScoreTeamB());
            ps.setInt(4, m.getTournamentId());
            ps.setInt(5, m.getGameId());
            ps.setInt(6, m.getTeamAId());
            ps.setInt(7, m.getTeamBId());
            ps.setBoolean(8, m.isRivalry());
            ps.setInt(9, m.getMatchId());
            ps.executeUpdate();
        }
        checkAndUpdateRivalry(m.getTeamAId(), m.getTeamBId());
    }

    private void checkAndUpdateRivalry(int teamAId, int teamBId) throws SQLException {
        com.carthagegg.utils.RivalryDetector detector = new com.carthagegg.utils.RivalryDetector();
        if (detector.isRivalry(teamAId, teamBId)) {
            updateRivalryFlag(teamAId, teamBId, true);
        }
    }

    private void updateRivalryFlag(int teamAId, int teamBId, boolean isRivalry) throws SQLException {
        String sql = "UPDATE matches SET is_rivalry = ? WHERE (team_a_id = ? AND team_b_id = ?) OR (team_a_id = ? AND team_b_id = ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isRivalry);
            ps.setInt(2, teamAId);
            ps.setInt(3, teamBId);
            ps.setInt(4, teamBId);
            ps.setInt(5, teamAId);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM matches WHERE match_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Match mapMatch(ResultSet rs) throws SQLException {
        Match m = new Match();
        m.setMatchId(rs.getInt("match_id"));
        
        Timestamp matchDate = rs.getTimestamp("match_date");
        if (matchDate != null) {
            m.setMatchDate(matchDate.toLocalDateTime());
        }
        
        m.setScoreTeamA(rs.getInt("score_team_a"));
        m.setScoreTeamB(rs.getInt("score_team_b"));
        m.setTournamentId(rs.getInt("tournament_id"));
        m.setGameId(rs.getInt("game_id"));
        m.setTeamAId(rs.getInt("team_a_id"));
        m.setTeamBId(rs.getInt("team_b_id"));
        
        try {
            m.setRivalry(rs.getBoolean("is_rivalry"));
        } catch (SQLException e) {
            // Column might be missing if migration hasn't run yet
            m.setRivalry(false);
        }
        
        // Try to map joined columns if they exist
        try {
            m.setTeamAName(rs.getString("team_a_name"));
            m.setTeamBName(rs.getString("team_b_name"));
            m.setTournamentName(rs.getString("tournament_name"));
        } catch (SQLException e) {
            // Columns might not exist in all queries (like save/update)
        }
        
        return m;
    }

    public List<Match> findByTeams(int teamAId, int teamBId) throws SQLException {
        List<Match> list = new ArrayList<>();
        String sql = "SELECT m.*, t1.team_name as team_a_name, t2.team_name as team_b_name, tr.tournament_name " +
                     "FROM matches m " +
                     "LEFT JOIN teams t1 ON m.team_a_id = t1.team_id " +
                     "LEFT JOIN teams t2 ON m.team_b_id = t2.team_id " +
                     "LEFT JOIN tournaments tr ON m.tournament_id = tr.tournament_id " +
                     "WHERE (m.team_a_id = ? AND m.team_b_id = ?) OR (m.team_a_id = ? AND m.team_b_id = ?) " +
                     "ORDER BY m.match_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamAId);
            ps.setInt(2, teamBId);
            ps.setInt(3, teamBId);
            ps.setInt(4, teamAId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapMatch(rs));
            }
        }
        return list;
    }

    public List<Match> findByTeam(int teamId) throws SQLException {
        List<Match> list = new ArrayList<>();
        String sql = "SELECT m.*, t1.team_name as team_a_name, t2.team_name as team_b_name, tr.tournament_name " +
                     "FROM matches m " +
                     "LEFT JOIN teams t1 ON m.team_a_id = t1.team_id " +
                     "LEFT JOIN teams t2 ON m.team_b_id = t2.team_id " +
                     "LEFT JOIN tournaments tr ON m.tournament_id = tr.tournament_id " +
                     "WHERE m.team_a_id = ? OR m.team_b_id = ? " +
                     "ORDER BY m.match_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapMatch(rs));
            }
        }
        return list;
    }
}
