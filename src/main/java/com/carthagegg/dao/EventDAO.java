package com.carthagegg.dao;

import com.carthagegg.models.Event;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    private Connection conn;

    public EventDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Event> findAll() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event"; // Removed ORDER BY start_at DESC in case column name is different
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapEvent(rs));
        }
        return list;
    }

    public void save(Event e) throws SQLException {
        String sql = "INSERT INTO event (title, description, start_at, end_at, location_id, max_seats) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDescription());
            if (e.getStartAt() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(e.getStartAt()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            if (e.getEndAt() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(e.getEndAt()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }
            ps.setInt(5, e.getLocationId());
            ps.setInt(6, e.getMaxSeats());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) e.setId(rs.getInt(1));
            }
        }
    }

    public void update(Event e) throws SQLException {
        String sql = "UPDATE event SET title=?, description=?, start_at=?, end_at=?, location_id=?, max_seats=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDescription());
            if (e.getStartAt() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(e.getStartAt()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            if (e.getEndAt() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(e.getEndAt()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }
            ps.setInt(5, e.getLocationId());
            ps.setInt(6, e.getMaxSeats());
            ps.setInt(7, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM event WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        Event e = new Event();
        if (hasColumn(rs, "id")) {
            e.setId(rs.getInt("id"));
        } else if (hasColumn(rs, "event_id")) {
            e.setId(rs.getInt("event_id"));
        }
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        
        if (hasColumn(rs, "start_at")) {
            Timestamp startAt = rs.getTimestamp("start_at");
            if (startAt != null) e.setStartAt(startAt.toLocalDateTime());
        } else if (hasColumn(rs, "start_date")) {
            Timestamp startAt = rs.getTimestamp("start_date");
            if (startAt != null) e.setStartAt(startAt.toLocalDateTime());
        }
        
        if (hasColumn(rs, "end_at")) {
            Timestamp endAt = rs.getTimestamp("end_at");
            if (endAt != null) e.setEndAt(endAt.toLocalDateTime());
        } else if (hasColumn(rs, "end_date")) {
            Timestamp endAt = rs.getTimestamp("end_date");
            if (endAt != null) e.setEndAt(endAt.toLocalDateTime());
        }
        
        if (hasColumn(rs, "location_id")) {
            e.setLocationId(rs.getInt("location_id"));
        } else if (hasColumn(rs, "location")) {
            e.setLocationId(rs.getInt("location"));
        }
        
        if (hasColumn(rs, "max_seats")) {
            e.setMaxSeats(rs.getInt("max_seats"));
        } else if (hasColumn(rs, "seats")) {
            e.setMaxSeats(rs.getInt("seats"));
        }
        return e;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        for (int i = 1; i <= count; i++) {
            String label = meta.getColumnLabel(i);
            String name = meta.getColumnName(i);
            if (columnName.equalsIgnoreCase(label) || columnName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
