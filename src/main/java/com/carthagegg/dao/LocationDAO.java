package com.carthagegg.dao;

import com.carthagegg.models.Location;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationDAO {
    private Connection conn;

    public LocationDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Location> findAll() throws SQLException {
        List<Location> list = new ArrayList<>();
        String sql = "SELECT * FROM location";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapLocation(rs));
        }
        return list;
    }

    public void save(Location l) throws SQLException {
        String sql = "INSERT INTO location (name, address, capacity, latitude, longitude, place_id) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, l.getName());
            ps.setString(2, l.getAddress());
            ps.setInt(3, l.getCapacity());
            ps.setDouble(4, l.getLatitude());
            ps.setDouble(5, l.getLongitude());
            ps.setString(6, l.getPlaceId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) l.setId(rs.getInt(1));
            }
        }
    }

    public void update(Location l) throws SQLException {
        String sql = "UPDATE location SET name=?, address=?, capacity=?, latitude=?, longitude=?, place_id=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getName());
            ps.setString(2, l.getAddress());
            ps.setInt(3, l.getCapacity());
            ps.setDouble(4, l.getLatitude());
            ps.setDouble(5, l.getLongitude());
            ps.setString(6, l.getPlaceId());
            ps.setInt(7, l.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM location WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Location findById(int id) throws SQLException {
        String sql = "SELECT * FROM location WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapLocation(rs);
                }
            }
        }
        return null;
    }

    private Location mapLocation(ResultSet rs) throws SQLException {
        Location l = new Location();
        l.setId(rs.getInt("id"));
        l.setName(rs.getString("name"));
        l.setAddress(rs.getString("address"));
        l.setCapacity(rs.getInt("capacity"));
        l.setLatitude(rs.getDouble("latitude"));
        l.setLongitude(rs.getDouble("longitude"));
        l.setPlaceId(rs.getString("place_id"));
        return l;
    }
}
