package com.carthagegg.dao;

import com.carthagegg.utils.DatabaseConnection;
import java.sql.*;

public class StatsDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    public int countTournaments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tournaments";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countTeams() throws SQLException {
        String sql = "SELECT COUNT(*) FROM teams";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(price) FROM products"; 
        try (Connection conn = getConnection();
             Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    public double getTournamentPrizePool() throws SQLException {
        String sql = "SELECT SUM(prize_pool) FROM tournaments";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    public int countActiveStreams() throws SQLException {
        String sql = "SELECT COUNT(*) FROM streams WHERE is_live = 1";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countUpcomingEvents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM event WHERE start_at > NOW()";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
