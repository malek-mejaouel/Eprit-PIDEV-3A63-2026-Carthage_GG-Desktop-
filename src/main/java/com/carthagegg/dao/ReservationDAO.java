package com.carthagegg.dao;

import com.carthagegg.models.Reservation;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private Connection conn;

    public ReservationDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Reservation> findAll() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapReservation(rs));
        }
        return list;
    }

    public void save(Reservation r) throws SQLException {
        String sql = "INSERT INTO reservation (name, price, reservation_date, event_id, seats, status, user_id) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getName());
            ps.setBigDecimal(2, r.getPrice());
            ps.setTimestamp(3, r.getReservationDate() != null ? Timestamp.valueOf(r.getReservationDate()) : new Timestamp(System.currentTimeMillis()));
            ps.setInt(4, r.getEventId());
            ps.setInt(5, r.getSeats());
            ps.setString(6, r.getStatus().name());
            ps.setInt(7, r.getUserId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) r.setId(rs.getInt(1));
            }
        }
    }

    public void update(Reservation r) throws SQLException {
        String sql = "UPDATE reservation SET name=?, price=?, reservation_date=?, event_id=?, seats=?, status=?, user_id=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getName());
            ps.setBigDecimal(2, r.getPrice());
            ps.setTimestamp(3, r.getReservationDate() != null ? Timestamp.valueOf(r.getReservationDate()) : new Timestamp(System.currentTimeMillis()));
            ps.setInt(4, r.getEventId());
            ps.setInt(5, r.getSeats());
            ps.setString(6, r.getStatus().name());
            ps.setInt(7, r.getUserId());
            ps.setInt(8, r.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Reservation> findByUserId(int userId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapReservation(rs));
            }
        }
        return list;
    }

    public Reservation findById(int id) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReservation(rs);
                }
            }
        }
        return null;
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setName(rs.getString("name"));
        r.setPrice(rs.getBigDecimal("price"));
        Timestamp date = rs.getTimestamp("reservation_date");
        if (date != null) r.setReservationDate(date.toLocalDateTime());
        r.setEventId(rs.getInt("event_id"));
        r.setSeats(rs.getInt("seats"));
        r.setStatus(Reservation.Status.valueOf(rs.getString("status")));
        r.setUserId(rs.getInt("user_id"));
        return r;
    }
}
