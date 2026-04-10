package com.carthagegg.dao;

import com.carthagegg.models.Tournament;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournamentDAO {
    private Connection conn;

    public TournamentDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Tournament> findAll() throws SQLException {
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT * FROM tournaments ORDER BY start_date DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapTournament(rs));
        }
        return list;
    }

    public void save(Tournament t) throws SQLException {
        String sql = "INSERT INTO tournaments (tournament_name, start_date, end_date, prize_pool, location, game_id, user_id) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTournamentName());
            ps.setDate(2, Date.valueOf(t.getStartDate()));
            ps.setDate(3, Date.valueOf(t.getEndDate()));
            ps.setBigDecimal(4, t.getPrizePool());
            ps.setString(5, t.getLocation());
            ps.setInt(6, t.getGameId());
            ps.setInt(7, t.getUserId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setTournamentId(rs.getInt(1));
            }
        }
    }

    public void update(Tournament t) throws SQLException {
        String sql = "UPDATE tournaments SET tournament_name=?, start_date=?, end_date=?, prize_pool=?, location=?, game_id=?, user_id=? WHERE tournament_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTournamentName());
            ps.setDate(2, Date.valueOf(t.getStartDate()));
            ps.setDate(3, Date.valueOf(t.getEndDate()));
            ps.setBigDecimal(4, t.getPrizePool());
            ps.setString(5, t.getLocation());
            ps.setInt(6, t.getGameId());
            ps.setInt(7, t.getUserId());
            ps.setInt(8, t.getTournamentId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM tournaments WHERE tournament_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Tournament mapTournament(ResultSet rs) throws SQLException {
        Tournament t = new Tournament();
        t.setTournamentId(rs.getInt("tournament_id"));
        t.setTournamentName(rs.getString("tournament_name"));
        t.setStartDate(rs.getDate("start_date").toLocalDate());
        t.setEndDate(rs.getDate("end_date").toLocalDate());
        t.setPrizePool(rs.getBigDecimal("prize_pool"));
        t.setLocation(rs.getString("location"));
        t.setGameId(rs.getInt("game_id"));
        t.setUserId(rs.getInt("user_id"));
        return t;
    }
}
