package com.carthagegg.dao;

import com.carthagegg.models.Stream;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StreamDAO {
    private Connection conn;

    public StreamDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Stream> findAll() throws SQLException {
        List<Stream> list = new ArrayList<>();
        String sql = "SELECT * FROM streams ORDER BY created_at DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapStream(rs));
        }
        return list;
    }

    public void save(Stream s) throws SQLException {
        String sql = "INSERT INTO streams (title, description, platform, channel_name, youtube_video_id, thumbnail, is_live, viewer_count, created_by, created_at, updated_at) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,NOW(),NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getTitle());
            ps.setString(2, s.getDescription());
            ps.setString(3, s.getPlatform());
            ps.setString(4, s.getChannelName());
            ps.setString(5, s.getYoutubeVideoId());
            ps.setString(6, s.getThumbnail());
            ps.setBoolean(7, s.isLive());
            ps.setInt(8, s.getViewerCount());
            ps.setInt(9, s.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setStreamId(rs.getInt(1));
            }
        }
    }

    public void update(Stream s) throws SQLException {
        String sql = "UPDATE streams SET title=?, description=?, platform=?, channel_name=?, youtube_video_id=?, thumbnail=?, is_live=?, viewer_count=?, updated_at=NOW() WHERE stream_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getTitle());
            ps.setString(2, s.getDescription());
            ps.setString(3, s.getPlatform());
            ps.setString(4, s.getChannelName());
            ps.setString(5, s.getYoutubeVideoId());
            ps.setString(6, s.getThumbnail());
            ps.setBoolean(7, s.isLive());
            ps.setInt(8, s.getViewerCount());
            ps.setInt(9, s.getStreamId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM streams WHERE stream_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Stream mapStream(ResultSet rs) throws SQLException {
        Stream s = new Stream();
        s.setStreamId(rs.getInt("stream_id"));
        s.setTitle(rs.getString("title"));
        s.setDescription(rs.getString("description"));
        s.setPlatform(rs.getString("platform"));
        s.setChannelName(rs.getString("channel_name"));
        s.setYoutubeVideoId(rs.getString("youtube_video_id"));
        s.setThumbnail(rs.getString("thumbnail"));
        s.setLive(rs.getBoolean("is_live"));
        s.setViewerCount(rs.getInt("viewer_count"));
        s.setCreatedBy(rs.getInt("created_by"));
        s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return s;
    }
}
