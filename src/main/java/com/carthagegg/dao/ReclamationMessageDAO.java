package com.carthagegg.dao;

import com.carthagegg.models.ReclamationMessage;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationMessageDAO {

    public void save(ReclamationMessage msg) throws SQLException {
        String sql = "INSERT INTO reclamation_messages (reclamation_id, sender_id, message) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, msg.getReclamationId());
            ps.setInt(2, msg.getSenderId());
            ps.setString(3, msg.getMessage());
            ps.executeUpdate();
        }
    }

    public List<ReclamationMessage> findByReclamationId(int reclamationId) throws SQLException {
        List<ReclamationMessage> messages = new ArrayList<>();
        String sql = "SELECT rm.*, u.username FROM reclamation_messages rm " +
                     "JOIN users u ON rm.sender_id = u.user_id " +
                     "WHERE rm.reclamation_id = ? ORDER BY rm.created_at ASC";
        
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReclamationMessage m = new ReclamationMessage();
                    m.setId(rs.getInt("id"));
                    m.setReclamationId(rs.getInt("reclamation_id"));
                    m.setSenderId(rs.getInt("sender_id"));
                    m.setSenderUsername(rs.getString("username"));
                    m.setMessage(rs.getString("message"));
                    m.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    messages.add(m);
                }
            }
        }
        return messages;
    }
}
