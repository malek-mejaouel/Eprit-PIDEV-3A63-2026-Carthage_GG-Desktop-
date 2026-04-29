package com.carthagegg.dao;

import com.carthagegg.models.Team;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    public List<Team> findAll() throws SQLException {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT * FROM teams ORDER BY creation_date DESC";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapTeam(rs));
        }
        return list;
    }

    public void save(Team t) throws SQLException {
        String sql = "INSERT INTO teams (team_name, logo, creation_date, user_id) VALUES (?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTeamName());
            ps.setString(2, t.getLogo());
            ps.setDate(3, Date.valueOf(t.getCreationDate()));
            ps.setInt(4, t.getUserId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setTeamId(rs.getInt(1));
            }
        }
    }

    public void update(Team t) throws SQLException {
        String sql = "UPDATE teams SET team_name=?, logo=?, creation_date=?, user_id=? WHERE team_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTeamName());
            ps.setString(2, t.getLogo());
            ps.setDate(3, Date.valueOf(t.getCreationDate()));
            ps.setInt(4, t.getUserId());
            ps.setInt(5, t.getTeamId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM teams WHERE team_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Team mapTeam(ResultSet rs) throws SQLException {
        Team t = new Team();
        t.setTeamId(rs.getInt("team_id"));
        t.setTeamName(rs.getString("team_name"));
        t.setLogo(rs.getString("logo"));
        t.setCreationDate(rs.getDate("creation_date").toLocalDate());
        t.setUserId(rs.getInt("user_id"));
        return t;
    }
}
