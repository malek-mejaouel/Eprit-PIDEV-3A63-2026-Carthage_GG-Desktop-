package com.carthagegg.dao;

import com.carthagegg.models.Reclamation;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    public void save(Reclamation rec) throws SQLException {
        String sql = "INSERT INTO reclamation (user_id, title, description, status) VALUES (?, ?, ?, 'pending')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rec.getUserId());
            ps.setString(2, rec.getTitle());
            ps.setString(3, rec.getDescription());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) rec.setId(rs.getInt(1));
            }
        }
    }

    public List<Reclamation> findByUserId(int userId) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapReclamation(rs));
            }
        }
        return list;
    }

    public List<Reclamation> findAll() throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT r.*, u.username FROM reclamation r JOIN users u ON r.user_id = u.user_id ORDER BY r.created_at DESC";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Reclamation r = mapReclamation(rs);
                r.setUsername(rs.getString("username"));
                list.add(r);
            }
        }
        return list;
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE reclamation SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reclamation WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Reclamation mapReclamation(ResultSet rs) throws SQLException {
        Reclamation r = new Reclamation();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setStatus(rs.getString("status"));
        
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());
        
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) r.setUpdatedAt(updated.toLocalDateTime());
        
        return r;
    }
}
